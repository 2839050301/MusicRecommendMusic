package com.streaming

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.JavaConversions._

// 定义一个基准推荐对象
case class Recommendation(songId: Long, score: Double)

// 定义基于预测评分的用户推荐列表
case class UserRecs(userId: Int, recommendations: Seq[Recommendation])

// 定义基于 LFM 歌曲特征向量的歌曲相似度列表
case class SongRecs(songId: Long, recommendations: Seq[Recommendation])

// 定义基于内容的歌曲推荐列表
case class ContentSongRecs(songId: Long, recs: Seq[Recommendation])

object ConnHekper extends Serializable {
  lazy val mongoClient = MongoClient(MongoClientURI("mongodb://192.168.187.131:27017/recommender"))
}

case class MongConfig(uri: String, db: String)

object StreamingRecommender {
  val MAX_USER_RATINGS_NUM = 20
  val MAX_SIM_SONG_NUM = 20
  val MONGODB_STREAM_RECS_COLLECTION = "StreamRecs"
  val MONGODB_USER_LIKE_COLLECTION = "User_like"
  val MONGODB_SONG_RECS_COLLECTION = "SongRecs"
  val MONGODB_CONTENT_SONG_RECS_COLLECTION = "ContentSongRecs"
  val MONGODB_USER_RECS_COLLECTION = "UserRecs"

  // 入口方法
  def main(args: Array[String]): Unit = {

    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.187.131:27017/recommender",
      "mongo.db" -> "recommender",
      "kafka.topic" -> "recommender"
    )
    // 创建一个 SparkConf 配置
    val sparkConf = new SparkConf().setAppName("StreamingRecommender").setMaster(config("spark.cores"))

    // 创建 Spark 的对象
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()
    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Seconds(2))

    implicit val mongConfig = MongConfig(config("mongo.uri"), config("mongo.db"))
    import spark.implicits._

    // *************************** 广播歌曲相似度矩阵
    // 转换成 Map[Long,Map[Long,Double]]
    val simSongsMatrix = spark
      .read
      .option("uri", config("mongo.uri"))
      .option("collection", MONGODB_SONG_RECS_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[SongRecs]
      .rdd
      .map { recs =>
        (recs.songId, recs.recommendations.map(x => (x.songId, x.score)).toMap)
      }.collectAsMap()

    val simSongsMatrixBroadCast = sc.broadcast(simSongsMatrix)

    // *************************** 广播基于内容的歌曲推荐矩阵
    val contentSongRecsMatrix = spark
      .read
      .option("uri", config("mongo.uri"))
      .option("collection", MONGODB_CONTENT_SONG_RECS_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[ContentSongRecs]
      .rdd
      .map { recs =>
        (recs.songId, recs.recs.map(x => (x.songId, x.score)).toMap)
      }.collectAsMap()

    val contentSongRecsMatrixBroadCast = sc.broadcast(contentSongRecsMatrix)

    // *************************** 广播基于用户的推荐矩阵
    val userRecsMatrix = spark
      .read
      .option("uri", config("mongo.uri"))
      .option("collection", MONGODB_USER_RECS_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[UserRecs]
      .rdd
      .map { recs =>
        (recs.userId, recs.recommendations.map(x => (x.songId, x.score)).toMap)
      }.collectAsMap()

    val userRecsMatrixBroadCast = sc.broadcast(userRecsMatrix)

    // 创建到 Kafka 的连接
    val kafkaPara = Map(
      "bootstrap.servers" -> "linux:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "recommender",
      "auto.offset.reset" -> "latest"
    )

    val kafkaStream = KafkaUtils.createDirectStream[String, String](ssc, LocationStrategies.PreferConsistent, ConsumerStrategies.Subscribe[String, String](Array(config("kafka.topic")), kafkaPara))

    // User_id|sid
    val likeStream = kafkaStream.map { case msg =>
      var attr = msg.value().split("\\|")
      if (attr.length == 2) {
        try {
          val userId = attr(0).toInt
          val songId = attr(1).toLong
          println(s"Received Kafka message: userId=$userId, songId=$songId")  // 增加日志输出
          (userId, songId)
        } catch {
          case e: NumberFormatException =>
            println(s"Invalid number format in Kafka message: ${msg.value()}")  // 增加日志输出
            (-1, -1L)  // 返回无效值
        }
      } else {
        println(s"Invalid Kafka message format: ${msg.value()}")  // 增加日志输出
        (-1, -1L)  // 返回无效值
      }
    }

    likeStream.foreachRDD { rdd =>
      rdd.filter { case (userId, songId) => userId != -1 && songId != -1L }  // 过滤无效值
        .map { case (userId, songId) =>
          println(">>>>>>>>>>>>>>")
          println(s"Processing userId=$userId, songId=$songId")  // 增加日志输出

        // 获取当前最近的 M 次歌曲

        // 获取歌曲 P 最相似的 K 个歌曲
        val simSongs = getTopSimSongs(MAX_SIM_SONG_NUM, songId, userId, simSongsMatrixBroadCast.value)

        // 获取基于内容的歌曲推荐
        val contentRecs = getContentRecs(songId, contentSongRecsMatrixBroadCast.value)

        // 获取基于用户的推荐
        val userRecs = getUserRecs(userId, userRecsMatrixBroadCast.value)

        // 合并三种推荐结果并计算综合得分
        val combinedRecs = combineRecs(simSongs, contentRecs, userRecs, simSongsMatrixBroadCast.value, contentSongRecsMatrixBroadCast.value, userRecsMatrixBroadCast.value)

        // 计算待选歌曲推荐优先级

        // 将数据保存到 MongoDB
      }.count()
    }

    // 启动 Streaming 程序
    ssc.start()
    ssc.awaitTermination()
  }

  def saveRecsToMongoDB(userId: Int, streamRecs: Array[(Long, Double)])(implicit mongConfig: MongConfig): Unit = {
    // 定义到 StreamRecs 表的连接
    val streamRecsCollection = ConnHekper.mongoClient(mongConfig.db)(MONGODB_STREAM_RECS_COLLECTION)

    // 如果表中已有 userId 对应的数据，则删除
    streamRecsCollection.findAndRemove(MongoDBObject("userId" -> userId))

    // 按评分降序排序
    val sortedStreamRecs = streamRecs.sortBy(-_._2)

    // 将 streamRecs 数据存入表中
    streamRecsCollection.insert(MongoDBObject("userId" -> userId,
      "recs" -> sortedStreamRecs.map(x => MongoDBObject("songId" -> x._1, "score" -> x._2))))
  }

  def computeSongScores(simSongs: scala.collection.Map[Long, Map[Long, Double]], userRecentlyRatings: Array[(Long, Double)], topsimSongs: Array[Long]): Array[(Long, Double)] = {
    // 用于保存每一个待选歌曲和最近评分的每一个歌曲的权重得分
    val score = scala.collection.mutable.ArrayBuffer[(Long, Double)]()

    // 用于保存每一个歌曲的增强因子
    val increMap = scala.collection.mutable.HashMap[Long, Int]()

    // 用于保存每一个歌曲的减弱因子数
    val decreMap = scala.collection.mutable.HashMap[Long, Int]()

    for (topsimSong <- topsimSongs; userRecentlyRating <- userRecentlyRatings) {
      val simScore = getSongsSimScore(simSongs, userRecentlyRating._1, topsimSong)
      if (simScore > 0.5) {
        score += ((topsimSong, simScore))
        increMap(topsimSong) = increMap.getOrElse(topsimSong, 0) + 1
      }
    }

    // 综合得分
    val finalScores = score.groupBy(_._1).map { case (songId, songScores) =>
      val totalScore = songScores.map(_._2).sum
      val finalScore = totalScore * (1 + math.log10(increMap.getOrElse(songId, 1).toDouble))
      (songId, finalScore)
    }.toArray
    finalScores
  }

  def getSongsSimScore(simSongs: scala.collection.Map[Long, Map[Long, Double]], userRatingSong: Long, topSimSong: Long): Double = {
    simSongs.get(userRatingSong).flatMap(_.get(topSimSong)).getOrElse(0.0)
  }

  /**
   * 获取当前歌曲的 k 歌相似的歌曲
   * @param num
   * @param songId
   * @param userId
   * @param simSongs
   * @param mongConfig
   * @return
   */
  def getTopSimSongs(num: Int, songId: Long, userId: Int, simSongs: scala.collection.Map[Long, Map[Long, Double]])(implicit mongConfig: MongConfig): Array[Long] = {
    // 从广播变量的歌曲相似度矩阵中获取当前歌曲所有的相似歌曲
    val allSimSongsOption = simSongs.get(songId)
    val allSimSongs = allSimSongsOption.map(_.toArray).getOrElse(Array.empty[(Long, Double)])

    if (allSimSongs.isEmpty) {
      // 若没有相似歌曲信息，直接返回空数组，不参与后续推荐
      return Array.empty[Long]
    }

    // 获取用户已经喜欢过的歌曲
    val likeExist = ConnHekper.mongoClient(mongConfig.db)(MONGODB_USER_LIKE_COLLECTION).find(MongoDBObject("userId" -> userId)).toArray.map { item =>
      item.get("songId").toString.toLong
    }

    allSimSongs.filter(x => !likeExist.contains(x._1)).sortWith(_._2 > _._2).take(num).map(x => x._1)
  }

  /**
   * 获取基于内容的歌曲推荐
   * @param songId
   * @param contentRecsMatrix
   * @return
   */
  def getContentRecs(songId: Long, contentRecsMatrix: scala.collection.Map[Long, Map[Long, Double]]): Array[(Long, Double)] = {
    contentRecsMatrix.get(songId).map(_.toArray).getOrElse(Array.empty[(Long, Double)])
  }

  /**
   * 获取基于用户的推荐
   * @param userId
   * @param userRecsMatrix
   * @return
   */
  def getUserRecs(userId: Int, userRecsMatrix: scala.collection.Map[Int, Map[Long, Double]]): Array[(Long, Double)] = {
    userRecsMatrix.get(userId).map(_.toArray).getOrElse(Array.empty[(Long, Double)])
  }

  /**
   * 合并三种推荐结果并计算综合得分
   * @param simSongs
   * @param contentRecs
   * @param userRecs
   * @param simSongsMatrix
   * @param contentSongRecsMatrix
   * @param userRecsMatrix
   * @return
   */
  def combineRecs(simSongs: Array[Long], contentRecs: Array[(Long, Double)], userRecs: Array[(Long, Double)],
                  simSongsMatrix: scala.collection.Map[Long, Map[Long, Double]],
                  contentSongRecsMatrix: scala.collection.Map[Long, Map[Long, Double]],
                  userRecsMatrix: scala.collection.Map[Int, Map[Long, Double]]): Array[(Long, Double)] = {

    val contentWeight = 0.4
    val userWeight = 0.3
    val simWeight = 0.3

    val allRecs = scala.collection.mutable.Map[Long, Double]()

    // 处理歌曲相似度推荐
    simSongs.foreach { songId =>
      val score = allRecs.getOrElse(songId, 0.0)
      val simScore = simSongsMatrix.get(songId).flatMap(_.get(songId)).getOrElse(0.0)
      allRecs(songId) = score + simWeight * simScore
    }

    // 处理基于内容的推荐
    contentRecs.foreach { case (songId, score) =>
      val currentScore = allRecs.getOrElse(songId, 0.0)
      allRecs(songId) = currentScore + contentWeight * score
    }

    // 处理基于用户的推荐
    userRecs.foreach { case (songId, score) =>
      val currentScore = allRecs.getOrElse(songId, 0.0)
      allRecs(songId) = currentScore + userWeight * score
    }

    // 归一化处理，确保综合得分不超过1
    val maxScore = if (allRecs.nonEmpty) allRecs.values.max else 1.0
    if (maxScore > 1.0) {
      allRecs.transform((_, score) => score / maxScore)
    }

    // 如果推荐结果为空，返回默认推荐
    if (allRecs.isEmpty) {
      return Array((4916064L, 1.0))  // 返回一个默认推荐
    }

    allRecs.toArray
  }

}
