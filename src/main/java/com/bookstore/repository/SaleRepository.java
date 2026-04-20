package com.bookstore.repository;

import com.bookstore.model.Sale;
import com.bookstore.util.MongoDBUtil;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.List;

/**
 * Persists sale records in the {@code sales} collection.
 */
public class SaleRepository {

    private static final String COLLECTION_NAME = "sales";

    private final MongoCollection<Document> collection;

    public SaleRepository() {
        this.collection = MongoDBUtil.getDatabase().getCollection(COLLECTION_NAME);
    }

    public void insert(Sale sale) {
        Document doc = sale.toDocument();
        collection.insertOne(doc);
        sale.setId(doc.getObjectId("_id"));
    }

    /** Number of sale records (transactions). */
    public long countAll() {
        return collection.countDocuments();
    }

    /** Sum of {@code totalPrice} across all sales. */
    public double totalRevenue() {
        List<Document> pipeline = List.of(
            new Document("$group", new Document("_id", null)
                .append("total", new Document("$sum", "$totalPrice")))
        );
        Document result = collection.aggregate(pipeline).first();
        return result != null ? ((Number) result.get("total")).doubleValue() : 0.0;
    }
}
