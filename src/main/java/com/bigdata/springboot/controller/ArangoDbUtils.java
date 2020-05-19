package com.bigdata.springboot.controller;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.bigdata.springboot.arangodbcrud.ArangoDbAdapterExt;
import com.bigdata.springboot.config.ExperimentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName ArangoDbUtils
 * @Description
 * @Author yangsen
 * @Date2020/5/11 14:27
 * @Version V1.0
 **/

public class ArangoDbUtils {
        @Autowired
        public static ExperimentConfig experimentConfig;

        public static ArangoDB arangoDB ;

        /***
         * 功能描述:根据租户id,获取相应的Database，实现多租户数据库隔离
         * @param tenantId
         * @return: com.bigdata.springboot.arangodbcrud.ArangoDbAdapterExt
         * @Author: EDZ
         * @Date: 2020/5/8 13:42
         */

        public ArangoDbAdapterExt getArangoDbAdapter(@PathVariable("tenantId") String tenantId) {
                 arangoDB = new ArangoDB.Builder().host(experimentConfig.getHost(), experimentConfig.getPort()).build();
                ArangoDatabase arangoDatabase = arangoDB.db(tenantId);
                //构造ArangoDbAdapterExt，注入arangoDB，arangoDatabase
                return new ArangoDbAdapterExt(arangoDB, arangoDatabase);
        }

        /**
         * 功能描述:组装AQL公共方法（ 共享表方式）
         *
         * @param tenantId
         * @param collectionName
         * @return: java.lang.String
         * @Author: EDZ
         * @Date: 2020/4/27 17:13
         */

        public String getAqlString(@PathVariable("tenantId") String tenantId, String collectionName) {
//                共享表方式实现多租户的方法
                String filterAql = tenantId == null ? "" : " FILTER C.tenantId=='" + tenantId + "'";
                return "FOR C IN " + collectionName + filterAql + "  RETURN C ";

        }

        /**
         * 功能描述:组装AQL公共方法（共享schema方式）
         *
         * @param collectionName
         * @return: java.lang.String
         * @Author: EDZ
         * @Date: 2020/4/27 17:13
         */

        public String getAqlString(String collectionName) {
                return "FOR C IN " + collectionName + "  RETURN C ";
        }

        /***
         * 功能描述:获取document数据仅返回id,name
         * @param aql
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: EDZ
         * @Date: 2020/5/12 11:02
         */
        public Map<String, Object> getDocMaps(String aql, @PathVariable("tenantId") String tenantId) {
                ArangoCursor<BaseDocument> document = null;
                Map<String, Object> result = new HashMap<String, Object>();
                List<HashMap<String, Object>> propertiesList = new ArrayList<HashMap<String, Object>>();
                Map<String, Object> listBody = new HashMap<String, Object>();
                try {
                        ArangoDbAdapterExt arangoDbAdapter = getArangoDbAdapter(tenantId);
                        document = (ArangoCursor<BaseDocument>) arangoDbAdapter.executeQuery(aql);
                        while (document.hasNext()) {
                                BaseDocument object = document.next();
                                String key = object.getKey();
                                String name = object.getAttribute("name") == null ? "" : object.getAttribute("name").toString();
                                HashMap<String, Object> properties = new HashMap<String, Object>();
                                properties.put("id", key);
                                properties.put("name", name);
                                propertiesList.add(properties);
                        }
                        listBody.put("data", propertiesList);
                        listBody.put("tenantId", tenantId);
                        result.put("status", "success");
                        result.put("reason", "success");
                        result.put("LIST_body", listBody);
                } catch (ArangoDBException e) {
                        result.put("status", "failed");
                        result.put("reason", e.getMessage().toString());
                        System.err.println("Failed to get document; " + e.getMessage());
                }

                return result;
        }

        /***
         * 功能描述:获取document数据，全部获取整体返回仅多拼接一个key
         * @param aql
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: EDZ
         * @Date: 2020/5/12 11:02
         */
        public Map<String, Object> getDocMapsProps(String aql, @PathVariable("tenantId") String tenantId) {
                ArangoCursor<BaseDocument> document = null;
                Map<String, Object> result = new HashMap<String, Object>();
                List<Map<String, Object>> propertiesList = new ArrayList<Map<String, Object>>();
                Map<String, Object> listBody = new HashMap<String, Object>();
                try {
                        ArangoDbAdapterExt arangoDbAdapter = getArangoDbAdapter(tenantId);
                        document = (ArangoCursor<BaseDocument>) arangoDbAdapter.executeQuery(aql);
                        while (document.hasNext()) {
                                BaseDocument object = document.next();
                                String key = object.getKey();
                                Map<String, Object> properties = object.getProperties();
                                properties.put("id", key);
                                propertiesList.add(properties);
                        }
                        listBody.put("data", propertiesList);
                        listBody.put("tenantId", tenantId);
                        result.put("status", "success");
                        result.put("reason", "success");
                        result.put("LIST_body", listBody);
                } catch (ArangoDBException e) {
                        result.put("status", "failed");
                        result.put("reason", e.getMessage().toString());
                        System.err.println("Failed to get document; " + e.getMessage());
                }

                return result;
        }
        /***
         * 功能描述:获取document数据，全部获取整体返回仅多拼接一个key
         * @param aql
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: EDZ
         * @Date: 2020/5/12 11:02
         */
        public  List<Map<String, Object>> getDocMapsLists(String aql, @PathVariable("tenantId") String tenantId) {
                ArangoCursor<BaseDocument> document = null;

                List<Map<String, Object>> propertiesList = new ArrayList<Map<String, Object>>();
                Map<String, Object> listBody = new HashMap<String, Object>();
                try {
                        ArangoDbAdapterExt arangoDbAdapter = getArangoDbAdapter(tenantId);
                        document = (ArangoCursor<BaseDocument>) arangoDbAdapter.executeQuery(aql);
                        while (document.hasNext()) {
                                BaseDocument object = document.next();
                                String key = object.getKey();
                                Map<String, Object> properties = object.getProperties();
                                properties.put("id", key);
                                propertiesList.add(properties);
                        }

                } catch (ArangoDBException e) {

                        System.err.println("Failed to get document; " + e.getMessage());
                        throw new ArangoDBException("Failed to get document; " + e.getMessage());
                }

                return propertiesList;
        }
        /**
         * 转换指定日期--时分秒
         *
         * @author yangsen
         */
        public String getTimeSec(Date d) {
                System.out.println("获取时间" + new Date());


                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                System.out.println("格式化输出：" + sdf.format(d));

                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                System.out.println("Asia/Shanghai:" + sdf.format(d));

                return sdf.format(d);
        }

        /**
         * 获取指定日期前后num天的日期
         *
         * @param date
         * @param num  负数多少天之后的日期   正数 多少天之后的日期
         * @return
         * @author yangsen
         */
        public static Date getDay(Date date, int num) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - num);
                return calendar.getTime();
        }

        /**
         * 转换指定日期
         *
         * @author yangsen
         */
        public String getTime(Date d) {
                System.out.println("获取时间" + new Date());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                System.out.println("格式化输出：" + sdf.format(d));

                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                System.out.println("Asia/Shanghai:" + sdf.format(d));

                return sdf.format(d);
        }
}
