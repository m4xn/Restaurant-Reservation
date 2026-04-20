package com.bookstore.controller;

import com.bookstore.model.Book;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class BookDetailsController {

    @FXML private Label titleValue;
    @FXML private Label authorValue;
    @FXML private Label categoryValue;
    @FXML private Label priceValue;
    @FXML private Label stockValue;
    @FXML private Label statusValue;
    @FXML private Label isbnValue;
    @FXML private Label yearValue;
    @FXML private Label descriptionValue;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setBook(Book book) {
        titleValue.setText(safeText(book.getTitle()));
        authorValue.setText(safeText(book.getAuthor()));
        categoryValue.setText(safeText(book.getCategory()));
        priceValue.setText(String.format("$%.2f", book.getPrice()));
        stockValue.setText(String.valueOf(book.getStockQuantity()));
        statusValue.setText(book.getStatus());
        isbnValue.setText(safeText(book.getIsbn()));
        yearValue.setText(String.valueOf(book.getPublicationYear()));
        descriptionValue.setText(safeText(book.getDescription()));
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }
}
