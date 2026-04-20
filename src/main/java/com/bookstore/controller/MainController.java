package com.bookstore.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.IOException;

/**
 * Controller for main.fxml.
 * Owns the shell layout (sidebar + content area) and handles navigation.
 */
public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private HBox       navDashboard;
    @FXML private HBox       navBooks;
    @FXML private HBox       navAddBook;

    private HBox activeNavItem;

    @FXML
    private void initialize() {
        // Show dashboard on startup
        showDashboard();
    }

    // ── Navigation handlers (called by FXML onMouseClicked) ──────────────────

    @FXML
    private void showDashboard() {
        loadView("dashboard.fxml");
        setActiveNav(navDashboard);
    }

    @FXML
    private void showBooks() {
        loadView("books.fxml");
        setActiveNav(navBooks);
    }

    @FXML
    private void showAddBook() {
        // Load the books view then immediately open the Add dialog
        FXMLLoader loader = loadView("books.fxml");
        setActiveNav(navBooks);
        if (loader != null) {
            BooksController bc = loader.getController();
            bc.openAddDialog();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Loads an FXML file into the center of the BorderPane. Returns the loader for controller access. */
    private FXMLLoader loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bookstore/fxml/" + fxmlFile)
            );
            Node view = loader.load();
            rootPane.setCenter(view);
            return loader;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Swaps the active CSS class on the sidebar nav items. */
    private void setActiveNav(HBox selected) {
        if (activeNavItem != null) {
            activeNavItem.getStyleClass().remove("nav-item-active");
            activeNavItem.getStyleClass().add("nav-item");
        }
        selected.getStyleClass().remove("nav-item");
        selected.getStyleClass().add("nav-item-active");
        activeNavItem = selected;
    }
}
