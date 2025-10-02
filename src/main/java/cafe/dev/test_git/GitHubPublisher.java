package cafe.dev.test_git;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Publishes rendered documents to a GitHub repository using the REST API.
 */
public class GitHubPublisher {
    private static final String API_BASE = "https://api.github.com";
    private static final Pattern SHA_PATTERN = Pattern.compile("\"sha\"\\s*:\\s*\"([0-9a-fA-F]{40})\"");

    private final HttpClient httpClient;

    public GitHubPublisher() {
        this(HttpClient.newHttpClient());
    }

    public GitHubPublisher(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Publishes the given markdown content to GitHub.
     *
     * @param markdown the document body to publish
     * @param request  publishing options
     * @return a {@link PublishResult} describing the outcome
     * @throws IOException          when network errors occur
     * @throws InterruptedException when the thread is interrupted
     */
    public PublishResult publish(String markdown, PublishRequest request) throws IOException, InterruptedException {
        String encodedPath = encodePath(request.getPath());
        String targetUrl = API_BASE + "/repos/" + encodeSegment(request.getOwner()) + "/"
            + encodeSegment(request.getRepository()) + "/contents/" + encodedPath;

        Optional<String> existingSha = fetchExistingSha(targetUrl, request);

        String payload = buildPayload(markdown, request, existingSha);
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(targetUrl))
            .header("Authorization", "Bearer " + request.getToken())
            .header("Accept", "application/vnd.github+json")
            .PUT(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return PublishResult.success(existingSha.isPresent(), response.statusCode());
        }

        return PublishResult.failure(response.statusCode(), response.body());
    }

    private Optional<String> fetchExistingSha(String targetUrl, PublishRequest request) throws IOException, InterruptedException {
        String urlWithBranch = request.getBranch() == null
            ? targetUrl
            : targetUrl + "?ref=" + URLEncoder.encode(request.getBranch(), StandardCharsets.UTF_8);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(urlWithBranch))
            .header("Authorization", "Bearer " + request.getToken())
            .header("Accept", "application/vnd.github+json")
            .GET();

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Matcher matcher = SHA_PATTERN.matcher(response.body());
            if (matcher.find()) {
                return Optional.ofNullable(matcher.group(1));
            }
        }
        return Optional.empty();
    }

    private String buildPayload(String markdown, PublishRequest request, Optional<String> existingSha) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        builder.append("\"message\":\"").append(escapeJson(request.getCommitMessage())).append("\"");
        builder.append(',');
        builder.append("\"content\":\"")
            .append(Base64.getEncoder().encodeToString(markdown.getBytes(StandardCharsets.UTF_8)))
            .append("\"");
        if (request.getBranch() != null && !request.getBranch().isBlank()) {
            builder.append(',');
            builder.append("\"branch\":\"")
                .append(escapeJson(request.getBranch()))
                .append("\"");
        }
        existingSha.ifPresent(sha -> {
            builder.append(',');
            builder.append("\"sha\":\"").append(sha).append("\"");
        });
        builder.append('}');
        return builder.toString();
    }

    private String encodePath(String path) {
        String[] segments = path.split("/");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                builder.append('/');
            }
            builder.append(encodeSegment(segments[i]));
        }
        return builder.toString();
    }

    private String encodeSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Parameters describing a GitHub publishing request.
     */
    public static class PublishRequest {
        private final String owner;
        private final String repository;
        private final String branch;
        private final String path;
        private final String token;
        private final String commitMessage;

        public PublishRequest(String owner, String repository, String branch, String path, String token, String commitMessage) {
            this.owner = owner;
            this.repository = repository;
            this.branch = (branch == null || branch.isBlank()) ? null : branch;
            this.path = path;
            this.token = token;
            this.commitMessage = commitMessage;
        }

        public String getOwner() {
            return owner;
        }

        public String getRepository() {
            return repository;
        }

        public String getBranch() {
            return branch;
        }

        public String getPath() {
            return path;
        }

        public String getToken() {
            return token;
        }

        public String getCommitMessage() {
            return commitMessage;
        }
    }

    /**
     * Outcome of a publishing attempt.
     */
    public static class PublishResult {
        private final boolean success;
        private final boolean updated;
        private final int statusCode;
        private final String errorMessage;

        private PublishResult(boolean success, boolean updated, int statusCode, String errorMessage) {
            this.success = success;
            this.updated = updated;
            this.statusCode = statusCode;
            this.errorMessage = errorMessage;
        }

        public static PublishResult success(boolean updated, int statusCode) {
            return new PublishResult(true, updated, statusCode, null);
        }

        public static PublishResult failure(int statusCode, String message) {
            return new PublishResult(false, false, statusCode, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isUpdated() {
            return updated;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
