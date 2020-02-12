package com.study.spark.stream

import com.study.spark.dao.impl.HBaseImpl
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  *
  */
object AreaAmt {

  def main(args: Array[String]) {
    //    if (args.length < 4) {
    //      System.err.println("Usage: KafkaWordCount <zkQuorum> <group> <topics> <numThreads>")
    //      System.exit(1)
    //    }
    //
    //    StreamingExamples.setStreamingLogLevels()
    //    val Array(zkQuorum, group, topics, numThreads) = args

    Logger.getLogger("org").setLevel(Level.WARN)

    val zkQuorum = "192.168.162.121:2181/kfk"
    val zkConn = "192.168.162.121"
    val groupid = "g1"
    val topics = "orderTopic"
    val num = 2

    val dao = new HBaseImpl(zkConn)

    val sparkConf = new SparkConf().setAppName("StatelessWordCount").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(5)) // 5s延迟（不需要特别实时），5s更新一次，往库里写一次
    ssc.checkpoint("hdfs://oda.com:9000/user/spark/sparkproj/checkpoint/areaAmt") //设置有状态的检查点

    val topicMap = topics.split(",").map((_, num.toInt)).toMap
    val orders = KafkaUtils.createStream(ssc, zkQuorum, groupid, topicMap).map(_._2) // _._2是数据
    //    lines.print(3)

    // 产生项目需要的pairRDD
    // 地区id，订单id，订单金额，订单时间
    // 2,6,12,2020-12-12 12:12:12
    val orderRdd = orders.map { row => {
      val arr = row.split(",")
      val key = arr(3).substring(0, 10) + "_" + arr(0) // date_地区id
      val value = arr(2).toInt // 金额
      (key, value)
    }
    }

    val computeFunc = (curValue: Seq[Int], prevValueState: Option[Int]) => {
      // 通过Spark内部的reduceByKey按key规约，然后这里传入某key当前批次的Seq,再计算每个key的总和
      val curCount = curValue.sum
      // 已累加的值
      val prevCount = prevValueState.getOrElse(0)
      // 返回累加后的结果，是一个Option[Int]类型
      Some(curCount + prevCount)
    }

    // 变量Dstream的标准语法
    orderRdd.updateStateByKey[Int](computeFunc).foreachRDD { rdd => // Dstream RDD
      rdd.foreachPartition { partitionOfRecords =>  // 遍历每个分区
        partitionOfRecords.foreach { record =>  // 再遍历其中每个rdd
          println(record._1 + "---" + record._2)  // key(date_地区id), value(金额)
          // 写库
          dao.insert("order", record._1, "cf", "order_amt", record._2 + "")

        }
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }

}
