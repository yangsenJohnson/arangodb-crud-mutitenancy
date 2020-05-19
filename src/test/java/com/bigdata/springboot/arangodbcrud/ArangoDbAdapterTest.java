package com.bigdata.springboot.arangodbcrud;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.util.MapBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ArangoDbAdapterTest {

    private ArangoDbAdapter arangoDbAdapter;
    private String testDBName = "TestDb";
    private String testCollectionName = "TestCollection";


    @BeforeClass
    public void setup() {
        try {
            arangoDbAdapter = new ArangoDbAdapter();
            arangoDbAdapter.Init("127.0.0.1", 8529);
        } catch (Exception ex) {
        } finally {
        }
    }

    @Test(groups = "groupCorrect", priority = 1)
    void createDatabaseTest() {
        boolean successful = arangoDbAdapter.createDatabase(testDBName);
        Assert.assertTrue(successful);
    }

    @Test(groups = "groupCorrect", priority = 2)
    void selectDatabaseTest() {
        ArangoDatabase arangoDatabase = arangoDbAdapter.selectDatabase(testDBName);
        Assert.assertNotNull(arangoDatabase);
    }

    @Test(groups = "groupCorrect", dependsOnMethods = {"selectDatabaseTest"}, priority = 3)
    void createCollectionTest() {
        CollectionEntity myArangoCollection = null;
        try {
            myArangoCollection = arangoDbAdapter.createCollection(testCollectionName);
            System.out.println("Collection created: " + myArangoCollection.getName());
        } catch (ArangoDBException e) {
            System.err.println("Failed to create collection: " + testCollectionName + "; " + e.getMessage());
        }
        Assert.assertNotNull(myArangoCollection);
    }

    @Test(groups = "groupCorrect", dependsOnMethods = {"selectDatabaseTest"}, priority = 4)
    void insertDocumentTest() {
        boolean successful = false;
        BaseDocument myObject = new BaseDocument();
        myObject.setKey("myKey");
        myObject.addAttribute("name", "jack");
        myObject.addAttribute("age", 20);
        try {
            arangoDbAdapter.insertDocument("TestCollection", myObject);
            successful = true;
            System.out.println("Document created");
        } catch (ArangoDBException e) {
            System.err.println("Failed to create document. " + e.getMessage());
        }
        Assert.assertTrue(successful);
    }

    @Test(groups = "groupCorrect", dependsOnMethods = {"selectDatabaseTest"}, priority = 7)
    void getDocumentTest() {
        BaseDocument document = null;
        try {
            document = arangoDbAdapter.getDocument(testCollectionName, "myKey");
            System.out.println("Key: " + document.getKey());
            Map<String, Object> properties = document.getProperties();
            for (Map.Entry<String, Object> entry: properties.entrySet()){
                System.out.println("Attribute " + entry.getKey() + ": " + entry.getValue());
            }
        } catch (ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }
        Assert.assertNotNull(document);
    }

    @Test(groups = "groupCorrect", dependsOnMethods = {"selectDatabaseTest"}, priority = 5)
    void getVelocyDocumentTest() {
        VPackSlice document = null;
        try {
            document = arangoDbAdapter.getVelocyDocument(testCollectionName, "myKey");
            System.out.println("Key: " + document.get("_key").getAsString());
            System.out.println("Attribute a: " + document.get("name").getAsString());
            System.out.println("Attribute b: " + document.get("age").getAsInt());
        } catch (ArangoDBException | VPackException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }
        Assert.assertNotNull(document);
    }

    @Test(groups = "groupCorrect", dependsOnMethods = {"selectDatabaseTest"}, priority = 6)
    void updateDocumentTest() {
        boolean successful = false;
        BaseDocument myObject = new BaseDocument();
        myObject.addAttribute("job", "Bartender");
        try {
            arangoDbAdapter.updateDocument(testCollectionName, "myKey", myObject);
            successful = true;
        } catch (ArangoDBException e) {
            System.err.println("Failed to update document. " + e.getMessage());
        }
        Assert.assertTrue(successful);
    }

    @Test(groups = "groupCorrect", dependsOnMethods = {"selectDatabaseTest"}, priority = 8)
    void deleteDocumentTest() {
        boolean successful = false;
        try {
            arangoDbAdapter.deleteDocument(testCollectionName, "myKey");
            successful = true;
        } catch (ArangoDBException e) {
            System.err.println("Failed to delete document. " + e.getMessage());
        }
        Assert.assertTrue(successful);
    }

    @Test(groups = "groupCorrect", dependsOnMethods = {"selectDatabaseTest"}, priority = 9)
    void executeQueryTest1() {
        int totalCount = 10;
        //create some documents named "Homer"
        for (int i = 0; i < totalCount; i++) {
            BaseDocument value = new BaseDocument();
            value.setKey(String.valueOf(i));
            value.addAttribute("name", "Homer");
            arangoDbAdapter.insertDocument(testCollectionName, value);
        }

        //get all document named "Homer", and go through query result
        AtomicInteger resultCount = new AtomicInteger();
        try {
            String query = "FOR t IN TestCollection FILTER t.name == @name RETURN t";
            Map<String, Object> bindVars = new MapBuilder().put("name", "Homer").get();
            ArangoCursor<BaseDocument> cursor = arangoDbAdapter.executeQuery(query, bindVars);
            cursor.forEachRemaining(aDocument -> {
                System.out.println("read Key: " + aDocument.getKey());
                resultCount.getAndIncrement();
            });
            System.out.println("Total read " + resultCount + " document");
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }
        Assert.assertEquals(resultCount.intValue(), totalCount);
    }

    @Test(groups = "groupCorrect", dependsOnMethods = {"selectDatabaseTest", "executeQueryTest1"}, priority = 10)
    void executeQueryTest2() {
        //delete all document we created in before
        AtomicInteger resultCount = new AtomicInteger();
        try {
            String query = "FOR t IN TestCollection FILTER t.name == @name "
                    + "REMOVE t IN TestCollection LET removed = OLD RETURN removed";
            Map<String, Object> bindVars = new MapBuilder().put("name", "Homer").get();
            ArangoCursor<BaseDocument> cursor = arangoDbAdapter.executeQuery(query, bindVars);
            cursor.forEachRemaining(aDocument -> {
                System.out.println("Removed document " + aDocument.getKey());
                resultCount.incrementAndGet();          //no difference with getAndIncrement() if you no care the return value
            });
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }

        Assert.assertEquals(resultCount.intValue(), 10);
    }

    @Test(groups = "groupCorrect", dependsOnMethods = {"selectDatabaseTest"}, priority = 11)
    void dropCollectionTest() {
        boolean successful = false;
        try {
            arangoDbAdapter.dropCollection(testCollectionName);
            successful = true;
        } catch (ArangoDBException e) {
            System.err.println("Failed to drop collection. " + e.getMessage());
        }
        Assert.assertTrue(successful);
    }

    @Test(groups = "groupCorrect", priority = 12)
    void dropDatabaseTest() {
        boolean successful = false;
        try {
            successful = arangoDbAdapter.dropDatabase(testDBName);
        } catch (ArangoDBException e) {
            System.err.println("Failed to drop collection. " + e.getMessage());
        }
        Assert.assertTrue(successful);
    }
}
