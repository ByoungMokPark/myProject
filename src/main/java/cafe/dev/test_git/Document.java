package cafe.dev.test_git;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a document that can be stored and managed by the application.
 */
public class Document {
    private final String id;
    private String title;
    private String content;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Set<String> tags = new LinkedHashSet<>();

    public Document(String id, String title, String content, Set<String> tags) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Document id must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Document title must not be blank");
        }
        this.id = id.trim();
        this.title = title.trim();
        this.content = content == null ? "" : content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (tags != null) {
            tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .forEach(this.tags::add);
        }
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Document title must not be blank");
        }
        this.title = title.trim();
        this.updatedAt = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? "" : content;
        this.updatedAt = LocalDateTime.now();
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void setTags(Set<String> updatedTags) {
        this.tags.clear();
        if (updatedTags != null) {
            updatedTags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .forEach(this.tags::add);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Document{" +
            "id='" + id + '\'' +
            ", title='" + title + '\'' +
            ", tags=" + tags +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
