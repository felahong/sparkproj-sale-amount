package com.study.spark.dao.imp;

import com.study.spark.dao.HBaseDAO;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PrefixFilter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * HBaseDao接口的实现类
 */
public class HBaseDAOImp implements HBaseDAO, Serializable {

    HConnection hTablePool = null;

    public HBaseDAOImp() {
        Configuration conf = new Configuration();
        String zk_list = "oda.com:2181";
        conf.set("hbase.zookeeper.quorum", zk_list);    // 给定zk的相关环境变量
        try {
            hTablePool = HConnectionManager.createConnection(conf);   // connetion
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行单个put
     * @param put Put实例
     * @param tableName
     */
    @Override
    public void save(Put put, String tableName) {
        // TODO Auto-generated method stub
        HTableInterface table = null;
        try {
            table = hTablePool.getTable(tableName); // 使用连接线程池获取表
            table.put(put);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行多个put
     * @param Put
     * @param tableName
     */
    @Override
    public void save(List<Put> Put, String tableName) {
        // TODO Auto-generated method stub
        HTableInterface table = null;
        try {
            table = hTablePool.getTable(tableName);
            table.put(Put);
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 单个字段插入。用户给定表名、rowkey(表示唯一一行)、列簇、字段名、插入的value，由程序创建put对象并执行put操作
     * @param tableName
     * @param rowKey
     * @param family
     * @param quailifer
     * @param value
     */
    @Override
    public void insert(String tableName, String rowKey, String family,
                       String quailifer, String value) {
        // TODO Auto-generated method stub
        HTableInterface table = null;
        try {
            table = hTablePool.getTable(tableName);
            Put put = new Put(rowKey.getBytes());
            put.add(family.getBytes(), quailifer.getBytes(), value.getBytes());
            table.put(put);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 多字段插入
     * @param tableName
     * @param rowKey
     * @param family
     * @param quailifer
     * @param value
     */
    @Override
    public void insert(String tableName, String rowKey, String family, String quailifer[], String value[]) {
        HTableInterface table = null;
        try {
            table = hTablePool.getTable(tableName);
            Put put = new Put(rowKey.getBytes());
            // 批量添加
            for (int i = 0; i < quailifer.length; i++) {
                String col = quailifer[i];
                String val = value[i];
                put.add(family.getBytes(), col.getBytes(), val.getBytes());
            }
            table.put(put);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取对应表中对应rowkey的数据
     * @param tableName
     * @param rowKey
     * @return
     */
    @Override
    public Result getOneRow(String tableName, String rowKey) {
        // TODO Auto-generated method stub
        HTableInterface table = null;
        Result rsResult = null;
        try {
            table = hTablePool.getTable(tableName);
            Get get = new Get(rowKey.getBytes());
            rsResult = table.get(get);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rsResult;
    }

    /**
     * 获取对应表中经过rowkeyLike过滤的数据（模糊匹配）
     * rowkey的模糊匹配是左匹配，aaa% ,不支持 %aaa% 和 %aaa
     * @param tableName
     * @param rowKeyLike
     * @return
     */
    @Override
    public List<Result> getRows(String tableName, String rowKeyLike) {
        // TODO Auto-generated method stub
        HTableInterface table = null;
        List<Result> list = null;
        try {
            table = hTablePool.getTable(tableName);
            PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());  // 使用前缀filter
            Scan scan = new Scan();
            scan.setFilter(filter);     // scan filter
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<Result>();
            for (Result rs : scanner) {
                list.add(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 这个方法很常用
     * 给定表名，rowkey，字段名
     * @param tableName
     * @param rowKeyLike
     * @param cols
     * @return
     */
    @Override
    public List<Result> getRows(String tableName, String rowKeyLike, String cols[]) {
        // TODO Auto-generated method stub
        HTableInterface table = null;
        List<Result> list = null;
        try {
            table = hTablePool.getTable(tableName);
            PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());
            Scan scan = new Scan();
            for (int i = 0; i < cols.length; i++) {
                scan.addColumn("cf".getBytes(), cols[i].getBytes());    // cf是列簇
            }
            scan.setFilter(filter);
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<Result>();
            for (Result rs : scanner) {
                list.add(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 范围查询，给定开始rowkey以及结束rowkey
     * @param tableName
     * @param startRow
     * @param stopRow
     * @return
     */
    @Override
    public List<Result> getRows(String tableName, String startRow, String stopRow) {
        HTableInterface table = null;
        List<Result> list = null;
        try {
            table = hTablePool.getTable(tableName);
            Scan scan = new Scan();
            scan.setStartRow(startRow.getBytes());
            scan.setStopRow(stopRow.getBytes());
            ResultScanner scanner = table.getScanner(scan);
            list = new ArrayList<Result>();
            for (Result rsResult : scanner) {
                list.add(rsResult);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    /**
     * 给定一个或多个rowkey，进行删除操作
     * @param tableName
     * @param rowKeyLike
     */
    @Override
    public void deleteRecords(String tableName, String rowKeyLike) {
        HTableInterface table = null;
        try {
            table = hTablePool.getTable(tableName);
            PrefixFilter filter = new PrefixFilter(rowKeyLike.getBytes());
            Scan scan = new Scan();
            scan.setFilter(filter);
            ResultScanner scanner = table.getScanner(scan);
            List<Delete> list = new ArrayList<Delete>();
            for (Result rs : scanner) {
                Delete del = new Delete(rs.getRow());
                list.add(del);
            }
            table.delete(list);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                table.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        HBaseDAO dao = new HBaseDAOImp();
//		List<Put> list = new ArrayList<Put>();
//		Put put = new Put("r1".getBytes());
//		put.add("cf".getBytes(), "name".getBytes(), "whisky".getBytes());
//		list.add(put) ;
//		dao.save(put, "test") ;
//
//		dao.insert("test9", "r1", "cf", "age", "30") ;
//		dao.insert("test", "testrow", "cf", "cardid", "12312312335") ;
//		dao.insert("test", "testrow", "cf", "tel", "13512312345") ;
//        List<Result> list2 = dao.getRows("order", "2016-09-11");
        List<Result> list2 = dao.getRows("order", "2020-02-11", new String[]{"order_amt"});
        for (Result rs : list2) {
            for (KeyValue keyValue : rs.raw()) {
                System.out.println("rowkey:" + new String(keyValue.getRow()));
                System.out.println("Qualifier:" + new String(keyValue.getQualifier()));
                System.out.println("Value:" + new String(keyValue.getValue()));
                System.out.println("----------------");
            }
        }
    }

}
