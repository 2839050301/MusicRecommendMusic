package com.dataloader

// 导入 MongoDB 相关的类，用于操作 MongoDB 数据库
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient

import java.net.InetAddress

// 导入 Spark 相关的类，用于构建 Spark 应用程序
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

/**
 * 歌曲信息的样例类
 * @param sid       歌曲id
 * @param sname     歌曲名
 * @param SingerId  歌手id
 * @param hot       热度
 * @param genre     歌曲风格
 * @param url       图像url
 */
case class Songs(val sid: Long, val sname: String, val SingerId: Long, val hot: Int, val genre: Int, val url: String)

/**
 * 歌手信息的样例类
 * @param Singerid  歌手id
 * @param sname     歌手名
 */
case class Singer(val Singerid: Long, val sname: String)

/**
 * 用户信息的样例类
 * @param userId    用户id
 * @param gender    性别
 * @param username  用户名
 * @param password  用户密码
 */
case class User(val userId: Int, val gender: String, val username: String, val password: String)

/**
 * 用户喜欢歌曲信息的样例类
 * @param userId    用户id
 * @param sid       喜欢歌曲id
 */
case class User_like(val userId: Int, val sid: Long)

/**
 * 歌曲流派信息的样例类
 * @param genre_id   流派id
 * @param genre_name 流派名
 */
case class Genre(val genre_id: Int, val genre_name: String)

/**
 * 歌曲_标签_语言
 * @param song_id    歌曲id
 * @param tag        标签
 * @param language   语言
 */
case class Tag(val song_id: Long, val tag: String, val language: String)

/**
 * MongoDB 配置信息的样例类
 * @param uri MongoDB的连接
 * @param db  MongoDb要操作的数据库
 */
case class MongoConfig(val uri: String, val db: String)

/**
 * Elasticsearch 配置信息的样例类
 * @param httpHost       http主机列表，以,分割
 * @param transportHosts transport主机列表，以，分割
 * @param index          需要操作的索引
 * @param clustername    集群名称，默认
 */
case class ESConfig(val httpHost: String, val transportHosts: String, val index: String, val clustername: String)

object DataLoader {
  // 定义各个 CSV 文件的路径，用于读取数据
  val SONGS_DATA_PATH = "D:\\MusicSystem\\MusicRecommendSystem\\recommender\\dataloader\\src\\main\\resources\\songs.csv"
  val SINGER_DATA_PATH = "D:\\MusicSystem\\MusicRecommendSystem\\recommender\\dataloader\\src\\main\\resources\\singer.csv"
  val USER_DATA_PATH = "D:\\MusicSystem\\MusicRecommendSystem\\recommender\\dataloader\\src\\main\\resources\\user.csv"
  val USER_LIKE_DATA_PATH = "D:\\MusicSystem\\MusicRecommendSystem\\recommender\\dataloader\\src\\main\\resources\\user_like.csv"
  val GENRE_DATA_PATH = "D:\\MusicSystem\\MusicRecommendSystem\\recommender\\dataloader\\src\\main\\resources\\genre.csv"
  val TAG_DATA_PATH = "D:\\MusicSystem\\MusicRecommendSystem\\recommender\\dataloader\\src\\main\\resources\\tag.csv"

  // 定义 MongoDB 中各个集合的名称
  val MONGODB_SONGS_COLLECTION = "Songs"
  val MONGODB_SINGER_COLLECTION = "Singer"
  val MONGODB_USER_COLLECTION = "User"
  val MONGODB_USER_LIKE_COLLECTION = "User_like"
  val MONGODB_GENRE_COLLECTION = "Genre"
  val MONGODB_TAG_COLLECTION = "Tag"
  // 定义 Elasticsearch 中歌曲索引的名称
  val ES_SONGS_INDEX = "Songs"

  // 程序入口
  def main(array: Array[String]): Unit = {
    // 定义配置信息，包括 Spark 核心数、MongoDB 连接信息、Elasticsearch 连接信息等
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.187.131:27017/recommender",
      "mongo.db" -> "recommender",
      "es.httpHosts" -> "192.168.187.131:9200",
      "es.transportHosts" -> "192.168.187.131:9300",
      "es.index" -> "recommder",
      "es.cluster.name" -> "es-cluster"
    )

    // 创建一个 SparkConf 配置，设置 Spark 应用的名称和运行模式
    val sparkConf = new SparkConf().setAppName("Dataloader").setMaster(config.get("spark.cores").get)

    // 创建一个 SparkSession，用于与 Spark 进行交互
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 导入 Spark SQL 的隐式转换，方便后续操作
    import spark.implicits._

    // 从 CSV 文件中加载歌曲数据，生成 RDD
    val songsRDD = spark.sparkContext.textFile(SONGS_DATA_PATH)
    // 将歌曲 RDD 转换为 DataFrame
    val songsDF = songsRDD.flatMap(item => {
      val attr = item.split(",")
      if (attr.length >= 6) {
        Some(Songs(attr(0).toLong, attr(1).trim, attr(2).toLong, attr(3).toInt, attr(4).toInt, attr(5).trim))
      } else {
        None
      }
    }).toDF()

    // 从 CSV 文件中加载歌手数据，生成 RDD
    val singerRDD = spark.sparkContext.textFile(SINGER_DATA_PATH)
    // 将歌手 RDD 转换为 DataFrame
    val singerDF = singerRDD.flatMap(item => {
      val attr = item.split(",")
      if (attr.length >= 2) {
        Some(Singer(attr(0).toLong, attr(1).trim))
      } else {
        None
      }
    }).toDF()

    // 从 CSV 文件中加载用户数据，生成 RDD
    val userRDD = spark.sparkContext.textFile(USER_DATA_PATH)
    // 将用户 RDD 转换为 DataFrame
    val userDF = userRDD.flatMap(item => {
      val attr = item.split(",")
      if (attr.length >= 4) {
        Some(User(attr(0).toInt, attr(1).trim, attr(2).trim, attr(3).trim))
      } else {
        None
      }
    }).toDF()

    // 从 CSV 文件中加载用户喜欢歌曲的数据，生成 RDD
    val user_likeRDD = spark.sparkContext.textFile(USER_LIKE_DATA_PATH)
    // 将用户喜欢歌曲的 RDD 转换为 DataFrame
    val user_likeDF = user_likeRDD.flatMap(item => {
      val attr = item.split(",")
      if (attr.length >= 2) {
        Some(User_like(attr(0).toInt, attr(1).toLong))
      } else {
        None
      }
    }).toDF()

    // 从 CSV 文件中加载歌曲流派数据，生成 RDD
    val genreRDD = spark.sparkContext.textFile(GENRE_DATA_PATH)
    // 将歌曲流派 RDD 转换为 DataFrame
    val genreDF = genreRDD.flatMap(item => {
      val attr = item.split(",")
      if (attr.length >= 2) {
        Some(Genre(attr(0).toInt, attr(1).trim))
      } else {
        None
      }
    }).toDF()

    // 从 CSV 文件中加载歌曲标签和语言数据，生成 RDD
    val tagRDD = spark.sparkContext.textFile(TAG_DATA_PATH)
    // 将歌曲标签和语言 RDD 转换为 DataFrame
    val tagDF = tagRDD.flatMap(item => {
      val attr = item.split(",")
      if (attr.length >= 3) {
        Some(Tag(attr(0).toLong, attr(1).trim, attr(2).trim))
      } else {
        None
      }
    }).toDF()

    // 处理 tag 和 language 数据
    val newTagDF = tagDF.groupBy($"song_id").agg(
      concat_ws("|", collect_list($"tag")).as("tags"),
      concat_ws("|", collect_list($"language")).as("languages")
    )

    // 合并歌曲数据和标签、语言数据
    val songWithTagsDF = songsDF.join(newTagDF, songsDF("sid") === newTagDF("song_id"), "left")

    // 隐式定义 MongoDB 配置信息
    implicit val mongoConfig = MongoConfig(config.get("mongo.uri").get, config.get("mongo.db").get)
    // 将数据保存到 MongoDB 中
    storeDataInMongoDB(songWithTagsDF, singerDF, userDF, user_likeDF, genreDF)

    implicit val esConfig = ESConfig(config.get("es.httpHosts").get, config.get("es.transportHosts").get, config.get("es.index").get, config.get("es.cluster.name").get)
    // 将数据保存到 Elasticsearch 中
    storeDataInES(songWithTagsDF)

    // 关闭 SparkSession，释放资源
    spark.stop()
  }

  // 将数据保存到 MongoDB 中的方法
  def storeDataInMongoDB(songsDF: DataFrame, singerDF: DataFrame, userDF: DataFrame, user_likeDF: DataFrame, genreDF: DataFrame)(implicit mongoConfig: MongoConfig): Unit = {
    // 创建一个 MongoDB 客户端连接
    val mongoClient = MongoClient(MongoClientURI(mongoConfig.uri))

    // 删除 MongoDB 中对应的集合
    mongoClient(mongoConfig.db)(MONGODB_SONGS_COLLECTION).dropCollection()
    mongoClient(mongoConfig.db)(MONGODB_SINGER_COLLECTION).dropCollection()
    mongoClient(mongoConfig.db)(MONGODB_USER_COLLECTION).dropCollection()
    mongoClient(mongoConfig.db)(MONGODB_USER_LIKE_COLLECTION).dropCollection()
    mongoClient(mongoConfig.db)(MONGODB_GENRE_COLLECTION).dropCollection()

    // 将各个 DataFrame 数据写入到 MongoDB 对应的集合中
    songsDF
      .write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_SONGS_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()
    singerDF
      .write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_SINGER_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()
    userDF
      .write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_USER_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()
    user_likeDF
      .write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_USER_LIKE_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()
    genreDF
      .write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_GENRE_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    // 为 MongoDB 中的各个集合创建索引，提高查询性能
    mongoClient(mongoConfig.db)(MONGODB_SONGS_COLLECTION).createIndex(MongoDBObject("sid" -> 1))
    mongoClient(mongoConfig.db)(MONGODB_SINGER_COLLECTION).createIndex(MongoDBObject("Singerid" -> 1))
    mongoClient(mongoConfig.db)(MONGODB_USER_COLLECTION).createIndex(MongoDBObject("userId" -> 1))
    mongoClient(mongoConfig.db)(MONGODB_USER_LIKE_COLLECTION).createIndex(MongoDBObject("userId" -> 1))
    mongoClient(mongoConfig.db)(MONGODB_GENRE_COLLECTION).createIndex(MongoDBObject("genre_id" -> 1))

    // 关闭 MongoDB 客户端连接
    mongoClient.close()
  }

  // 将数据保存到 Elasticsearch 中的方法
  def storeDataInES(songDF: DataFrame)(implicit eSConfig: ESConfig): Unit = {
    // 新建一个配置
    val settings: Settings = Settings.builder().put("cluster.name", eSConfig.clustername).build()

    // 新建一个 ES 的客户端
    val esClient = new PreBuiltTransportClient(settings)

    // 需要将 TransportHosts 添加到 esClient 中
    val REGEX_HOST_PORT = "(.+):(\\d+)".r
    eSConfig.transportHosts.split(",").foreach {
      case REGEX_HOST_PORT(host: String, port: String) => {
        esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port.toInt))
      }
    }
    // 需要清除掉 ES 中遗留的数据
    if (esClient.admin().indices().exists(new IndicesExistsRequest(eSConfig.index)).actionGet().isExists) {
      esClient.admin().indices().delete(new DeleteIndexRequest(eSConfig.index))
    }
    esClient.admin().indices().create(new CreateIndexRequest(eSConfig.index))
    // 将数据写入到 ES 中
    songDF
      .write
      .option("es.nodes", eSConfig.httpHost)
      .option("es.http.timeout", "100m")
      .option("es.mapping.id", "sid")
      .mode("overwrite")
      .format("org.elasticsearch.spark.sql")
      .save(eSConfig.index + "/" + ES_SONGS_INDEX)
  }
}