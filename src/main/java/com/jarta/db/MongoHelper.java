package com.jarta.db;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Created by wei on 2015/4/27.
 */
public class MongoHelper {
    private MongoHelper(){ this(DEFAULT_DB);}
    private MongoHelper(String dbName) {
        client = new MongoClient("localhost",27017);
        db = client.getDatabase(dbName);
    }
    private static MongoHelper instance;
    private static Object lock = new Object();
    private MongoClient client;
    private MongoDatabase db;
    private static final String DEFAULT_DB = "test";

    public static MongoHelper getInstance(){
        synchronized (lock) {
            if(instance == null) {
                instance = new MongoHelper();
            }
        }
        return instance;
    }
    public MongoCollection getCollection(String collectionName) {
        return  db.getCollection(collectionName);
    }
}
