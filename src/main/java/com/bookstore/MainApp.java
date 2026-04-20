package com.bookstore;

import com.bookstore.service.BookService;
import com.bookstore.util.MongoDBUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Application entry point.
 * Run with:  mvn javafx:run
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ensure sample books are inserted only for first run.
        new BookService().initialize();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/bookstore/fxml/main.fxml")
        );

        Scene scene = new Scene(loader.load(), 1280, 800);
        scene.getStylesheets().add(
            getClass().getResource("/com/bookstore/css/dark-theme.css").toExternalForm()
        );

        primaryStage.setTitle("BookStore Manager");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Close the MongoDB connection cleanly when the app exits
        MongoDBUtil.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
