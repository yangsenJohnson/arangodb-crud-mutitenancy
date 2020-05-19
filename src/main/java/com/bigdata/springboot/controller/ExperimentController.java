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
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * ExperimentController
 *
 * @Author: yangsen
 * @Date: 2020/4/20 11:43
 */
@RestController
@RequestMapping("/api1/{tenantId}")
public class ExperimentController {

        @Autowired
        public ExperimentConfig experimentConfig;

//        //配置文件获取host
//        private ArangoDB arangoDB = new ArangoDB.Builder().host(experimentConfig.getHost(), experimentConfig.getPort()).build();
        private ArangoDbUtils arangoDbUtils=new ArangoDbUtils();
        @Autowired
        private RestTemplate restTemplate;
//        @Value("${param.port}")
//        int port;
//        @Value("${param.host}")
//        String host;
        @Value("${param.jupyterUrl}")
        String jupyterUrl;
        @Value("${param.infradbName}")
        String infradbName;

        /**
         * 功能描述:Computer Cluster 名称列表查询
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:44
         */
        @GetMapping("/computer/queryComputers")
        public Map<String, Object> queryComputers(@PathVariable(value = "tenantId") String tenantId) throws SQLException {
                tenantId=infradbName;
                //加入多租户筛选
                String aql = getAqlString( experimentConfig.getComputerClusterName());

                return arangoDbUtils.getDocMaps(aql, tenantId);
        }


        /**
         * 功能描述:DataSource 列表查询，只但会name和id
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:43
         */
        @GetMapping("/dataApplicance/listDataSource")
        public Map<String, Object> listDataSource(@PathVariable(value = "tenantId") String tenantId) throws SQLException {

                //加入多租户筛选
                String aql = getAqlString( experimentConfig.getDsCollectionName());

                return arangoDbUtils.getDocMaps(aql, tenantId);
        }
        /**
         * 功能描述:DataSource 列表查询:查询出DataSource所有字段
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:44
         */
        @GetMapping("/dataApplicance/queryDataSourceList")
        public Map<String, Object> queryDataSourceList(@PathVariable(value = "tenantId") String tenantId) throws SQLException {

                //加入多租户筛选
                String aql = getAqlString(experimentConfig.getDsCollectionName());

                return arangoDbUtils.getDocMapsProps(aql, tenantId);
        }

        /**
         * 功能描述:查看notebook详细信息
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:44
         */
        @GetMapping("/notebook/viewNotebook")
        public Map<String, Object> viewNotebook(@PathVariable(value = "tenantId") String tenantId) throws SQLException {

                //加入多租户筛选
                String aql = getAqlString(experimentConfig.getNoteBookCllectionName());

                return arangoDbUtils.getDocMaps(aql, tenantId);
        }

        /**
         * 功能描述:保存experiment
         *
         * @param myObject
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:45
         */
        @RequestMapping(value = "/experiment/saveExperiment", method = RequestMethod.POST, consumes = "application/json")
        public Map<String, Object> saveExperiment(@RequestBody BaseDocument myObject, @PathVariable(value = "tenantId") String tenantId) {
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {
                        Date d = new Date();
                        //写入当前时间，格式"yyyy-MM-dd kk:mm:ss"
                        myObject.addAttribute("createOn", getTimeSec(d));
                        //加入多租户tenantId
                        myObject.addAttribute("tenantId", tenantId);
                        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
                        myObject.addAttribute("dr", 0);
                        //Start 2020年5月19日 从infria查询出computeClusters然后保存进去
                        String aql = getAqlString(experimentConfig.getComputerClusterName());
                        myObject.addAttribute("clusters",  arangoDbUtils.getDocMapsLists(aql, infradbName)==null?"":arangoDbUtils.getDocMapsLists(aql, infradbName).toArray());
                        //end 2020年5月19日 从infria查询出computeClusters然后保存进去

                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);
                        arangoDbAdapter.insertDocument(experimentConfig.getCollectionName(), myObject);

                        System.out.println("Document created:" + myObject.getKey() + myObject.getProperties());
                        msg = "Document created key:" + myObject.getKey() + " properties:" + myObject.getProperties();
                        result.put("status", "success");
                        result.put("reason", msg);
                } catch (ArangoDBException e) {
                        System.err.println("Failed to create document. " + e.getMessage());
                        msg = "Failed to create document. " + e.getMessage();
                        result.put("status", "failed");
                        result.put("reason", msg);
                }
                return result;
        }


        /**
         * 功能描述:根据时间筛选 Experiment列表
         *
         * @param lastDays
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:46
         */
        @GetMapping("/experiment/queryExperimentsByDate/{lastDays}")
        public Map<String, Object> queryExperimentsByDate(@PathVariable(value = "lastDays") int lastDays, @PathVariable(value = "tenantId") String tenantId) throws SQLException {
                ArangoCursor<BaseDocument> document = null;
                Map<String, Object> result = new HashMap<String, Object>();
                List<Map<String, Object>> propertiesList = new ArrayList<Map<String, Object>>();
                String str = null;
                Date d = new Date();
                //转换格式+计算提前的日期
                String datatimeBefore = getTime(getDay(d, lastDays));
                String datatimeNow = getTime(getDay(d, -1));
                //加入多租户筛选
                String filterAql = tenantId == null ? "" : " C.tenantId=='" + tenantId + "'&&";
                String aql = "FOR C IN " + experimentConfig.getCollectionName() + " FILTER " + filterAql + "C.createOn!=null&&C.createOn>='" + datatimeBefore + "'&&C.createOn<='" + datatimeNow + "' RETURN C ";
                Map<String, Object> listBody = new HashMap<String, Object>();
                try {
                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);
                        document = (ArangoCursor<BaseDocument>) arangoDbAdapter.executeQuery(aql);
                        //ArangoCursor需遍历
                        while (document.hasNext()) {
                                BaseDocument object = document.next();
                                String key = object.getKey();
                                //暂时展示所有字段，供前端选择使用
//                                String dataSource = object.getAttribute("dataSource") == null ? "" : object.getAttribute("dataSource").toString();
//                                String name = object.getAttribute("experimentName") == null ? "" : object.getAttribute("experimentName").toString();
                                Map<String, Object> properties = object.getProperties();
                                properties.put("id", key);
                                propertiesList.add(properties);
                        }
                        listBody.put("data|" + String.valueOf(lastDays), propertiesList);
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

        /**
         * 功能描述:保存Compute Cluster
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:46
         */
        @RequestMapping(value = "/computer/saveComputers", method = RequestMethod.POST, consumes = "application/json")
        public Map<String, Object> saveComputers(@RequestBody BaseDocument myObject, @PathVariable(value = "tenantId") String tenantId) {
                //Cluster在mle-infra进行CRUD
                tenantId=infradbName;
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {
                        Date d = new Date();
                        //写入当前时间，格式"yyyy-MM-dd kk:mm:ss"
                        myObject.addAttribute("createOn", getTimeSec(d));
                        //加入多租户tenantId
                        myObject.addAttribute("tenantId", tenantId);
                        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
                        myObject.addAttribute("dr", 0);
                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);
                        arangoDbAdapter.insertDocument(experimentConfig.getComputerClusterName(), myObject);

                        System.out.println("Document created:" + myObject.getKey() + myObject.getProperties());
                        msg = "Document created key:" + myObject.getKey() + " properties:" + myObject.getProperties();
                        result.put("status", "success");
                        result.put("reason", msg);
                } catch (ArangoDBException e) {
                        System.err.println("Failed to create document. " + e.getMessage());
                        msg = "Failed to create document. " + e.getMessage();
                        result.put("status", "failed");
                        result.put("reason", msg);
                }
                return result;
        }

        /**
         * 功能描述:更新Compute Cluster
         * 目前使用PUT，后面看需求是否改为PATCH
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:46
         */
        @RequestMapping(value = "/computer/editComputer/{key}", method = RequestMethod.PUT, consumes = "application/json")
        public Map<String, Object> editComputer(@RequestBody BaseDocument myObject, @PathVariable(value = "key") String key, @PathVariable(value = "tenantId") String tenantId) {
                //Cluster在mle-infra进行CRUD
                tenantId=infradbName;
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {
                        Date d = new Date();
                        //写入当前时间，格式"yyyy-MM-dd kk:mm:ss"
                        myObject.addAttribute("updateOn", getTimeSec(d));

                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);

                        arangoDbAdapter.updateDocument(experimentConfig.getComputerClusterName(), key, myObject);

                        System.out.println("Document updated:" + key + myObject.getProperties());
                        msg = "Document updated key :" + key + " properties:" + myObject.getProperties();
                        result.put("status", "success");
                        result.put("reason", msg);
                } catch (ArangoDBException e) {
                        System.err.println("Failed to create document. " + e.getMessage());
                        msg = "Failed to create document. " + e.getMessage();
                        result.put("status", "failed");
                        result.put("reason", msg);
                }
                return result;
        }

        /**
         * 功能描述:删除Compute Cluster档案
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:46
         */
        @RequestMapping(value = "/computer/deleteComputer/{key}", method = RequestMethod.DELETE, consumes = "application/json")
        public Map<String, Object> deleteComputer(@PathVariable(value = "key") String key, @PathVariable(value = "tenantId") String tenantId) {
                //Cluster在mle-infra进行CRUD
                tenantId=infradbName;
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {
                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);

                        arangoDbAdapter.deleteDocument(experimentConfig.getComputerClusterName(), key);

                        System.out.println("Document deleted:" + key);
                        msg = "Document  deleted:" + key;
                        result.put("status", "success");
                        result.put("reason", msg);
                } catch (ArangoDBException e) {
                        System.err.println("Failed to deleted document. " + e.getMessage());
                        msg = "Failed to deleted document. " + e.getMessage();
                        result.put("status", "failed");
                        result.put("reason", msg);
                }
                return result;
        }

        /**
         * 功能描述:保存dataSource
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:46
         */
        @RequestMapping(value = "/dataApplicance/saveDataSource", method = RequestMethod.POST, consumes = "application/json")
        public Map<String, Object> createNewDataSource(@RequestBody BaseDocument myObject, @PathVariable(value = "tenantId") String tenantId) {
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {
                        Date d = new Date();
                        //写入当前时间，格式"yyyy-MM-dd kk:mm:ss"
                        myObject.addAttribute("createOn", getTimeSec(d));
                        //加入多租户tenantId
                        myObject.addAttribute("tenantId", tenantId);
                        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
                        myObject.addAttribute("dr", 0);
                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);
                        arangoDbAdapter.insertDocument(experimentConfig.getDsCollectionName(), myObject);

                        System.out.println("Document created:" + myObject.getKey() + myObject.getProperties());
                        msg = "Document created key:" + myObject.getKey() + " properties:" + myObject.getProperties();
                        result.put("status", "success");
                        result.put("reason", msg);
                } catch (ArangoDBException e) {
                        System.err.println("Failed to create document. " + e.getMessage());
                        msg = "Failed to create document. " + e.getMessage();
                        result.put("status", "failed");
                        result.put("reason", msg);
                }
                return result;
        }

        /**
         * 功能描述:更新dataSource
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:46
         */
        @RequestMapping(value = "/dataApplicance/editDataSource/{key}", method = RequestMethod.PUT, consumes = "application/json")
        public Map<String, Object> editDataSource(@RequestBody BaseDocument myObject, @PathVariable(value = "key") String key, @PathVariable(value = "tenantId") String tenantId) {
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {
                        Date d = new Date();
                        //写入当前时间，格式"yyyy-MM-dd kk:mm:ss"
                        myObject.addAttribute("updateOn", getTimeSec(d));

                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);

                        arangoDbAdapter.updateDocument(experimentConfig.getDsCollectionName(), key, myObject);

                        System.out.println("Document updated:" + key + myObject.getProperties());
                        msg = "Document updated key :" + key + " properties:" + myObject.getProperties();
                        result.put("status", "success");
                        result.put("reason", msg);
                } catch (ArangoDBException e) {
                        System.err.println("Failed to create document. " + e.getMessage());
                        msg = "Failed to create document. " + e.getMessage();
                        result.put("status", "failed");
                        result.put("reason", msg);
                }
                return result;
        }

        /**
         * 功能描述:删除dataSource档案
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:46
         */
        @RequestMapping(value = "/dataApplicance/deleteDataSource/{key}", method = RequestMethod.DELETE, consumes = "application/json")
        public Map<String, Object> deleteDataSource(@PathVariable(value = "key") String key, @PathVariable(value = "tenantId") String tenantId) {
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {
                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);

                        arangoDbAdapter.deleteDocument(experimentConfig.getDsCollectionName(), key);

                        System.out.println("Document deleted:" + key);
                        msg = "Document  deleted:" + key;
                        result.put("status", "success");
                        result.put("reason", msg);
                } catch (ArangoDBException e) {
                        System.err.println("Failed to deleted document. " + e.getMessage());
                        msg = "Failed to deleted document. " + e.getMessage();
                        result.put("status", "failed");
                        result.put("reason", msg);
                }
                return result;
        }

        /***
         * 功能描述:根据租户id,获取相应的Database，实现多租户数据库隔离
         * @param tenantId
         * @return: com.bigdata.springboot.arangodbcrud.ArangoDbAdapterExt
         * @Author: EDZ
         * @Date: 2020/5/8 13:42
         */
//        private ArangoDbAdapterExt getArangoDbAdapter(@PathVariable("tenantId") String tenantId) {
//                ArangoDatabase arangoDatabase = arangoDB.db(tenantId);
//                //构造ArangoDbAdapterExt，注入arangoDB，arangoDatabase
//                return new ArangoDbAdapterExt(arangoDB, arangoDatabase);
//        }

        /***
         * 功能描述: 查询指定url的notebook数据并返回
         * @param
         * @return: java.lang.String
         * @Author: EDZ
         * @Date: 2020/5/8 10:03
         */
//        @GetMapping("/queryNoteBooks/{path}")
//        public String queryNoteBooks(@PathVariable("path") String path) {
//                String url = jupyterUrl + "/api/contents" + (path == null ? "" : "/" + path);
//                //String json =restTemplate.getForObject(url,Object.class);
//
//                ResponseEntity<String> results = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
//                String json = results.getBody();
//
//                return json;
//        }

        /***
         * 功能描述: 查询根目录的notebook数据并返回
         * @param
         * @return: java.lang.String
         * @Author: EDZ
         * @Date: 2020/5/8 10:03
         */
        @GetMapping("/queryNoteBooks/{username}")
        public String queryNoteBooksByUser(@PathVariable("username") String username) {
                if (username == null || "".equals(username)) {
                        throw new NullPointerException("name cannot be null!");
                }
                String url = jupyterUrl + "/api/contents/" + username;
                ;

                ResponseEntity<String> results = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
                String json = results.getBody();

                return json;
        }

        /***
         * 功能描述: 删除notebook数据
         * @param
         * @return: java.lang.String
         * @Author: EDZ
         * @Date: 2020/5/8 10:03
         */
        @DeleteMapping("/deleteNoteBooks/{username}/{name}")
        public String deleteNoteBooks(@PathVariable("username") String username, @PathVariable("name") String name) {
                if (name == null || "".equals(name)) {
                        throw new NullPointerException("name cannot be null!");
                }
                if (username == null || "".equals(username)) {
                        throw new NullPointerException("username cannot be null!");
                }
                String url = jupyterUrl + "/api/contents/" + username + "/" + name;
                String json;
                String msg;
                try {
                        ResponseEntity<String> results = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
                        json = results.getBody();
                        msg = "successfully deleted " + (json == null ? "" : json);
                } catch (Exception e) {
                        System.err.println("Failed to delete. " + e.getMessage());
                        msg = "delete failed:" + e.getMessage();
                }
                return msg;
        }

        /***
         * 功能描述:修改NoteBooks
         * @param name
         * @return: org.springframework.web.servlet.ModelAndView
         * @Author: EDZ
         * @Date: 2020/5/9 10:23
         */
        @PutMapping("/editNoteBooks/{username}/{name}")
        public ModelAndView editNoteBooks(@PathVariable("username") String username, @PathVariable("name") String name) {
                if (name == null || "".equals(name)) {
                        throw new NullPointerException("name cannot be null!");
                }
                if (username == null || "".equals(username)) {
                        throw new NullPointerException("username cannot be null!");
                }
                //页面跳转,到修改页面
                return new ModelAndView(new RedirectView(jupyterUrl + "/edit/" + username + "/" + name));
        }

        /***
         * 功能描述:create NoteBooks
         * 后续可以扩展创建的文本类型
         * @return: org.springframework.web.servlet.ModelAndView
         * @Author: EDZ
         * @Date: 2020/5/9 10:23
         */
        @GetMapping("/createNoteBooks/{username}")
        public ModelAndView createNoteBooks(@PathVariable("username") String username) {
                if (username == null || "".equals(username)) {
                        throw new NullPointerException("username cannot be null!");
                }
                String url = jupyterUrl + "/api/contents/" + username;
                String reqJsonStr = "{\"type\":\"notebook\"}";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<String>(reqJsonStr, headers);
                ResponseEntity<JSONObject> results = restTemplate.exchange(url, HttpMethod.POST, entity, JSONObject.class);
                JSONObject json = results.getBody();
                String str = results.getHeaders().getLocation().getPath() == null ? null : results.getHeaders().getLocation().getPath();
                //截取出创建出的名称
                String name = str.substring("/api/contents/".length(), str.length());
                //页面跳转,到修改页面
                return new ModelAndView(new RedirectView(jupyterUrl + "/notebooks/" + name + "?kernel_name=python3"));
        }

        /**
         * 功能描述:组装AQL公共方法
         *
         * @param tenantId
         * @param collectionName
         * @return: java.lang.String
         * @Author: EDZ
         * @Date: 2020/4/27 17:13
         */

        private String getAqlString(@PathVariable("tenantId") String tenantId, String collectionName) {
                String filterAql = tenantId == null ? "" : " FILTER C.tenantId=='" + tenantId + "'";
                return "FOR C IN " + collectionName + filterAql + "  RETURN C ";
        }
        private String getAqlString( String collectionName) {
                return "FOR C IN " + collectionName  + "  RETURN C ";
        }

//        private Map<String, Object> getDocMaps(String aql, @PathVariable("tenantId") String tenantId) {
//                ArangoCursor<BaseDocument> document = null;
//                Map<String, Object> result = new HashMap<String, Object>();
//                List<HashMap<String, Object>> propertiesList = new ArrayList<HashMap<String, Object>>();
//                Map<String, Object> listBody = new HashMap<String, Object>();
//                try {
//                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);
//                        document = (ArangoCursor<BaseDocument>) arangoDbAdapter.executeQuery(aql);
//                        while (document.hasNext()) {
//                                BaseDocument object = document.next();
//                                String key = object.getKey();
//                                String name = object.getAttribute("name") == null ? "" : object.getAttribute("name").toString();
//                                HashMap<String, Object> properties = new HashMap<String, Object>();
//                                properties.put("id", key);
//                                properties.put("name", name);
//                                propertiesList.add(properties);
//                        }
//                        listBody.put("data", propertiesList);
//                        listBody.put("tenantId", tenantId);
//                        result.put("status", "success");
//                        result.put("reason", "success");
//                        result.put("LIST_body", listBody);
//                } catch (ArangoDBException e) {
//                        result.put("status", "failed");
//                        result.put("reason", e.getMessage().toString());
//                        System.err.println("Failed to get document; " + e.getMessage());
//                }
//
//                return result;
//        }

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


}
