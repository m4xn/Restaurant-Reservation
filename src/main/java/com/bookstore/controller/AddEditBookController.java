package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import com.bookstore.util.AlertUtil;
import com.bookstore.util.DashboardRefreshNotifier;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    @FXML private VBox      customCategoryBox;
    @FXML private TextField customCategoryField;
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
        categoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean showOther = "Other".equals(newVal);
            customCategoryBox.setVisible(showOther);
            customCategoryBox.setManaged(showOther);
            if (!showOther) {
                customCategoryField.clear();
            }
        });
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
            customCategoryField.clear();
            categoryCombo.setValue(null);
        } else {
            dialogTitleLabel.setText("Edit Book");
            saveButton.setText("Update Book");
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            applyCategoryForEdit(book.getCategory());
            priceField.setText(String.format("%.2f", book.getPrice()));
            stockField.setText(String.valueOf(book.getStockQuantity()));
            isbnField.setText(book.getIsbn());
            yearField.setText(String.valueOf(book.getPublicationYear()));
            descriptionArea.setText(book.getDescription());
        }
    }

    /** Picks a combo value, or "Other" plus custom field when the stored category is not in the list. */
    private void applyCategoryForEdit(String storedCategory) {
        if (storedCategory == null || storedCategory.isBlank()) {
            categoryCombo.setValue(null);
            customCategoryField.clear();
            return;
        }
        if (categoryCombo.getItems().contains(storedCategory)) {
            categoryCombo.setValue(storedCategory);
            customCategoryField.clear();
        } else {
            categoryCombo.setValue("Other");
            customCategoryField.setText(storedCategory);
        }
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        clearError();

        // ── Parse & validate ──────────────────────────────────────────────────
        String title    = titleField.getText().trim();
        String author   = authorField.getText().trim();
        String category = resolveCategoryFromForm();
        String isbn     = isbnField.getText().trim();
        String desc     = descriptionArea.getText().trim();

        if (title.isBlank()) {
            showError("Title cannot be empty.");
            return;
        }
        if (author.isBlank()) {
            showError("Author cannot be empty.");
            return;
        }
        if (category == null) {
            return;
        }
        if (isbn.isBlank()) {
            showError("ISBN cannot be empty.");
            return;
        }

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
                AlertUtil.showInfo(dialogStage, "Success", "\"" + title + "\" has been added.");
            } else {
                bookService.updateBook(book);
                AlertUtil.showInfo(dialogStage, "Success", "\"" + title + "\" has been updated.");
            }
            DashboardRefreshNotifier.notifyRefresh();
            dialogStage.close();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError(dialogStage, "Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleReset() {
        if (existingBook == null) {
            titleField.clear();
            authorField.clear();
            categoryCombo.setValue(null);
            customCategoryField.clear();
            priceField.clear();
            stockField.clear();
            isbnField.clear();
            yearField.clear();
            descriptionArea.clear();
        } else {
            setBook(existingBook);
        }
        clearError();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Resolves the category string for save.
     * When "Other" is selected, uses {@link #customCategoryField} (must be non-blank).
     * @return resolved category, or {@code null} if validation failed (error already shown)
     */
    private String resolveCategoryFromForm() {
        String selected = categoryCombo.getValue();
        if (selected == null || selected.isBlank()) {
            showError("Category cannot be empty.");
            return null;
        }
        if ("Other".equals(selected)) {
            String custom = customCategoryField.getText().trim();
            if (custom.isBlank()) {
                showError("Please type your category name when \"Other\" is selected.");
                return null;
            }
            return custom;
        }
        return selected;
    }

    private void loadCategories() {
        List<String> predefined = List.of(
            "Fiction", "Science", "Technology", "History", "Business", "Self-Help",
            "Programming", "Architecture", "Science Fiction", "Fantasy",
            "Mystery", "Biography", "Children", "Other"
        );
        categoryCombo.getItems().clear();
        categoryCombo.getItems().addAll(predefined);
        for (String category : bookService.getAllCategories()) {
            if (!categoryCombo.getItems().contains(category)) {
                categoryCombo.getItems().add(category);
            }
        }
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
