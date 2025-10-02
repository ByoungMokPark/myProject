package cafe.dev.test_git;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides high level operations for managing documents.
 */
public class DocumentService {
    private final DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    public Document create(String id, String title, String content, Set<String> tags) {
        if (repository.exists(id)) {
            throw new IllegalArgumentException("Document with id '" + id + "' already exists");
        }
        Document document = new Document(id, title, content, tags);
        repository.save(document);
        return document;
    }

    public Optional<Document> updateContent(String id, String newContent) {
        return repository.findById(id).map(document -> {
            document.setContent(newContent);
            return document;
        });
    }

    public Optional<Document> updateTitle(String id, String title) {
        return repository.findById(id).map(document -> {
            document.setTitle(title);
            return document;
        });
    }

    public Optional<Document> updateTags(String id, Set<String> tags) {
        return repository.findById(id).map(document -> {
            document.setTags(tags);
            return document;
        });
    }

    public Optional<Document> remove(String id) {
        return repository.delete(id);
    }

    public Optional<Document> findById(String id) {
        return repository.findById(id);
    }

    public List<Document> listAll() {
        return repository.findAll().stream()
            .sorted(Comparator.comparing(Document::getCreatedAt))
            .collect(Collectors.toList());
    }

    public List<Document> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String lowered = keyword.toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
            .filter(doc -> doc.getTitle().toLowerCase(Locale.ROOT).contains(lowered)
                || doc.getContent().toLowerCase(Locale.ROOT).contains(lowered))
            .sorted(Comparator.comparing(Document::getTitle, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    public List<Document> filterByTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return List.of();
        }
        String normalized = tag.trim().toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
            .filter(doc -> doc.getTags().stream()
                .map(existingTag -> existingTag.toLowerCase(Locale.ROOT))
                .anyMatch(existing -> existing.equals(normalized)))
            .sorted(Comparator.comparing(Document::getTitle, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    public int count() {
        return repository.count();
    }
}
