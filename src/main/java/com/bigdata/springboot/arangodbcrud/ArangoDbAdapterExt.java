package com.bigdata.springboot.arangodbcrud;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.velocypack.VPackSlice;


/***
 * 给ArangoDbAdapter加入构造函数，使其能在Controller使用相应的database
 * @ClassName ArangoDbAdapterExt
 * @Description
 * @Author yangsen
 * @Date2020/4/27 16:58
 * @Version V1.0
 **/
public class ArangoDbAdapterExt extends ArangoDbAdapter {
        public  ArangoDB arangoDB = null;

        public ArangoDatabase arangoDatabase = null;

        public ArangoDbAdapterExt(ArangoDB arangoDB, ArangoDatabase arangoDatabase){
                this.arangoDB = arangoDB;
                this.arangoDatabase = arangoDatabase;
        }

        public ArangoDbAdapterExt(){ }

        @Override
        public void insertDocument(String collectionName, BaseDocument myObject){
                arangoDatabase.collection(collectionName).insertDocument(myObject);
        }
        @Override
        public VPackSlice getVelocyDocument(String collectionName, String key){
                return arangoDatabase.collection(collectionName).getDocument(key, VPackSlice.class);
        }

        @Override
        public CollectionEntity createCollection(String collectionName){
                return arangoDatabase.createCollection(collectionName);
        }
        @Override
        public void dropCollection(String collectionName){
                arangoDatabase.collection(collectionName).drop();
        }

        @Override
        public BaseDocument getDocument(String collectionName, String key){
                return arangoDatabase.collection(collectionName).getDocument(key, BaseDocument.class);
        }
        @Override
        public void updateDocument(String collectionName, String key, BaseDocument myObject){
                arangoDatabase.collection(collectionName).updateDocument(key, myObject);
        }

        @Override
        public void deleteDocument(String collectionName, String key){
                arangoDatabase.collection(collectionName).deleteDocument(key);
        }

        /**
         * 功能描述:根据AQL查询
         * @param query
         * @return: com.arangodb.ArangoCursor<com.arangodb.entity.BaseDocument>
         * @Author: EDZ
         * @Date: 2020/4/28 10:09
         */
        public ArangoCursor<BaseDocument> executeQuery(String query){
                //根据AQL查询
                return arangoDatabase.query(query,  BaseDocument.class);
        }
}
