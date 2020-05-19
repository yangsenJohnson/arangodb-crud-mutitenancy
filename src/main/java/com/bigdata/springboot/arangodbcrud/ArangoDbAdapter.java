package com.bigdata.springboot.arangodbcrud;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.velocypack.VPackSlice;
import java.util.Map;

public class ArangoDbAdapter {

    private  ArangoDB arangoDB = null;

    private ArangoDatabase arangoDatabase = null;

    /**
     * Returns an instance of ArangoDB.
     * @param host address of the host
     * @param port port of the host
     * @return ArangoDB link
     */
    public void Init(String host, int port){
        arangoDB = new ArangoDB.Builder().host(host, port).build();
    }

    /**
     * Creates a new database with the given name.
     * @param dbName Name of the database to create
     * @return true if the database was created successfully.
     */
    public boolean createDatabase(String dbName){
        return arangoDB.createDatabase(dbName);
    }

    /**
     * Select a ArangoDatabase instance for the given database name.
     * @param dbName Name of the database
     */
    public ArangoDatabase selectDatabase(String dbName){
        arangoDatabase = arangoDB.db(dbName);
        return arangoDatabase;
    }

    /**
     * Deletes the database from the server.
     * @param dbName Name of the database
     * @return true if the database was dropped successfully
     */
    public Boolean dropDatabase(String dbName){
        return arangoDB.db(dbName).drop();
    }

    /**
     * Creates a collection for the given collection's name, then returns collection information from the server.
     * @param collectionName The name of the collection
     * @return CollectionEntity of created collection
     */
    public CollectionEntity createCollection(String collectionName){
        return arangoDatabase.createCollection(collectionName);
    }

    public void dropCollection(String collectionName){
        arangoDatabase.collection(collectionName).drop();
    }

    /**
     * Creates a new document from the given document, unless there is already a document with the _key given. If no
     * _key is given, a new unique _key is generated automatically.
     * @param myObject A representation of a single document (POJO, VPackSlice or String for JSON)
     */
    public void insertDocument(String collectionName, BaseDocument myObject){
            arangoDatabase.collection(collectionName).insertDocument(myObject);
    }

    /**
     * Retrieves the document with the given key from the collection.
     * @param key  The key of the document
     * @return the document identified by the key
     */
    public BaseDocument getDocument(String collectionName, String key){
        return arangoDatabase.collection(collectionName).getDocument(key, BaseDocument.class);
    }

    /**
     * Retrieves the velocy document with the given key from the collection.
     * @param key  The key of the document
     * @return the document identified by the key
     */
    public VPackSlice getVelocyDocument(String collectionName, String key){
        return arangoDatabase.collection(collectionName).getDocument(key, VPackSlice.class);
    }

    /**
     * Partially updates the document identified by document-key. The value must contain a document with the attributes
     * to patch (the patch document). All attributes from the patch document will be added to the existing document if
     * they do not yet exist, and overwritten in the existing document if they do exist there.
     *
     * @param key   The key of the document
     * @param myObject A representation of a single document (POJO, VPackSlice or String for JSON)
     */
    public void updateDocument(String collectionName, String key, BaseDocument myObject){
        arangoDatabase.collection(collectionName).updateDocument(key, myObject);
    }

    /**
     * Deletes the document with the given {@code key} from the collection.
     * @param key The key of the document
     */
    public void deleteDocument(String collectionName, String key){
        arangoDatabase.collection(collectionName).deleteDocument(key);
    }

    /**
     * Performs a database query using the given {@code query} and {@code bindVars}, then returns a new ArangoCursor instance for the result list.
     * @param query    An AQL query string
     * @param bindVars key/value pairs defining the variables to bind the query to
     * @return cursor of the results
     */
    public ArangoCursor<BaseDocument> executeQuery(String query, Map<String, Object> bindVars){
        return arangoDatabase.query(query, bindVars, null, BaseDocument.class);

    }
}
