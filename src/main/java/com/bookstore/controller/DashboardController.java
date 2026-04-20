package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import com.bookstore.util.DashboardRefreshNotifier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for dashboard.fxml.
 * Displays aggregate stats and a recent-books table.
 */
public class DashboardController {

    @FXML private Label totalBooksValue;
    @FXML private Label totalStockValue;
    @FXML private Label categoriesValue;
    @FXML private Label outOfStockValue;
    @FXML private Label lowStockValue;
    @FXML private Label inventoryValueLabel;

    @FXML private TableView<Book>           recentTable;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colCategory;
    @FXML private TableColumn<Book, Double> colPrice;
    @FXML private TableColumn<Book, Integer> colStock;

    private final BookService bookService = new BookService();

    @FXML
    private void initialize() {
        configureTable();
        refreshData();
        DashboardRefreshNotifier.subscribe(this::refreshData);
    }

    private void configureTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        // Format price as $XX.XX
        colPrice.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price));
            }
        });

        // Color stock badges
        colStock.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(stock));
                    setStyle(stock == 0
                        ? "-fx-text-fill: #F85149; -fx-font-weight: bold;"
                        : "-fx-text-fill: #3FB950; -fx-font-weight: bold;");
                }
            }
        });

        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void loadStats() {
        try {
            totalBooksValue.setText(String.valueOf(bookService.getTotalBooks()));
            totalStockValue.setText(String.valueOf(bookService.getTotalStock()));
            categoriesValue.setText(String.valueOf(bookService.getTotalCategories()));
            outOfStockValue.setText(String.valueOf(bookService.getOutOfStockCount()));
            lowStockValue.setText(String.valueOf(bookService.getLowStockCount()));
            inventoryValueLabel.setText(String.format("$%,.2f", bookService.getTotalInventoryValue()));
        } catch (Exception e) {
            totalBooksValue.setText("—");
            totalStockValue.setText("—");
            categoriesValue.setText("—");
            outOfStockValue.setText("—");
            lowStockValue.setText("—");
            inventoryValueLabel.setText("—");
        }
    }

    private void loadRecentBooks() {
        try {
            List<Book> recent = bookService.getRecentBooks(8);
            recentTable.setItems(FXCollections.observableArrayList(recent));
        } catch (Exception e) {
            recentTable.setPlaceholder(new Label("Could not connect to MongoDB."));
        }
    }

    private void refreshData() {
        loadStats();
        loadRecentBooks();
    }
}
