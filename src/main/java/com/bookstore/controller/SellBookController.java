package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import com.bookstore.util.AlertUtil;
import com.bookstore.util.DashboardRefreshNotifier;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Modal for selling copies of a book (customer purchase).
 */
public class SellBookController {

    @FXML private Label     bookTitleLabel;
    @FXML private Label     metaLabel;
    @FXML private TextField quantityField;
    @FXML private Label     errorLabel;

    private Stage       dialogStage;
    private Book        book;
    private final BookService bookService = new BookService();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setBook(Book book) {
        this.book = book;
        bookTitleLabel.setText(book.getTitle());
        metaLabel.setText(String.format(
            "Unit price: $%.2f  ·  Available: %d",
            book.getPrice(),
            book.getStockQuantity()
        ));
        quantityField.clear();
        clearError();
    }

    @FXML
    private void handleConfirm() {
        clearError();
        if (book == null || book.getId() == null) {
            showError("Invalid book.");
            return;
        }
        if (book.getStockQuantity() <= 0) {
            showError("This book is out of stock.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Quantity must be a whole number.");
            return;
        }

        try {
            bookService.sellBook(book.getId(), qty);
            DashboardRefreshNotifier.notifyRefresh();
            AlertUtil.showInfo(
                dialogStage,
                "Sale complete",
                String.format("Sold %d copy/copies of \"%s\". Total: $%.2f",
                    qty, book.getTitle(), book.getPrice() * qty)
            );
            dialogStage.close();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError(dialogStage, "Sale failed", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
