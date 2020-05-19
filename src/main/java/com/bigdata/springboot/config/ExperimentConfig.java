package com.bigdata.springboot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

//@Configuration
//@Data
@ConfigurationProperties(prefix = "param")
@Component
public class ExperimentConfig {
        // 不使用
        //        @Value("${param.port}")
//        int port;
//        @Value("${param.host}")
//        String host;
//        @Value("${param.dbName}")
//        String dbName;
//        @Value("${param.collectionName}")
//        String collectionName;
//        @Value("${param.computerClusterName}")
//        String computerClusterName;
//        @Value("${param.dsCollectionName}")
//        String dsCollectionName;
//        @Value("${param.noteBookCllectionName}")
//        String noteBookCllectionName;
//        @Value("${param.modelCollectionName}")
//        String modelCollectionName;
//        @Value("${param.jupyterUrl}")
//        String jupyterUrl;

        private static String host;

        private static int port;

        private static String dbName;

        private static String collectionName;

        private static String computerClusterName;

        private static String dsCollectionName;

        private static String noteBookCllectionName;



        public static String getHost() {
                return host;
        }
        @Value("${param.host}")
        public  void setHost(String host) {
                this.host = host;
        }

        public static int getPort() {
                return port;
        }
        @Value("${param.port}")
        public  void setPort(int port) {
                this.port = port;
        }

        public static String getDbName() {
                return dbName;
        }
        @Value("${param.dbName}")
        public  void setDbName(String dbName) {
                this.dbName = dbName;
        }

        public static String getCollectionName() {
                return collectionName;
        }
        @Value("${param.collectionName}")
        public  void setCollectionName(String collectionName) {
                this.collectionName = collectionName;
        }

        public static String getComputerClusterName() {
                return computerClusterName;
        }
        @Value("${param.computerClusterName}")
        public  void setComputerClusterName(String computerClusterName) {
                this.computerClusterName = computerClusterName;
        }

        public static String getDsCollectionName() {
                return dsCollectionName;
        }
        @Value("${param.dsCollectionName}")
        public void setDsCollectionName(String dsCollectionName) {
                this.dsCollectionName = dsCollectionName;
        }

        public static String getNoteBookCllectionName() {
                return noteBookCllectionName;
        }
        @Value("${param.noteBookCllectionName}")
        public void setNoteBookCllectionName(String noteBookCllectionName) {
                this.noteBookCllectionName = noteBookCllectionName;
        }

}
