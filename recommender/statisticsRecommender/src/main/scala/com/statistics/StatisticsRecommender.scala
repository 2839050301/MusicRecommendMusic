package com.statistics

import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.functions.{collect_list, struct}

// 修改为使用 songId
case class Songs(songId: Long, sname: String, SingerId: Long, hot: Int, genre: Int, url: String)
case class User_like(userId: Int, songId: Long)
// 定义一个基准推荐对象，使用 songId
case class Recommendation(songId: Long, score: Double)

// 定义性别top30推荐对象
case class GenderRecommendation(gender: String, recs: Seq[Recommendation])

// 定义流派top30推荐对象
case class GenreRecommendation(genre_id: Int, recs: Seq[Recommendation])

case class Genre(genre_id: Int, genre_name: String)
case class User(userId: Int, gender: String, username: String, password: String)

case class MongoConfig(uri: String, db: String)

object StatisticsRecommender {

  private val MONGODB_SONGS_COLLECTION = "Songs"
  private val MONGODB_USER_LIKE_COLLECTION = "User_like"
  private val MONGODB_GENRE_COLLECTION = "Genre"
  private val MONGODB_USER_COLLECTION = "User"
  // 统计的表的名称
  private val GENDER_TOP_SONGS = "GenderTopSongs"
  private val GENRE_TOP_SONGS = "GenreTopSongs"
  private val POPULAR_SONGS = "PopularSongs"

  def main(args: Array[String]): Unit = {

    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.187.131:27017/recommender",
      "mongo.db" -> "recommender"
    )

    val sparkConf = new SparkConf()
      .setAppName("StatisticsRecommender")
      .setMaster(config("spark.cores"))

    val spark = SparkSession.builder()
      .config(sparkConf)
      .getOrCreate()

    val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))
    import spark.implicits._

    // 加载数据
    val songsDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_SONGS_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Songs]
      .toDF()

    val userLikeDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_USER_LIKE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[User_like]
      .toDF()

    val genreDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_GENRE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Genre]
      .toDF()

    val userDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_USER_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[User]
      .toDF()

    // 关联 User 表和 User_like 表
    val userLikeWithGenderDF = userLikeDF.join(userDF, "userId")

    // 创建名为 userLikesWithGender 的临时表
    userLikeWithGenderDF.createOrReplaceTempView("userLikesWithGender")

    // 分别统计男女最喜欢的 30 首歌
    val genderTopSongsDF = spark.sql(
        """
          |SELECT gender, songId, count(songId) as count
          |FROM userLikesWithGender
          |GROUP BY gender, songId
          |ORDER BY gender, count DESC
      """.stripMargin)
      .groupBy("gender")
      .agg(collect_list(struct($"songId", $"count".cast("double"))).as("recs"))
      .map { row =>
        val gender = row.getString(0)
        val recs = row.getSeq[Row](1).map { r =>
          Recommendation(r.getLong(0), r.getDouble(1))
        }.sortWith(_.score > _.score).take(30)
        GenderRecommendation(gender, recs)
      }
      .toDF()

    // 把结果写入对应的 MongoDB 表中
    storeDFInMongoDB(genderTopSongsDF, GENDER_TOP_SONGS, mongoConfig)

    // 关联 Songs 表和 User_like 表
    val songsWithLikesDF = songsDF.join(userLikeDF, "songId")

    // 创建名为 songsWithLikes 的临时表
    songsWithLikesDF.createOrReplaceTempView("songsWithLikes")

    // 统计各个流派喜欢的 top30 歌曲
    val genreTopSongsDF = spark.sql(
        """
          |SELECT genre, songId, count(songId) as count
          |FROM songsWithLikes
          |GROUP BY genre, songId
          |ORDER BY genre, count DESC
      """.stripMargin)
      .groupBy("genre")
      .agg(collect_list(struct($"songId", $"count".cast("double")).alias("rec")).as("recs"))
      .map { row =>
        val genre_id = row.getInt(0)
        val recs = row.getSeq[Row](1).take(30).map { r =>
          Recommendation(r.getLong(0), r.getDouble(1))
        }
        GenreRecommendation(genre_id, recs)
      }
      .toDF()

    // 把结果写入对应的 MongoDB 表中
    storeDFInMongoDB(genreTopSongsDF, GENRE_TOP_SONGS, mongoConfig)

    // 统计各个歌曲喜欢的总数 top30 降序排列
    val popularSongsDF = spark.sql(
        """
          |SELECT songId, count(songId) as count
          |FROM userLikeDF
          |GROUP BY songId
          |ORDER BY count DESC
          |LIMIT 30
      """.stripMargin)
      .map { row =>
        Recommendation(row.getLong(0), row.getDouble(1))
      }
      .toDF()
      .sort($"score".desc)

    // 把结果写入对应的 MongoDB 表中
    storeDFInMongoDB(popularSongsDF, POPULAR_SONGS, mongoConfig)

    spark.stop()
  }

  private def storeDFInMongoDB(df: org.apache.spark.sql.DataFrame, collection_name: String, mongoConfig: MongoConfig): Unit = {
    df.write
      .option("uri", mongoConfig.uri)
      .option("collection", collection_name)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()
  }
}