package com.offline

import org.apache.spark.SparkConf
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

// 定义一个简单的样例类来替代 (Int, Long) 元组
case class UserSong(userId: Int, songId: Long)

object ALSTrians {
  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://192.168.187.131:27017/recommender",
      "mongo.db" -> "recommender"
    )

    // 创建 SparkConf
    val sparkConf = new SparkConf().setAppName("ALSTrians").setMaster(config("spark.cores"))

    // 创建 SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 导入 Spark SQL 的隐式转换，包含编码器
    import spark.implicits._

    // 从 MongoDB 中读取用户喜欢歌曲的数据，转换为 DataFrame
    val userLikesDF = spark
      .read
      .option("uri", config("mongo.uri"))
      .option("collection", OfflineRecommender.USER_LIKE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .withColumnRenamed("sid", "songId")
      .as[UserSong]

    // 将 DataFrame 转换为 RDD[Rating]
    val userLikesRDD: RDD[Rating] = userLikesDF.rdd.map { userSong =>
      Rating(userSong.userId, userSong.songId.toInt, 1.0)
    }

    // 划分训练集和测试集
    val Array(training, test) = userLikesRDD.randomSplit(Array(0.8, 0.2))

    // 定义参数网格
    val ranks = Array(30,50,100)
    val iterations = Array(10, 15, 20)
    val lambdas = Array(1,0.1,0.01,0.001)

    var bestRMSE = Double.MaxValue
    var bestRank = 0
    var bestIter = 0
    var bestLambda = 0.0

    for (rank <- ranks; iter <- iterations; lambda <- lambdas) {
      // 训练模型
      val model = ALS.train(training, rank, iter, lambda)

      // 进行预测
      val userProducts = test.map { case Rating(user, product, _) => (user, product) }
      val predictions = model.predict(userProducts).map { case Rating(user, product, rate) => ((user, product), rate) }
      val ratesAndPreds = test.map { case Rating(user, product, rate) => ((user, product), rate) }.join(predictions)

      // 计算 RMSE 均方根误差
      val rmse = new RegressionMetrics(ratesAndPreds.map(_._2)).rootMeanSquaredError

      if (rmse < bestRMSE) {
        bestRMSE = rmse
        bestRank = rank
        bestIter = iter
        bestLambda = lambda
      }
    }

    // 输出最优参数
    println(s"Best rank: $bestRank")
    println(s"Best iterations: $bestIter")
    println(s"Best lambda: $bestLambda")
    println(s"Best RMSE: $bestRMSE")

    // 关闭 SparkSession
    spark.stop()
  }
}