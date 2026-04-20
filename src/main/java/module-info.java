module com.bookstore {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires org.slf4j;

    opens com.bookstore to javafx.fxml;
    opens com.bookstore.controller to javafx.fxml;
    opens com.bookstore.model to javafx.fxml;

    exports com.bookstore;
    exports com.bookstore.model;
    exports com.bookstore.controller;
    exports com.bookstore.service;
    exports com.bookstore.repository;
    exports com.bookstore.util;
}
