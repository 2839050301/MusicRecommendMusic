package com.statistics

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession


case class Songs(sid: Long,sname: String,SingerId: Long,hot: Int,genre: Int,url: String)
case class Genre(genre_id: Int,genre_name: String)
case class User_like(userId: Int,sid: Long)

case class MongoConfig(uri: String,db: String)

// 定义一个基准推荐对象
case class Recommendation(sid: Long, score: Double)

// 定义歌曲类别top30推荐对象
case class GenresRecommendation(genre_name: String, recs: Seq[Recommendation])

object StatisticsRecommender {

  private val MONGODB_SONGS_COLLECTION ="Songs"
  private val MONGODB_USER_LIKE_COLLECTION = "User_like"
  private val MONGODB_GENRE_COLLECTION = "Genre"
  // 统计的表的名称
  private val POPULAR_SONGS = "PopularSongs"
  private val GENRES_TOP_SONGS = "GenresTopSongs"

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

    // 创建名为userLikes的临时表
    userLikeDF.createOrReplaceTempView("userLikes")

    // 1. 热门歌曲统计，统计喜欢次数最多的歌曲，sid，count
    val popularSongsDF = spark.sql("select sid, count(sid) as count from userLikes group by sid order by count desc")
    // 把结果写入对应的MongoDB表中
    storeDFInMongoDB(popularSongsDF, POPULAR_SONGS, mongoConfig)

    // 2. 各类别歌曲Top30统计
    // 把喜欢次数加入songs表里，加一列，inner join
    val songsWithLikeCount = songsDF.join(popularSongsDF, "sid")

    // 关联genre表
    val songsWithGenreName = songsWithLikeCount.join(genreDF, songsWithLikeCount("genre") === genreDF("genre_id"), "inner")

    // 找出所有不同的歌曲类别名称
    val allGenres = songsWithGenreName.select("genre_name").distinct().collect().map(_.getString(0)).toList

    // 为做笛卡尔积，把genres转成RDD
    val genresRDD = spark.sparkContext.makeRDD(allGenres)

    // 计算类别top30，首先对类别和歌曲做笛卡尔积
    val genresTopSongsDF = genresRDD.cartesian(songsWithGenreName.rdd)
      .filter {
        // 条件过滤，找出songs的字段genre_name值包含当前类别genre_name的那些
        case (genre_name, songRow) => songRow.getAs[String]("genre_name") == genre_name
      }
      .map {
        case (genre_name, songRow) => (genre_name, (songRow.getAs[Long]("sid"), songRow.getAs[Long]("count").toDouble))
      }
      .groupByKey()
      .map {
        case (genre_name, items) => GenresRecommendation(genre_name, items.toList.sortWith(_._2 > _._2).take(30).map(item => Recommendation(item._1, item._2)))
      }
      .toDF()

    // 把结果写入对应的MongoDB表中
    storeDFInMongoDB(genresTopSongsDF, GENRES_TOP_SONGS, mongoConfig)

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