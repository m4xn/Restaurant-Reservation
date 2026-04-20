package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import org.bson.types.ObjectId;

import java.time.Year;
import java.util.List;

/**
 * Service layer — business logic and validation.
 * Controllers call this class; this class calls BookRepository.
 */
public class BookService {

    private final BookRepository repository;

    public BookService() {
        this.repository = new BookRepository();
    }

    // ── Initialization ────────────────────────────────────────────────────────

    /** Seeds the database with sample data if empty. */
    public void initialize() {
        repository.seedIfEmpty();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<Book> getAllBooks()               { return repository.findAll(); }
    public List<Book> searchBooks(String term)    { return repository.search(term); }
    public List<Book> getBooksByCategory(String c){ return repository.findByCategory(c); }
    public List<Book> getRecentBooks(int limit)   { return repository.findRecent(limit); }
    public List<String> getAllCategories()         { return repository.findAllCategories(); }

    // ── Stats ─────────────────────────────────────────────────────────────────

    public long getTotalBooks()      { return repository.countAll(); }
    public long getTotalStock()      { return repository.totalStock(); }
    public long getTotalCategories() { return repository.countCategories(); }
    public long getOutOfStockCount() { return repository.countOutOfStock(); }
    public long getLowStockCount()   { return repository.countLowStock(); }
    public double getTotalInventoryValue() { return repository.totalInventoryValue(); }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Validates and adds a new book.
     * @throws IllegalArgumentException if validation fails.
     */
    public void addBook(Book book) {
        validate(book);
        repository.insert(book);
    }

    /**
     * Validates and updates an existing book.
     * @throws IllegalArgumentException if validation fails.
     */
    public void updateBook(Book book) {
        if (book.getId() == null)
            throw new IllegalArgumentException("Cannot update a book without an ID.");
        validate(book);
        repository.update(book);
    }

    /** Deletes a book by ID. */
    public void deleteBook(ObjectId id) {
        repository.delete(id);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private void validate(Book book) {
        if (book.getTitle() == null || book.getTitle().isBlank())
            throw new IllegalArgumentException("Title is required.");
        if (book.getAuthor() == null || book.getAuthor().isBlank())
            throw new IllegalArgumentException("Author is required.");
        if (book.getCategory() == null || book.getCategory().isBlank())
            throw new IllegalArgumentException("Category is required.");
        if (book.getIsbn() == null || book.getIsbn().isBlank())
            throw new IllegalArgumentException("ISBN is required.");
        if (book.getPrice() < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
        if (book.getStockQuantity() < 0)
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        int currentYear = Year.now().getValue();
        if (book.getPublicationYear() < 1450 || book.getPublicationYear() > currentYear + 1)
            throw new IllegalArgumentException("Publication year must be between 1450 and " + (currentYear + 1) + ".");
    }
}
