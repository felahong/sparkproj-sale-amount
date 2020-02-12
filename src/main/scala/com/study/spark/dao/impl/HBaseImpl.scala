package com.study.spark.dao.impl

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{HTable, Put}
import org.apache.hadoop.hbase.util.Bytes

/**
  * scala版本的HBase操作类
  * @param zkQuorum zookeeper连接
  */
class HBaseImpl(zkQuorum: String) extends Serializable{

  def insert(tableName: String, rowKey: String, family: String, quailifer: String, value: String) {

    val conf = HBaseConfiguration.create()  // Spark直接调用的外部类都需要序列化，所以放在函数里可以减少这样的要求。
    conf.set("hbase.zookeeper.quorum", zkQuorum)
    //Put操作
    val table = new HTable(conf, "order")
    val put = new Put(rowKey.getBytes)
    put.add(Bytes.toBytes(family), Bytes.toBytes(quailifer), Bytes.toBytes(value))
    table.put(put)
    table.flushCommits()
  }


}
