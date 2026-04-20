package com.bookstore.model;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Date;

/**
 * One sale line recorded in the {@code sales} collection.
 */
public class Sale {

    private ObjectId id;
    private ObjectId bookId;
    private String bookTitle;
    private int quantitySold;
    private double unitPrice;
    private double totalPrice;
    private Instant soldAt;

    public Sale() {}

    public Sale(ObjectId bookId, String bookTitle, int quantitySold,
                double unitPrice, double totalPrice, Instant soldAt) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.soldAt = soldAt;
    }

    public Document toDocument() {
        Document doc = new Document();
        if (id != null) {
            doc.append("_id", id);
        }
        doc.append("bookId", bookId)
           .append("bookTitle", bookTitle)
           .append("quantitySold", quantitySold)
           .append("unitPrice", unitPrice)
           .append("totalPrice", totalPrice)
           .append("soldAt", soldAt != null ? Date.from(soldAt) : new Date());
        return doc;
    }

    public static Sale fromDocument(Document doc) {
        Sale s = new Sale();
        s.id = doc.getObjectId("_id");
        s.bookId = doc.getObjectId("bookId");
        s.bookTitle = doc.getString("bookTitle");
        s.quantitySold = doc.getInteger("quantitySold", 0);
        s.unitPrice = doc.get("unitPrice") != null ? ((Number) doc.get("unitPrice")).doubleValue() : 0.0;
        s.totalPrice = doc.get("totalPrice") != null ? ((Number) doc.get("totalPrice")).doubleValue() : 0.0;
        Object raw = doc.get("soldAt");
        if (raw instanceof Date d) {
            s.soldAt = d.toInstant();
        } else {
            s.soldAt = Instant.now();
        }
        return s;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
}
