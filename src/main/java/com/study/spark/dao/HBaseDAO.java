package com.study.spark.dao;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;

import java.util.List;

/**
 * 数据访问接口
 */
public interface HBaseDAO {

    // 表名，rowkey，列簇，字段名，字段value
    // 插入一个字段
    public void insert(String tableName, String rowKey, String family, String quailifer, String value);

    // 插入多个字段
    public void insert(String tableName, String rowKey, String family, String quailifer[], String value[]);

    // put，添加数据
    public void save(Put put, String tableName);

    // 多个put
    public void save(List<Put> Put, String tableName);

    // 通过rowkey查询一行
    public Result getOneRow(String tableName, String rowKey);

    // 通过rowkey查询多行
    public List<Result> getRows(String tableName, String rowKey_like);

    // 通过rowkey加字段查询多行
    public List<Result> getRows(String tableName, String rowKeyLike, String cols[]);

    public List<Result> getRows(String tableName, String startRow, String stopRow);

    public void deleteRecords(String tableName, String rowKeyLike);
}
