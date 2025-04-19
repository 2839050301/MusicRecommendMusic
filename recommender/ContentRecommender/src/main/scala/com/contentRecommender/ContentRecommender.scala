package com.contentRecommender

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.SparkSession
import org.jblas.DoubleMatrix

// 歌曲数据模型
case class Song(songId: Long, sname: String, SingerId: Long, hot: Int, genre: Int, url: String, tags: String, languages: String)

// MongoDB 配置
case class MongoConfig(uri: String, db: String)

// 推荐对象
case class Recommendation(songId: Long, score: Double)

// 歌曲推荐列表
case class SongRecs(songId: Long, recs: Seq[Recommendation])

object ContentRecommender {
  // 定义表名和常量
  val MONGODB_SONG_COLLECTION = "Songs"
  val CONTENT_SONG_RECS = "ContentSongRecs"

  def main(args: Array[String]): Unit = {
    // 配置信息
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.187.131:27017/recommender",
      "mongo.db" -> "recommender"
    )

    // 创建 SparkConf 和 SparkSession
    // 在main方法开始处添加配置
    val sparkConf = new SparkConf()
      .setMaster(config("spark.cores"))
      .setAppName("SongContentRecommender")
      .set("spark.sql.shuffle.partitions", "200")  // 增加shuffle并行度
      .set("spark.default.parallelism", "200")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._
    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 加载数据并预处理
    val songTagsDF = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_SONG_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Song]
      .map { song =>
        // 提取 songId, SingerId, genre, tags 作为特征
        val processedTags = song.tags.map(c => if (c == '|') ' ' else c)
        (song.songId, song.SingerId, song.genre, processedTags)
      }
      .toDF("songId", "SingerId", "genre", "tags")
      .cache()

    // 处理 tags 特征，使用 TF-IDF 提取特征向量
    val tokenizer = new Tokenizer().setInputCol("tags").setOutputCol("words")
    val wordsData = tokenizer.transform(songTagsDF)

    val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(50)
    val featurizedData = hashingTF.transform(wordsData)

    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("tagsFeatures")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)

    // 提取所有特征向量
    // 在特征提取部分增加缓存和分区优化
    val songFeatures = rescaledData.map { row =>
        val songId = row.getAs[Long]("songId")
        val singerId = row.getAs[Long]("SingerId")
        val genre = row.getAs[Int]("genre")
        val tagsVector = row.getAs[SparseVector]("tagsFeatures").toArray
        (songId, singerId, genre, tagsVector)
      }.rdd
      .repartition(100)
      .cache()

    // 计算歌曲相似度
    // 替换原有的笛卡尔积计算
    val songRecs = songFeatures.map { case (songId, singerId, genre, tagsVector) =>
      val featureVector = new DoubleMatrix(tagsVector)
      (songId, (singerId, genre, featureVector))
    }.cache()

    val songRecsBroadcast = spark.sparkContext.broadcast(songRecs.collectAsMap())

    val recommendations = songRecs.mapPartitions { iter =>
        val localData = songRecsBroadcast.value
        iter.flatMap { case (songId, (singerId1, genre1, vector1)) =>
          localData.iterator
            .filter(_._1 != songId)
            .map { case (otherId, (singerId2, genre2, vector2)) =>
              val tagScore = consinSim(vector1, vector2)
              val singerScore = if (singerId1 == singerId2) 1.0 else 0.0
              val genreScore = if (genre1 == genre2) 1.0 else 0.0

              // 设置特征权重
              val singerWeight = 0.2
              val genreWeight = 0.4
              val tagsWeight = 0.4

              // 按权重计算最终得分
              val score = tagScore * tagsWeight + singerScore * singerWeight + genreScore * genreWeight
              (songId, (otherId, score))
            }
            .filter(_._2._2 > 0.6)
            .toSeq
            .sortBy(-_._2._2) // 按得分降序排序
            .take(50) // 取前20个最高分
        }
      }
      .groupByKey()
      .map {
        case (songId, items) =>
          // 显式转换为immutable.Seq
          SongRecs(songId, items.toList.map(x => Recommendation(x._1, x._2)))
      }
      .toDF()

    // 将推荐结果保存到 MongoDB
    recommendations.write // 修改这里，使用正确的变量名
      .option("uri", mongoConfig.uri)
      .option("collection", CONTENT_SONG_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    // 停止 SparkSession
    spark.stop()
  }

  // 求向量余弦相似度
  // 计算余弦相似度，返回0.0到1.0之间的值
  def consinSim(song1: DoubleMatrix, song2: DoubleMatrix): Double = {
    val dotProduct = song1.dot(song2)
    val norm1 = math.sqrt(song1.dot(song1))
    val norm2 = math.sqrt(song2.dot(song2))
    if (norm1 == 0 || norm2 == 0) 0.0 else dotProduct / (norm1 * norm2)
  }
}