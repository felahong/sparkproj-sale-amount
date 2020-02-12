package com.study.spark.servlet;

import com.study.spark.dao.HBaseDAO;
import com.study.spark.dao.imp.HBaseDAOImp;
import com.study.spark.tools.DateFmt;
import com.study.spark.vo.AreaVo;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.util.List;

/**
 *
 */
//@WebServlet("/amt")
//@WebServlet(name="AreaAmtServlet", urlPatterns = {"/AreaAmtServlet"})
public class AreaAmtServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    HBaseDAO dao = null;

    String today = null;
    String hisDay = null;
    String hisData = null;

    public void init(){
        // 启动时，执行一次
        dao = new HBaseDAOImp();
        // servlet构建的日期，即tomcat启动日期
        today = DateFmt.getCountDate(null, DateFmt.date_short);   // yyyy-MM-dd
    }

    // doGet直接掉doPost
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        try {
            this.doPost(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 所有实现基本都在doPost里
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        int n = -7;  // 取前n的数据
        hisDay = DateFmt.getCountDate(null, DateFmt.date_short, n);    // getCountDate设置了对日期的操作，如-1表示上一天
//		System.out.println("hisDay: "+hisDay);
        hisData = this.getData(hisDay, dao);  // 查n天前的数据

        System.out.println("hisData：" + hisData);
        while (true) {
            // 长连接。servlet里的长链接实现方式
            String dateStr = DateFmt.getCountDate(null, DateFmt.date_short);
            if (!dateStr.equals(today)) {
                // 跨天处理。当跨天了，清空数据，再取新一天的数据
                today = dateStr;
            }
            // 每个5s查询一次hbase
            String data = this.getData(today, dao); // 取当天的数据
            System.out.println("data：" + data);
            // 当天数据和昨天数据拼成json字符串。todayData:123,hisData:456
            String jsDataString = "{\'todayData\':" + data + ",\'hisData\':" + hisData + "}";

            boolean flag = this.sentData("jsFun", response, jsDataString);  // 发给前端，前端通过jsFun接收
            // 死循环（如果tomcat不重启，就一直跑着），所以需要有断开长连接的方式（比如界面上刷新，退出的时候）。if (!flag)就是做这个判断
            if (!flag) {
                break;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送数据。把数据推给前台的jsp，同时调用jsp里的一个函数jsFun
     * 每次数据推到前端，执行jsFun函数，这时候才会刷新图表里的图形--》打通前后端
     * @param jsFun JavaScript函数
     * @param response
     * @param data
     * @return
     */
    public boolean sentData(String jsFun, HttpServletResponse response, String data) {
        try {
            // 通过response发到jsp里
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write("<script type=\"text/javascript\">parent." + jsFun + "(\"" + data + "\")</script>");
            response.flushBuffer();
            return true;
        } catch (Exception e) {
            System.out.println(" long connect 已断开 ");
            return false;
        }
    }

    /**
     * 读库
     * @param date
     * @param dao
     * @return
     */
    public String getData(String date, HBaseDAO dao) {
        List<Result> list = dao.getRows("order", date); // 通过表名和rowkey（这里是日期）查询（模糊匹配）
        AreaVo vo = new AreaVo();
        for (Result rs : list) {    // 循环遍历每一行，存到
            String rowKey = new String(rs.getRow());    // 拿到一行
            String aredid = null;
            if (rowKey.split("_").length == 2) {
                aredid = rowKey.split("_")[1];  // 以下划线分割，获取地区id
            }
            for (KeyValue keyValue : rs.raw()) {
                if ("order_amt".equals(new String(keyValue.getQualifier()))) {  // 如果字段是"order_amt"，
                    vo.setData(aredid, new String(keyValue.getValue()));        // 则获取它的值，存到vo对象里
                    break;
                }
            }
        }
        // HighCharts需要json类的数据格式，所以加了[]
        // 取数据时，和前端要对应上：['北京','上海','广州','深圳','成都']
        String result = "[" + getFmtPoint(vo.getBeijing()) + "," + getFmtPoint(vo.getShanghai()) + "," + getFmtPoint(vo.getGuangzhou()) + "," + getFmtPoint(vo.getShenzhen()) + "," + getFmtPoint(vo.getChengdu()) + "]";
        System.out.println("result: " + result);
        return result;

    }

    // 格式处理，小数只取整数部分
    public String getFmtPoint(String str) {
        DecimalFormat format = new DecimalFormat("#");  // 取所有整数部分。参考：https://www.cnblogs.com/Small-sunshine/p/11648652.html
        if (str != null) {
            return format.format(Double.parseDouble(str));
        }
        return null;
    }

//    public static void main(String[] args) {
//        AreaAmtServlet areaAmtServlet = new AreaAmtServlet();
//        System.out.println(areaAmtServlet.getFmtPoint("12.12"));
//
//    }

}
