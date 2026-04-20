package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import com.bookstore.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for add-edit-book.fxml.
 * Works in two modes:
 *   - Add mode  (book == null): inserts a new Book
 *   - Edit mode (book != null): updates the existing Book
 */
public class AddEditBookController {

    // ── Form fields ───────────────────────────────────────────────────────────
    @FXML private Label     dialogTitleLabel;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField isbnField;
    @FXML private TextField yearField;
    @FXML private TextArea  descriptionArea;
    @FXML private Label     errorLabel;
    @FXML private Button    saveButton;

    private Stage       dialogStage;
    private Book        existingBook;   // null = add mode
    private final BookService bookService = new BookService();

    @FXML
    private void initialize() {
        loadCategories();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    // ── Public API (called by BooksController before showAndWait) ─────────────

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Populates the form for edit mode, or leaves it blank for add mode.
     * @param book  null → add; non-null → edit
     */
    public void setBook(Book book) {
        this.existingBook = book;

        if (book == null) {
            dialogTitleLabel.setText("Add New Book");
            saveButton.setText("Save Book");
        } else {
            dialogTitleLabel.setText("Edit Book");
            saveButton.setText("Update Book");
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            categoryCombo.setValue(book.getCategory());
            priceField.setText(String.format("%.2f", book.getPrice()));
            stockField.setText(String.valueOf(book.getStockQuantity()));
            isbnField.setText(book.getIsbn());
            yearField.setText(String.valueOf(book.getPublicationYear()));
            descriptionArea.setText(book.getDescription());
        }
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        clearError();

        // ── Parse & validate ──────────────────────────────────────────────────
        String title    = titleField.getText().trim();
        String author   = authorField.getText().trim();
        String category = categoryCombo.getValue();
        String isbn     = isbnField.getText().trim();
        String desc     = descriptionArea.getText().trim();

        double price;
        int    stock;
        int    year;

        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Price must be a valid number (e.g. 19.99)."); return;
        }
        try {
            stock = Integer.parseInt(stockField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Stock quantity must be a whole number."); return;
        }
        try {
            year = Integer.parseInt(yearField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Publication year must be a 4-digit number."); return;
        }

        // Build Book object
        Book book;
        if (existingBook == null) {
            book = new Book(title, author, category, price, stock, isbn, year, desc);
        } else {
            book = existingBook;
            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory(category);
            book.setPrice(price);
            book.setStockQuantity(stock);
            book.setIsbn(isbn);
            book.setPublicationYear(year);
            book.setDescription(desc);
        }

        // ── Persist ───────────────────────────────────────────────────────────
        try {
            if (existingBook == null) {
                bookService.addBook(book);
                AlertUtil.showInfo("Success", "\"" + title + "\" has been added.");
            } else {
                bookService.updateBook(book);
                AlertUtil.showInfo("Success", "\"" + title + "\" has been updated.");
            }
            dialogStage.close();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void loadCategories() {
        List<String> predefined = List.of(
            "Programming", "Architecture", "Science Fiction", "Fantasy",
            "Mystery", "Self-Help", "Biography", "History",
            "Science", "Business", "Children", "Other"
        );
        categoryCombo.getItems().addAll(predefined);
    }

    private void showError(String message) {
        errorLabel.setText("⚠  " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
