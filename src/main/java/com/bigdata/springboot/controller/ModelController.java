package com.bigdata.springboot.controller;

import com.alibaba.fastjson.JSON;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.bigdata.springboot.arangodbcrud.ArangoDbAdapterExt;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNodeReader;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * ModelController
 *
 * @Author: yangsen
 * @Date: 2020/5/11 11:43
 */
@RestController
@RequestMapping("/api1/{tenantId}")
public class ModelController {

//        @Autowired
//        public ExperimentConfig experimentConfig;

        //配置文件获取host
//        private ArangoDB arangoDB = new ArangoDB.Builder().host(experimentConfig.getHost(), experimentConfig.getPort()).build();


        private ArangoDbUtils arangoDbUtils=new ArangoDbUtils();

        @Value("${param.modelCollectionName}")
        String modelCollectionName;
        /**
         * 功能描述:查看Model列表
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:44
         */
        @GetMapping("/model/queryModelsByEx")
        public Map<String, Object> queryExperimentModels(@PathVariable(value = "tenantId") String tenantId)  {

                //加入多租户筛选
                String aql = arangoDbUtils.getAqlString( modelCollectionName);

                return arangoDbUtils.getDocMapsProps(aql,tenantId);

        }
        /**
         * 功能描述:根据ExperimentID查看model列表
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:44
         */
        @GetMapping("/model/queryExperimentModels/{experimentId}")
        public Map<String, Object> queryExperimentModels(@PathVariable(value = "tenantId") String tenantId,@PathVariable(value = "experimentId") String experimentId)  {

                //加入多租户筛选
                String aql =  "FOR C IN models FILTER C.experimentId=='"+experimentId+"' RETURN C ";

                return arangoDbUtils.getDocMapsProps(aql,tenantId);

        }
        /**
         * 功能描述:保存Model
         * @param tenantId 相应的ExperimentID
         * @param experimentId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:46
         */
        @RequestMapping(value = "/model/promoteModels/{experimentId}", method = RequestMethod.POST, consumes = "application/json")
        public Map<String, Object> promoteModels(@RequestBody BaseDocument myObject, @PathVariable(value = "tenantId") String tenantId,@PathVariable(value = "experimentId") String experimentId) {
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {
                        Date d = new Date();
                        //写入当前时间，格式"yyyy-MM-dd kk:mm:ss"
                        myObject.addAttribute("createOn", arangoDbUtils.getTimeSec(d));
                        myObject.addAttribute("experimentId", experimentId);
                        //加入多租户tenantId
//                        myObject.addAttribute("tenantId", tenantId);
                        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
//                        myObject.addAttribute("dr", 0);


//                        //这个就是你设定的标准JSON
//                        InputStream inputStream = getClass().getResourceAsStream("/Schema.json");
//                        //这个是你打算验证的JSON，这里我也用一份文件来存放，你也可以使用string或者jsonObject
//                        InputStream inputStream1 = getClass().
//                        JSONObject Schema = new JSONObject(new JSONTokener(inputStream));
//                        JSONObject data = new JSONObject(new JSONTokener(inputStream1));
//                        org.everit.json.schema.Schema schema = SchemaLoader.load(Schema);
//
//                        schema.validate(data);
                        String  param= JSON.toJSONString(myObject.getProperties());
                        JsonNode jsonNode = JsonLoader.fromString(param);
                        String filePath = "./mle-ops/experiments.json";
                        JsonNode jsonNodeS=  getSchemaNode(filePath);
                        if(!validateJsonByFgeByJsonNode(jsonNode,jsonNodeS)){
                             throw  new    ArangoDBException(" request validate json schema error,please check your request");
                        }
                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);
                        arangoDbAdapter.insertDocument(modelCollectionName, myObject);

                        System.out.println("Document created:" + myObject.getKey() + myObject.getProperties());
                        msg = "Document created key:" + myObject.getKey() + " properties:" + myObject.getProperties();
                        result.put("status", "success");
                        result.put("reason", msg);
                } catch (ArangoDBException e) {
                        System.err.println("Failed to create document. " + e.getMessage());
                        msg = "Failed to create document. " + e.getMessage();
                        result.put("status", "failed");
                        result.put("reason", msg);
                } catch (IOException e) {
                        e.printStackTrace();
                }
//                catch (ValidationException e) {
//                        System.out.println(e.getErrorMessage());
//                        result.put("reason", e.getErrorMessage());
//                }
                return result;
        }
        public JsonNode getSchemaNode( String filePath){

        JsonNode jsonNode = null;
        try {
                String path = this.getClass().getClassLoader().getResource(filePath).getPath();
                jsonNode = new JsonNodeReader().fromReader(new FileReader(path));
        } catch (FileNotFoundException e) {
                e.printStackTrace();
        } catch (IOException e) {
                e.printStackTrace();
        }
        return jsonNode;
        }

        public static boolean validateJsonByFgeByJsonNode(JsonNode jsonNode, JsonNode schemaNode){
                ProcessingReport report = null;
                boolean falg=false;
                report = JsonSchemaFactory.byDefault().getValidator().validateUnchecked(schemaNode, jsonNode);
                if (report.isSuccess()) {
                        falg=true;
                        // 校验成功
                        System.out.println("校验成功！");
                }else {
                        System.out.println("校验失败！");
                        Iterator<ProcessingMessage> it = report.iterator();
                        while(it.hasNext()){
                                System.out.println(it.next());
                        }
                        falg=false;
                }
        return falg;
        }

        /**
         * 功能描述:修改指定model
         * @param key modelId
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:44
         */
        @RequestMapping(value = "/model/editModels/{key}", method = RequestMethod.PUT, consumes = "application/json")
        public Map<String, Object> editModels (@RequestBody BaseDocument myObject, @PathVariable(value = "key") String key, @PathVariable(value = "tenantId") String tenantId) {
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {
                        Date d = new Date();
                        //写入当前时间，格式"yyyy-MM-dd kk:mm:ss"
                        myObject.addAttribute("lastUpdate", arangoDbUtils.getTimeSec(d));

                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);
                        arangoDbAdapter.updateDocument(modelCollectionName, key, myObject);

                        System.out.println("Document edit:" + key);
                        msg = "Document  edit:" + key;
                        result.put("status", "success");
                        result.put("reason", msg);
                } catch (ArangoDBException e) {
                        System.err.println("Failed to edit document. " + e.getMessage());
                        msg = "Failed to edit document. " + e.getMessage();
                        result.put("status", "failed");
                        result.put("reason", msg);
                }
                return result;
        }
        /**
         * 功能描述:deleteModels 删除指定model
         *
         * @param tenantId
         * @return: java.util.Map<java.lang.String, java.lang.Object>
         * @Author: yangsen
         * @Date: 2020/4/27 15:44
         */
        @RequestMapping(value = "/deleteModels/{key}", method = RequestMethod.DELETE, consumes = "application/json")
        public Map<String, Object> deleteModels(@PathVariable(value = "key") String key, @PathVariable(value = "tenantId") String tenantId) {
                Map<String, Object> result = new HashMap<String, Object>();
                String msg = "";
                try {

                        ArangoDbAdapterExt arangoDbAdapter = arangoDbUtils.getArangoDbAdapter(tenantId);
                        arangoDbAdapter.deleteDocument(modelCollectionName, key);

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

}