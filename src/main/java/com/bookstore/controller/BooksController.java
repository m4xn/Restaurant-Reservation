package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.BookService;
import com.bookstore.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for books.fxml.
 * Manages the full-page book table with search, filter, and CRUD actions.
 */
public class BooksController {

    // ── Toolbar ───────────────────────────────────────────────────────────────
    @FXML private TextField         searchField;
    @FXML private ComboBox<String>  categoryFilter;
    @FXML private ComboBox<String>  sortCombo;
    @FXML private Label             resultCountLabel;
    @FXML private Label             emptyStateTitleLabel;

    // ── Table ─────────────────────────────────────────────────────────────────
    @FXML private TableView<Book>            booksTable;
    @FXML private TableColumn<Book, String>  colTitle;
    @FXML private TableColumn<Book, String>  colAuthor;
    @FXML private TableColumn<Book, String>  colCategory;
    @FXML private TableColumn<Book, Double>  colPrice;
    @FXML private TableColumn<Book, Integer> colStock;
    @FXML private TableColumn<Book, String>  colIsbn;
    @FXML private TableColumn<Book, Integer> colYear;
    @FXML private TableColumn<Book, String>  colStatus;
    @FXML private TableColumn<Book, Void>    colActions;

    private final BookService bookService = new BookService();

    @FXML
    private void initialize() {
        configureColumns();
        configureActionsColumn();
        configureSortOptions();
        loadCategoryFilter();
        loadBooks();

        // Live search as the user types
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterBooks());
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterBooks());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterBooks());
    }

    // ── Table setup ───────────────────────────────────────────────────────────

    private void configureColumns() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Format price
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price));
                setStyle("-fx-alignment: CENTER-RIGHT;");
            }
        });

        // Color-coded stock badge
        colStock.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null); setStyle(""); return;
                }
                setText(String.valueOf(stock));
                if (stock == 0)
                    setStyle("-fx-text-fill: #F85149; -fx-font-weight: bold; -fx-alignment: CENTER;");
                else if (stock <= 5)
                    setStyle("-fx-text-fill: #FBBF24; -fx-font-weight: bold; -fx-alignment: CENTER;");
                else
                    setStyle("-fx-text-fill: #3FB950; -fx-font-weight: bold; -fx-alignment: CENTER;");
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                if ("Out of Stock".equals(status)) {
                    setStyle("-fx-text-fill: #F85149; -fx-font-weight: bold;");
                } else if ("Low Stock".equals(status)) {
                    setStyle("-fx-text-fill: #FBBF24; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #3FB950; -fx-font-weight: bold;");
                }
            }
        });

        booksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    /** Adds View, Edit and Delete buttons to every row in the Actions column. */
    private void configureActionsColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn   = new Button("View");
            private final Button editBtn   = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox   box       = new HBox(8, viewBtn, editBtn, deleteBtn);

            {
                viewBtn.getStyleClass().add("btn-secondary");
                editBtn.getStyleClass().add("btn-secondary");
                deleteBtn.getStyleClass().add("btn-danger");
                box.setStyle("-fx-alignment: CENTER;");

                viewBtn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    openDetailsDialog(book);
                });
                editBtn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    openEditDialog(book);
                });
                deleteBtn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    handleDelete(book);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void configureSortOptions() {
        sortCombo.getItems().setAll(
            "Title (A-Z)",
            "Title (Z-A)",
            "Price (Low to High)",
            "Price (High to Low)",
            "Stock (Low to High)",
            "Stock (High to Low)",
            "Year (Newest)"
        );
        sortCombo.setValue("Title (A-Z)");
    }

    private void loadCategoryFilter() {
        List<String> cats = bookService.getAllCategories();
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.getItems().addAll(cats);
        categoryFilter.setValue("All Categories");
    }

    // ── Data loading ──────────────────────────────────────────────────────────

    private void loadBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            books = applySort(books);
            booksTable.setItems(FXCollections.observableArrayList(books));
            updateResultCount(books.size());
            updateEmptyStateMessage(books.size());
        } catch (Exception e) {
            AlertUtil.showError("Database Error", "Could not load books: " + e.getMessage());
        }
    }

    private void filterBooks() {
        try {
            String term     = searchField.getText().trim();
            String category = categoryFilter.getValue();

            List<Book> books;
            if (!term.isEmpty()) {
                books = bookService.searchBooks(term);
            } else {
                books = bookService.getAllBooks();
            }

            // Apply category filter client-side after the search
            if (category != null && !category.equals("All Categories")) {
                books = books.stream()
                             .filter(b -> category.equalsIgnoreCase(b.getCategory()))
                             .toList();
            }

            books = applySort(books);

            booksTable.setItems(FXCollections.observableArrayList(books));
            updateResultCount(books.size());
            updateEmptyStateMessage(books.size());
        } catch (Exception e) {
            AlertUtil.showError("Search Error", e.getMessage());
        }
    }

    private void updateResultCount(int count) {
        resultCountLabel.setText(count + " book" + (count != 1 ? "s" : ""));
    }

    private List<Book> applySort(List<Book> books) {
        String selectedSort = sortCombo.getValue();
        Comparator<Book> comparator;

        if ("Title (Z-A)".equals(selectedSort)) {
            comparator = Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
        } else if ("Price (Low to High)".equals(selectedSort)) {
            comparator = Comparator.comparingDouble(Book::getPrice);
        } else if ("Price (High to Low)".equals(selectedSort)) {
            comparator = Comparator.comparingDouble(Book::getPrice).reversed();
        } else if ("Stock (Low to High)".equals(selectedSort)) {
            comparator = Comparator.comparingInt(Book::getStockQuantity);
        } else if ("Stock (High to Low)".equals(selectedSort)) {
            comparator = Comparator.comparingInt(Book::getStockQuantity).reversed();
        } else if ("Year (Newest)".equals(selectedSort)) {
            comparator = Comparator.comparingInt(Book::getPublicationYear).reversed();
        } else {
            comparator = Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
        }

        return books.stream().sorted(comparator).toList();
    }

    private void updateEmptyStateMessage(int count) {
        if (emptyStateTitleLabel == null || count > 0) {
            return;
        }

        boolean hasFilters = !searchField.getText().isBlank()
            || (categoryFilter.getValue() != null && !"All Categories".equals(categoryFilter.getValue()));

        emptyStateTitleLabel.setText(hasFilters
            ? "No books found"
            : "Add your first book to get started");
    }

    // ── CRUD handlers ─────────────────────────────────────────────────────────

    @FXML
    public void openAddDialog() {
        openDialog(null);
    }

    private void openEditDialog(Book book) {
        openDialog(book);
    }

    private void handleDelete(Book book) {
        boolean confirmed = AlertUtil.showConfirmation(
            "Delete Book",
            "Are you sure you want to delete \"" + book.getTitle() + "\"?\nThis cannot be undone."
        );
        if (!confirmed) return;

        try {
            bookService.deleteBook(book.getId());
            loadBooks();
            loadCategoryFilter();
            com.bookstore.util.DashboardRefreshNotifier.notifyRefresh();
            AlertUtil.showInfo("Deleted", "\"" + book.getTitle() + "\" has been deleted.");
        } catch (Exception e) {
            AlertUtil.showError("Delete Failed", e.getMessage());
        }
    }

    /**
     * Opens the Add/Edit modal dialog.
     * @param book  null → Add mode; non-null → Edit mode
     */
    private void openDialog(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bookstore/fxml/add-edit-book.fxml")
            );
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(book == null ? "Add New Book" : "Edit Book");
            dialog.setResizable(false);

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                getClass().getResource("/com/bookstore/css/dark-theme.css").toExternalForm()
            );
            dialog.setScene(scene);

            AddEditBookController controller = loader.getController();
            controller.setDialogStage(dialog);
            controller.setBook(book);   // null = add mode

            dialog.showAndWait();

            // Refresh table after dialog closes (whether saved or cancelled)
            loadBooks();
            loadCategoryFilter();

        } catch (IOException e) {
            AlertUtil.showError("Error", "Could not open dialog: " + e.getMessage());
        }
    }

    private void openDetailsDialog(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bookstore/fxml/book-details.fxml")
            );
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Book Details");
            dialog.setResizable(false);

            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                getClass().getResource("/com/bookstore/css/dark-theme.css").toExternalForm()
            );
            dialog.setScene(scene);

            BookDetailsController controller = loader.getController();
            controller.setDialogStage(dialog);
            controller.setBook(book);

            dialog.showAndWait();
        } catch (IOException e) {
            AlertUtil.showError("Error", "Could not open details view: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        sortCombo.setValue("Title (A-Z)");
        loadBooks();
        loadCategoryFilter();
    }
}
