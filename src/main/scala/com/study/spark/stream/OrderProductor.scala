package com.study.spark.stream

import java.util.HashMap

import com.study.spark.tools.DateFmt
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import scala.util.Random

/**
  * kafka数据生产类
  */
object OrderProductor {
  def main(args: Array[String]) {
//    if (args.length < 4) {
//      System.err.println("Usage: KafkaWordCountProducer <metadataBrokerList> <topic> " +
//        "<messagesPerSec> <wordsPerMessage>")
//      System.exit(1)
//    }
//
//    val Array(brokers, topic, messagesPerSec, wordsPerMessage) = args

    val topic = "orderTopic"
    val brokers = "oda.com:9092,oda.com:9093"

    // Zookeeper连接属性
    val props = new HashMap[String, Object]()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    // 1s 生产10个订单
    while(true) {
      (1 to 10).foreach { messageNum =>
        // 地区id，订单id，订单金额，订单时间
        // 地区id：5之内的随机数
        // 订单金额：100以内的double取整
        // 订单时间类型："yyyy-MM-dd HH:mm:ss"
        val str = Random.nextInt(5)+","+Random.nextInt(10)+","+Math.round(Random.nextDouble()*100)+
          ","+DateFmt.getCountDate(null, DateFmt.date_long)

        val message = new ProducerRecord[String, String](topic, null, str)
//        println(message)
        producer.send(message)
      }

      Thread.sleep(1000)
    }
  }
}
