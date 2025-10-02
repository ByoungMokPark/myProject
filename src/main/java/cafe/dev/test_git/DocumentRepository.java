package cafe.dev.test_git;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory store for documents.
 */
public class DocumentRepository {
    private final Map<String, Document> documents = new ConcurrentHashMap<>();

    public Optional<Document> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(documents.get(id.trim()));
    }

    public Collection<Document> findAll() {
        return documents.values();
    }

    public Document save(Document document) {
        documents.put(document.getId(), document);
        return document;
    }

    public Optional<Document> delete(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(documents.remove(id.trim()));
    }

    public boolean exists(String id) {
        if (id == null) {
            return false;
        }
        return documents.containsKey(id.trim());
    }

    public int count() {
        return documents.size();
    }
}
