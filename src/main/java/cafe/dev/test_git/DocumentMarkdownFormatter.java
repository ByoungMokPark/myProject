package cafe.dev.test_git;

import java.time.format.DateTimeFormatter;

/**
 * Converts {@link Document} instances into Markdown representations that can be
 * published to services such as GitHub.
 */
public class DocumentMarkdownFormatter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Builds a Markdown string containing the document's metadata and body.
     *
     * @param document the document to format
     * @return a Markdown representation
     */
    public String toMarkdown(Document document) {
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(document.getTitle()).append('\n');
        builder.append("\n");
        builder.append("- ID: ").append(document.getId()).append('\n');
        builder.append("- 생성일: ")
            .append(document.getCreatedAt().format(DATE_FORMATTER))
            .append('\n');
        builder.append("- 수정일: ")
            .append(document.getUpdatedAt().format(DATE_FORMATTER))
            .append('\n');
        if (!document.getTags().isEmpty()) {
            builder.append("- 태그: ");
            builder.append(String.join(", ", document.getTags()));
            builder.append('\n');
        }
        builder.append("\n");
        builder.append(document.getContent()).append('\n');
        return builder.toString();
    }
}
