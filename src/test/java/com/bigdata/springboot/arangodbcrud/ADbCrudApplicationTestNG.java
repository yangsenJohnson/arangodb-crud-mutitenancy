package com.bigdata.springboot.arangodbcrud;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.bigdata.springboot.config.ExperimentConfig;
import com.bigdata.springboot.controller.ExperimentController;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.servlet.ModelAndView;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ADbCrudApplicationTestNG extends AbstractTestNGSpringContextTests {

//    @Autowired
//    public DemoConfig demoConfig;
//
//    @Autowired
//    public CreatePodsDemo demo;
//
//    @org.testng.annotations.Test
//    public  void contextLoads() throws InterruptedException {
//        //先注入，才能在调用demo.create时候demo里注入demoConfig，否则会空指针
//        //CreateDemo demo = new CreateDemo();
//        String result = demo.create("test8");//输入创建pod名称
////        Assert.assertEquals(result, "sucess");
//        Assert.assertNotNull(result);
//    }
//    private String tenantId = "root";
    private String tenantId = "_system";
    private int lastDays =60;
    private String computekey ="13386888";
    private String dskey ="13386887";
    private String name ="Untitled18.ipynb";
    private  String username="datalab";
    private Date d=new Date();

    private ArangoDbAdapter arangoDbAdapter;

    private String testCollectionName = "TestCollection";

    @Autowired
    public ExperimentConfig experimentConfig;

    @Autowired
    public ExperimentController experimentController;
    //配置文件获取host
    private ArangoDB arangoDB = new ArangoDB.Builder().host(experimentConfig.getHost(), experimentConfig.getPort()).build();

    private ArangoDatabase arangoDatabase = arangoDB.db(experimentConfig.getDbName());
    //构造ArangoDbAdapterExt，注入arangoDB，arangoDatabase
//    private ArangoDbAdapterExt arangoDbAdapter = new ArangoDbAdapterExt(arangoDB, arangoDatabase);

    @BeforeClass
    public void setup() {
        try {
            arangoDbAdapter = new ArangoDbAdapterExt();
            arangoDbAdapter.Init(experimentConfig.getHost(),  experimentConfig.getPort());
        } catch (Exception ex) {
        } finally {
        }
    }
//    @Test
//    public  void contextLoads() throws InterruptedException {
//
//        Assert.assertNotNull("");
//    }

    @Test(groups = "groupCorrect", priority = 1)
    void queryComputersTest() throws SQLException {
      Map<String , Object> result= (Map<String , Object>) experimentController.queryComputers(tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }
    @Test(groups = "groupCorrect", priority = 2)
    void listDataSourceTest() throws SQLException {
        Map<String , Object> result= (Map<String , Object>)experimentController.listDataSource(tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }
    @Test(groups = "groupCorrect", priority = 3)
    void viewNotebookTest() throws SQLException {
        Map<String , Object> result= (Map<String , Object>) experimentController.viewNotebook(tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }

    @Test(groups = "groupCorrect", priority =4)
    void queryExperimentsByDateTest() throws SQLException {
        Map<String , Object> result= (Map<String , Object>) experimentController.queryExperimentsByDate(lastDays,tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }
    @Test(groups = "groupCorrect",  priority =5)
    void getTimeSecTest() throws SQLException {

        String result=  experimentController.getTimeSec(d);
        Assert.assertNotNull(result);
    }
    @Test(groups = "groupCorrect",dependsOnMethods= {"getTimeSecTest"},  priority =6)
    void saveExperimentTest() throws SQLException {
        BaseDocument myObject = new BaseDocument();
//        myObject.setKey("myKey1");
        myObject.addAttribute("createTime",experimentController.getTimeSec(d));
        //加入多租户tenantId
        myObject.addAttribute("tenantId",tenantId);
        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
        myObject.addAttribute("dr",0);
        Map<String , Object> result= (Map<String , Object>) experimentController.saveExperiment(myObject,tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }

    @Test(groups = "groupCorrect", priority =6)
    void saveComputeTest() throws SQLException {
        BaseDocument myObject = new BaseDocument();
//        myObject.setKey("myKey1");
        myObject.addAttribute("createTime",experimentController.getTimeSec(d));
        //加入多租户tenantId
        myObject.addAttribute("tenantId",tenantId);
        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
        myObject.addAttribute("dr",0);
        Map<String , Object> result= (Map<String , Object>) experimentController.saveComputers(myObject,tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }
    @Test(groups = "groupCorrect", priority =7)
    void editComputeTest() throws SQLException {
        BaseDocument myObject = new BaseDocument();
//        myObject.setKey("myKey1");
        myObject.addAttribute("createTime",experimentController.getTimeSec(d));
        //加入多租户tenantId
        myObject.addAttribute("tenantId",tenantId);
        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
        myObject.addAttribute("dr",0);
        Map<String , Object> result= (Map<String , Object>) experimentController.editComputer(myObject,computekey,tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }
    @Test(groups = "groupCorrect", priority =7)
    void deleteComputeTest() throws SQLException {
        Map<String , Object> result= (Map<String , Object>) experimentController.deleteComputer(computekey,tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }

    @Test(groups = "groupCorrect", priority =6)
    void savedsTest() throws SQLException {
        BaseDocument myObject = new BaseDocument();
//        myObject.setKey("myKey1");
        myObject.addAttribute("createTime",experimentController.getTimeSec(d));
        //加入多租户tenantId
        myObject.addAttribute("tenantId",tenantId);
        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
        myObject.addAttribute("dr",0);
        Map<String , Object> result= (Map<String , Object>) experimentController.createNewDataSource(myObject,tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }
    @Test(groups = "groupCorrect", priority =7)
    void editdsTest() throws SQLException {
        BaseDocument myObject = new BaseDocument();
//        myObject.setKey("myKey1");
        myObject.addAttribute("createTime",experimentController.getTimeSec(d));
        //加入多租户tenantId
        myObject.addAttribute("tenantId",tenantId);
        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
        myObject.addAttribute("dr",0);
        Map<String , Object> result= (Map<String , Object>) experimentController.editDataSource(myObject,dskey,tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }
    @Test(groups = "groupCorrect", priority =7)
    void deletedsTest() throws SQLException {
        Map<String , Object> result= (Map<String , Object>) experimentController.deleteDataSource(dskey,tenantId);
        Assert.assertEquals(result.get("status"),"success");
    }

    @Test(groups = "groupCorrect", priority =6)
    void createnotebooksTest() throws SQLException {

        ModelAndView result= (ModelAndView) experimentController.createNoteBooks(username);
        Assert.assertNotNull(result);
    }
    @Test(groups = "groupCorrect", priority =7)
    void editnotebooksTest() throws SQLException {
        BaseDocument myObject = new BaseDocument();
//        myObject.setKey("myKey1");
        myObject.addAttribute("createTime",experimentController.getTimeSec(d));
        //加入多租户tenantId
        myObject.addAttribute("tenantId",tenantId);
        //是否已删除标志字段，删除的数据可以将dr更新为1表示已删除，但查询时需要配合FILTER dr=0
        myObject.addAttribute("dr",0);
        ModelAndView result= (ModelAndView) experimentController.editNoteBooks(username,name);
        Assert.assertNotNull(result);
    }
    @Test(groups = "groupCorrect", priority =7)
    void deletenotebooksTest() throws SQLException {
        String result= (String) experimentController.deleteNoteBooks(username,name);
        Assert.assertNotNull(result);
    }
}
