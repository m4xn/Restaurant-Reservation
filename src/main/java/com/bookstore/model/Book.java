package com.bookstore.model;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Book domain model.
 * Handles conversion to/from MongoDB Documents.
 */
public class Book {

    private ObjectId id;
    private String title;
    private String author;
    private String category;
    private double price;
    private int stockQuantity;
    private String isbn;
    private int publicationYear;
    private String description;

    public Book() {}

    public Book(String title, String author, String category, double price,
                int stockQuantity, String isbn, int publicationYear, String description) {
        this.title           = title;
        this.author          = author;
        this.category        = category;
        this.price           = price;
        this.stockQuantity   = stockQuantity;
        this.isbn            = isbn;
        this.publicationYear = publicationYear;
        this.description     = description;
    }

    // ── Conversions ──────────────────────────────────────────────────────────

    /** Serialize to a MongoDB Document for insert/update. */
    public Document toDocument() {
        Document doc = new Document();
        if (id != null) doc.append("_id", id);
        doc.append("title",           title)
           .append("author",          author)
           .append("category",        category)
           .append("price",           price)
           .append("stockQuantity",   stockQuantity)
           .append("isbn",            isbn)
           .append("publicationYear", publicationYear)
           .append("description",     description);
        return doc;
    }

    /** Deserialize from a MongoDB Document. */
    public static Book fromDocument(Document doc) {
        Book b = new Book();
        b.id              = doc.getObjectId("_id");
        b.title           = doc.getString("title");
        b.author          = doc.getString("author");
        b.category        = doc.getString("category");
        b.price           = doc.get("price") != null ? ((Number) doc.get("price")).doubleValue() : 0.0;
        b.stockQuantity   = doc.getInteger("stockQuantity", 0);
        b.isbn            = doc.getString("isbn");
        b.publicationYear = doc.getInteger("publicationYear", 0);
        b.description     = doc.getString("description");
        return b;
    }

    /** Returns the MongoDB _id as a hex string, or empty string if null. */
    public String getIdString() {
        return id != null ? id.toHexString() : "";
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public ObjectId getId()                        { return id; }
    public void     setId(ObjectId id)             { this.id = id; }

    public String   getTitle()                     { return title; }
    public void     setTitle(String title)         { this.title = title; }

    public String   getAuthor()                    { return author; }
    public void     setAuthor(String author)       { this.author = author; }

    public String   getCategory()                  { return category; }
    public void     setCategory(String category)   { this.category = category; }

    public double   getPrice()                     { return price; }
    public void     setPrice(double price)         { this.price = price; }

    public int      getStockQuantity()             { return stockQuantity; }
    public void     setStockQuantity(int qty)      { this.stockQuantity = qty; }

    public String   getIsbn()                      { return isbn; }
    public void     setIsbn(String isbn)           { this.isbn = isbn; }

    public int      getPublicationYear()           { return publicationYear; }
    public void     setPublicationYear(int year)   { this.publicationYear = year; }

    public String   getDescription()               { return description; }
    public void     setDescription(String desc)    { this.description = desc; }

    /**
     * Derived inventory status used by table views and badges.
     */
    public String getStatus() {
        if (stockQuantity <= 0) {
            return "Out of Stock";
        }
        if (stockQuantity <= 5) {
            return "Low Stock";
        }
        return "In Stock";
    }

    @Override
    public String toString() {
        return "Book{id=" + getIdString() + ", title='" + title + "'}";
    }
}
