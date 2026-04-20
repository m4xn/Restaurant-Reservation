package com.bookstore.repository;

import com.bookstore.model.Book;
import com.bookstore.util.MongoDBUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Repository layer: all MongoDB CRUD operations for the "books" collection.
 */
public class BookRepository {

    private static final String COLLECTION_NAME = "books";

    private final MongoCollection<Document> collection;

    public BookRepository() {
        this.collection = MongoDBUtil.getDatabase().getCollection(COLLECTION_NAME);
    }

    // ── Create ────────────────────────────────────────────────────────────────

    /** Inserts a new book. MongoDB assigns the _id automatically. */
    public void insert(Book book) {
        Document doc = book.toDocument();
        collection.insertOne(doc);
        // Write the generated _id back to the model
        book.setId(doc.getObjectId("_id"));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Returns all books sorted by title. */
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        collection.find()
                  .sort(Sorts.ascending("title"))
                  .forEach(doc -> books.add(Book.fromDocument(doc)));
        return books;
    }

    /** Returns books whose title, author, or ISBN match the search term (case-insensitive). */
    public List<Book> search(String term) {
        Pattern regex = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.or(
            Filters.regex("title",  regex),
            Filters.regex("author", regex),
            Filters.regex("isbn",   regex)
        );
        List<Book> books = new ArrayList<>();
        collection.find(filter)
                  .sort(Sorts.ascending("title"))
                  .forEach(doc -> books.add(Book.fromDocument(doc)));
        return books;
    }

    /** Returns books belonging to a specific category. */
    public List<Book> findByCategory(String category) {
        List<Book> books = new ArrayList<>();
        collection.find(Filters.eq("category", category))
                  .sort(Sorts.ascending("title"))
                  .forEach(doc -> books.add(Book.fromDocument(doc)));
        return books;
    }

    /** Returns the 10 most recently inserted books for the dashboard. */
    public List<Book> findRecent(int limit) {
        List<Book> books = new ArrayList<>();
        collection.find()
                  .sort(Sorts.descending("_id"))
                  .limit(limit)
                  .forEach(doc -> books.add(Book.fromDocument(doc)));
        return books;
    }

    /** Finds a single book by its ObjectId. */
    public Book findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        return doc != null ? Book.fromDocument(doc) : null;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /** Updates all mutable fields of an existing book. */
    public void update(Book book) {
        Bson filter = Filters.eq("_id", book.getId());
        Bson update = Updates.combine(
            Updates.set("title",           book.getTitle()),
            Updates.set("author",          book.getAuthor()),
            Updates.set("category",        book.getCategory()),
            Updates.set("price",           book.getPrice()),
            Updates.set("stockQuantity",   book.getStockQuantity()),
            Updates.set("isbn",            book.getIsbn()),
            Updates.set("publicationYear", book.getPublicationYear()),
            Updates.set("description",     book.getDescription())
        );
        collection.updateOne(filter, update);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /** Deletes a book by its ObjectId. */
    public void delete(ObjectId id) {
        collection.deleteOne(Filters.eq("_id", id));
    }

    // ── Aggregates ────────────────────────────────────────────────────────────

    /** Total number of books in the collection. */
    public long countAll() {
        return collection.countDocuments();
    }

    /** Total stock across all books. */
    public long totalStock() {
        List<Document> pipeline = List.of(
            new Document("$group", new Document("_id", null)
                .append("total", new Document("$sum", "$stockQuantity")))
        );
        Document result = collection.aggregate(pipeline).first();
        return result != null ? result.getInteger("total", 0) : 0;
    }

    /** Number of distinct categories. */
    public long countCategories() {
        return collection.distinct("category", String.class).into(new ArrayList<>()).size();
    }

    /** Number of books with stock quantity of 0. */
    public long countOutOfStock() {
        return collection.countDocuments(Filters.lte("stockQuantity", 0));
    }

    /** All distinct category values, sorted. */
    public List<String> findAllCategories() {
        List<String> cats = new ArrayList<>();
        collection.distinct("category", String.class).forEach(cats::add);
        cats.sort(String::compareToIgnoreCase);
        return cats;
    }

    /** Seeds the collection with sample data if it is empty. */
    public void seedIfEmpty() {
        if (countAll() > 0) return;

        List<Book> samples = List.of(
            new Book("Clean Code", "Robert C. Martin", "Programming",
                     39.99, 15, "9780132350884", 2008,
                     "A handbook of agile software craftsmanship."),
            new Book("The Pragmatic Programmer", "David Thomas & Andrew Hunt", "Programming",
                     49.99, 8,  "9780135957059", 2019,
                     "Your journey to mastery, 20th anniversary edition."),
            new Book("Design Patterns", "Gang of Four", "Architecture",
                     54.99, 5,  "9780201633610", 1994,
                     "Elements of reusable object-oriented software."),
            new Book("Dune", "Frank Herbert", "Science Fiction",
                     14.99, 22, "9780441013593", 1965,
                     "A stunning blend of adventure and mysticism."),
            new Book("Atomic Habits", "James Clear", "Self-Help",
                     18.99, 30, "9780735211292", 2018,
                     "Tiny changes, remarkable results."),
            new Book("The Hobbit", "J.R.R. Tolkien", "Fantasy",
                     12.99, 25, "9780547928227", 1937,
                     "A fantasy novel and children's book."),
            new Book("Sapiens", "Yuval Noah Harari", "History",
                     16.99, 18, "9780062316097", 2011,
                     "A brief history of humankind."),
            new Book("Deep Work", "Cal Newport", "Self-Help",
                     22.99, 0,  "9781455586691", 2016,
                     "Rules for focused success in a distracted world.")
        );
        samples.forEach(this::insert);
    }
}
