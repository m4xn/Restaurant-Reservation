package com.bookstore.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Singleton MongoDB connection utility.
 * Call MongoDBUtil.getDatabase() to obtain a MongoDatabase instance.
 * Call MongoDBUtil.close() on application exit.
 */
public class MongoDBUtil {

    private static final String CONNECTION_URI = "mongodb://localhost:27017";
    private static final String DATABASE_NAME  = "bookstore_db";

    private static MongoClient   mongoClient;
    private static MongoDatabase database;

    private MongoDBUtil() {}

    /** Returns the shared MongoDatabase, creating the connection on first call. */
    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_URI);
            database    = mongoClient.getDatabase(DATABASE_NAME);
        }
        return database;
    }

    /** Closes the MongoDB connection. Call this when the application exits. */
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database    = null;
        }
    }
}
