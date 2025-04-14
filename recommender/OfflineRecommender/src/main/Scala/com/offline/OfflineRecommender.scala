package com.offline

// 导入 Spark 配置相关类
import org.apache.spark.SparkConf
// 导入 ALS 推荐算法和 Rating 类
import org.apache.spark.mllib.recommendation.{ALS, Rating}
// 导入 RDD 类
import org.apache.spark.rdd.RDD
// 导入 SparkSession 类
import org.apache.spark.sql.SparkSession
// 导入广播变量类
import org.apache.spark.broadcast.Broadcast
// 导入 JBlas 库中的 DoubleMatrix 类
import org.jblas.DoubleMatrix

// 导入 Java 集合转换工具
import scala.collection.JavaConversions.asJavaCollection

/**
 * 歌曲信息的样例类
 * @param songId       歌曲的唯一标识符
 * @param songName     歌曲的名称
 * @param singerId     歌手的唯一标识符
 * @param popularity   歌曲的热度
 * @param genre        歌曲的风格
 * @param imageUrl     歌曲相关图像的 URL
 */
case class Songs(songId: Long, songName: String, singerId: Long, popularity: Int, genre: Int, imageUrl: String)

/**
 * 用户喜欢歌曲信息的样例类
 * @param userId    用户的唯一标识符
 * @param songId    用户喜欢的歌曲的唯一标识符
 */
case class Songs_User_like(userId: Int, songId: Long)

// 定义 MongoDB 配置样例类
case class MongoConfig(mongoUri: String, databaseName: String)

// 定义一个基准推荐对象
case class Recommendation(songId: Long, score: Double)

// 定义基于预测评分的用户推荐列表
case class UserRecs(userId: Int, recommendations: Seq[Recommendation])

// 定义基于 LFM 歌曲特征向量的歌曲相似度列表
case class SongRecs(songId: Long, recommendations: Seq[Recommendation])

object OfflineRecommender {
  // 定义 MongoDB 中用户喜欢歌曲集合的名称
  val USER_LIKE_COLLECTION = "User_like"
  // 定义 MongoDB 中歌曲集合的名称
  val SONGS_COLLECTION = "Songs"
  // 定义用户推荐结果集合的名称
  val USER_RECOMMENDATIONS_COLLECTION = "UserRecs"
  // 定义歌曲相似度推荐结果集合的名称
  val SONG_SIMILARITIES_COLLECTION = "SongRecs"
  // 定义每个用户的最大推荐数量
  val MAX_RECOMMENDATIONS_PER_USER = 20

  def main(args: Array[String]): Unit = {
    // 创建一个 Spark 配置，指定运行模式和 MongoDB 连接信息
    val configuration = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.187.131:27017/recommender",
      "mongo.db" -> "recommender"
    )

    // 创建一个 SparkConf 配置，设置应用名称、运行模式和内存参数
    val sparkConfiguration = new SparkConf()
      .setAppName("OfflineRecommender")
      .setMaster(configuration("spark.cores"))
      .set("spark.executor.memory", "4G")
      .set("spark.driver.memory", "3G")

    // 基于 SparkConf 创建一个 SparkSession
    val sparkSession = SparkSession.builder().config(sparkConfiguration).getOrCreate()

    // 创建一个 MongoDB 的配置对象
    val mongoDbConfig = MongoConfig(configuration("mongo.uri"), configuration("mongo.db"))
    // 导入 Spark SQL 的隐式转换
    import sparkSession.implicits._

    // 从 MongoDB 中读取用户喜欢歌曲的数据，转换为 RDD
    val userLikesRDD = sparkSession
      .read
      .option("uri", mongoDbConfig.mongoUri)
      .option("collection", USER_LIKE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .withColumnRenamed("sid", "songId") // 将 sid 重命名为 songId
      .as[Songs_User_like]
      .rdd

    // 生成 songId 到 Int 的映射，方便后续处理
    val songIdToIntMap = userLikesRDD.map(_.songId).distinct().zipWithIndex().collect().toMap
    // 生成 Int 到 songId 的映射
    val intToSongIdMap = songIdToIntMap.map(_.swap)
    // 将 songId 到 Int 的映射广播到各个节点
    val broadcastSongIdToIntMap: Broadcast[Map[Long, Long]] = sparkSession.sparkContext.broadcast(songIdToIntMap)
    // 将 Int 到 songId 的映射广播到各个节点
    val broadcastIntToSongIdMap: Broadcast[Map[Long, Long]] = sparkSession.sparkContext.broadcast(intToSongIdMap)

    // 将 songId 映射为 Int 类型，构建 Rating 对象
    val mappedUserLikesRDD = userLikesRDD.map { like =>
      val mappedSongId = broadcastSongIdToIntMap.value(like.songId).toInt
      Rating(like.userId, mappedSongId, 1.0)
    }.cache()

    // 定义 ALS 模型的参数`
    val rank = 50
    val iterations = 15
    val lambda = 0.01
    // 使用 ALS 算法训练模型
    val alsModel = ALS.train(mappedUserLikesRDD, rank, iterations, lambda)
    // 将模型广播到各个节点
    val broadcastAlsModel: Broadcast[org.apache.spark.mllib.recommendation.MatrixFactorizationModel] = sparkSession.sparkContext.broadcast(alsModel)

    // 构建用户 - 歌曲的全量映射，按用户分组
    val userSongPairs = mappedUserLikesRDD.map(x => (x.user, x.product)).groupByKey()

    // 计算每个用户的推荐列表
    val userRecommendations = userSongPairs.map { case (userId, likedSongIds) =>
      // 获取广播的模型副本
      val modelCopy = broadcastAlsModel.value
      // 为用户推荐歌曲，多推荐一些，确保能过滤掉已喜欢的
      val allRecommendedSongs = modelCopy.recommendProducts(userId, MAX_RECOMMENDATIONS_PER_USER + likedSongIds.size)
      // 过滤掉用户已经喜欢的歌曲
      val filteredRecommendations = allRecommendedSongs.filter { x =>
        val originalSongId = broadcastIntToSongIdMap.value(x.product.toLong)
        !likedSongIds.contains(x.product)
      }.take(MAX_RECOMMENDATIONS_PER_USER)
      // 构建 UserRecs 对象
      UserRecs(userId, filteredRecommendations.map { x =>
        val originalSongId = broadcastIntToSongIdMap.value(x.product.toLong)
        Recommendation(originalSongId, x.rating)
      }.toSeq)
    }.toDF()

    // 提取歌曲的特征向量，并将映射后的歌曲 id 转换回原始 id
    val songFeatureVectors = alsModel.productFeatures.map { case (mappedSongId, features) =>
      val originalSongId = broadcastIntToSongIdMap.value(mappedSongId.toLong)
      (originalSongId, new DoubleMatrix(features))
    }

    // 计算歌曲相似度矩阵
    val songSimilarities = songFeatureVectors.cartesian(songFeatureVectors)
      .filter { case ((songId1, _), (songId2, _)) => songId1 != songId2 }
      .map { case ((songId1, features1), (songId2, features2)) =>
        // 计算余弦相似度
        val similarityScore = cosineSimilarity(features1, features2)
        (songId1, (songId2, similarityScore))
      }
      .filter(_._2._2 > 0.6) // 过滤相似度小于 0.6 的
      .groupByKey()
      .map { case (songId, similarities) =>
        // 对相似度进行排序，取前 MAX_RECOMMENDATIONS_PER_USER 个
        SongRecs(songId, similarities.toList.sortWith(_._2 > _._2).take(MAX_RECOMMENDATIONS_PER_USER).map(x => Recommendation(x._1, x._2)))
      }.toDF().cache()


    // 将用户推荐结果保存到 MongoDB
    userRecommendations.write
      .option("uri", mongoDbConfig.mongoUri)
      .option("collection", USER_RECOMMENDATIONS_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    // 将歌曲相似度推荐结果保存到 MongoDB
    songSimilarities.write
      .option("uri", mongoDbConfig.mongoUri)
      .option("collection", SONG_SIMILARITIES_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    // 销毁广播变量，释放资源
    if (broadcastAlsModel != null) {
      broadcastAlsModel.destroy()
    }
    if (broadcastSongIdToIntMap != null) {
      broadcastSongIdToIntMap.destroy()
    }
    if (broadcastIntToSongIdMap != null) {
      broadcastIntToSongIdMap.destroy()
    }

    // 关闭 SparkSession
    sparkSession.stop()
  }

  // 计算余弦相似度的方法
  def cosineSimilarity(vector1: DoubleMatrix, vector2: DoubleMatrix): Double = {
    vector1.dot(vector2) / (vector1.norm2() * vector2.norm2())
  }
}