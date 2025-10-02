package cafe.dev.test_git;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simple console interface for interacting with the document service.
 */
public class DocumentConsoleUI {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final DocumentService service;
    private final DocumentMarkdownFormatter formatter;
    private final GitHubPublisher publisher;
    private final Scanner scanner;

    public DocumentConsoleUI(DocumentService service, DocumentMarkdownFormatter formatter, GitHubPublisher publisher,
        Scanner scanner) {
        this.service = service;
        this.formatter = formatter;
        this.publisher = publisher;
        this.scanner = scanner;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("번호를 선택하세요");
            switch (choice) {
                case "1":
                    addDocument();
                    break;
                case "2":
                    updateDocumentTitle();
                    break;
                case "3":
                    updateDocumentContent();
                    break;
                case "4":
                    updateDocumentTags();
                    break;
                case "5":
                    deleteDocument();
                    break;
                case "6":
                    searchByKeyword();
                    break;
                case "7":
                    filterByTag();
                    break;
                case "8":
                    listDocuments();
                    break;
                case "9":
                    showSummary();
                    break;
                case "10":
                    publishToGitHub();
                    break;
                case "0":
                    running = false;
                    break;
                default:
                    System.out.println("올바르지 않은 입력입니다. 다시 시도하세요.");
            }
        }
        System.out.println("프로그램을 종료합니다.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("==== 문서 관리 프로그램 ====");
        System.out.println("1. 문서 추가");
        System.out.println("2. 문서 제목 수정");
        System.out.println("3. 문서 내용 수정");
        System.out.println("4. 문서 태그 수정");
        System.out.println("5. 문서 삭제");
        System.out.println("6. 키워드로 검색");
        System.out.println("7. 태그로 필터링");
        System.out.println("8. 전체 문서 목록");
        System.out.println("9. 문서 통계 보기");
        System.out.println("10. 깃허브에 문서 등록");
        System.out.println("0. 종료");
    }

    private void addDocument() {
        try {
            String id = prompt("문서 ID");
            String title = prompt("제목");
            String content = promptMultiline("내용 (빈 줄 입력 시 종료)");
            Set<String> tags = parseTags(prompt("태그 (쉼표로 구분, 생략 가능)"));
            service.create(id, title, content, tags);
            System.out.println("문서가 저장되었습니다.");
        } catch (IllegalArgumentException ex) {
            System.out.println("문서를 추가하지 못했습니다: " + ex.getMessage());
        }
    }

    private void updateDocumentTitle() {
        String id = prompt("수정할 문서 ID");
        String newTitle = prompt("새 제목");
        if (service.updateTitle(id, newTitle).isPresent()) {
            System.out.println("제목이 수정되었습니다.");
        } else {
            System.out.println("해당 ID의 문서를 찾을 수 없습니다.");
        }
    }

    private void updateDocumentContent() {
        String id = prompt("수정할 문서 ID");
        String content = promptMultiline("새 내용 (빈 줄 입력 시 종료)");
        if (service.updateContent(id, content).isPresent()) {
            System.out.println("내용이 수정되었습니다.");
        } else {
            System.out.println("해당 ID의 문서를 찾을 수 없습니다.");
        }
    }

    private void updateDocumentTags() {
        String id = prompt("수정할 문서 ID");
        Set<String> tags = parseTags(prompt("새 태그 (쉼표로 구분)"));
        if (service.updateTags(id, tags).isPresent()) {
            System.out.println("태그가 수정되었습니다.");
        } else {
            System.out.println("해당 ID의 문서를 찾을 수 없습니다.");
        }
    }

    private void deleteDocument() {
        String id = prompt("삭제할 문서 ID");
        if (service.remove(id).isPresent()) {
            System.out.println("문서가 삭제되었습니다.");
        } else {
            System.out.println("해당 ID의 문서를 찾을 수 없습니다.");
        }
    }

    private void searchByKeyword() {
        String keyword = prompt("검색할 키워드");
        List<Document> results = service.searchByKeyword(keyword);
        if (results.isEmpty()) {
            System.out.println("검색 결과가 없습니다.");
        } else {
            System.out.println(results.size() + "개의 문서를 찾았습니다.");
            results.forEach(this::printDocumentDetail);
        }
    }

    private void filterByTag() {
        String tag = prompt("필터링할 태그");
        List<Document> results = service.filterByTag(tag);
        if (results.isEmpty()) {
            System.out.println("해당 태그를 가진 문서가 없습니다.");
        } else {
            System.out.println(results.size() + "개의 문서를 찾았습니다.");
            results.forEach(this::printDocumentDetail);
        }
    }

    private void listDocuments() {
        List<Document> documents = service.listAll();
        if (documents.isEmpty()) {
            System.out.println("등록된 문서가 없습니다.");
        } else {
            documents.forEach(this::printDocumentDetail);
        }
    }

    private void showSummary() {
        int total = service.count();
        System.out.println("총 문서 수: " + total);
        if (total == 0) {
            System.out.println("등록된 문서 ID: (없음)");
            return;
        }
        String joinedIds = service.listAll().stream()
            .map(Document::getId)
            .collect(Collectors.joining(", "));
        System.out.println("등록된 문서 ID: " + joinedIds);
    }

    private void publishToGitHub() {
        String id = prompt("등록할 문서 ID");
        Document document = service.findById(id).orElse(null);
        if (document == null) {
            System.out.println("해당 ID의 문서를 찾을 수 없습니다.");
            return;
        }

        String owner = prompt("GitHub 사용자/조직 이름");
        if (owner.isBlank()) {
            System.out.println("사용자 또는 조직 이름을 입력해야 합니다.");
            return;
        }
        String repository = prompt("레포지토리 이름");
        if (repository.isBlank()) {
            System.out.println("레포지토리 이름을 입력해야 합니다.");
            return;
        }
        String branch = prompt("브랜치 (엔터 시 기본 브랜치 사용)");
        if (branch.isBlank()) {
            branch = null;
        }
        String defaultPath = "docs/" + document.getId() + ".md";
        String path = prompt("저장할 경로 (예: " + defaultPath + ")");
        if (path.isBlank()) {
            path = defaultPath;
        }
        String commitMessage = prompt("커밋 메시지 (엔터 시 자동 생성)");
        if (commitMessage.isBlank()) {
            commitMessage = "Add document " + document.getTitle();
        }
        String token = prompt("개인 액세스 토큰");
        if (token.isBlank()) {
            System.out.println("개인 액세스 토큰을 입력해야 합니다.");
            return;
        }

        GitHubPublisher.PublishRequest request = new GitHubPublisher.PublishRequest(owner, repository, branch, path, token,
            commitMessage);

        try {
            GitHubPublisher.PublishResult result = publisher.publish(formatter.toMarkdown(document), request);
            if (result.isSuccess()) {
                String message = result.isUpdated()
                    ? "기존 문서를 업데이트했습니다."
                    : "새 문서를 등록했습니다.";
                System.out.println("GitHub에 성공적으로 업로드되었습니다: " + message);
                System.out.println("HTTP 상태 코드: " + result.getStatusCode());
            } else {
                System.out.println("업로드에 실패했습니다. 상태 코드: " + result.getStatusCode());
                if (result.getErrorMessage() != null && !result.getErrorMessage().isBlank()) {
                    System.out.println("응답 메시지: " + result.getErrorMessage());
                }
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            System.out.println("GitHub 통신이 중단되었습니다.");
        } catch (IOException ex) {
            System.out.println("GitHub에 연결하지 못했습니다: " + ex.getMessage());
        }
    }

    private void printDocumentDetail(Document document) {
        String tags = document.getTags().isEmpty()
            ? "(태그 없음)"
            : String.join(", ", document.getTags());
        System.out.println("-----------------------------");
        System.out.println("ID: " + document.getId());
        System.out.println("제목: " + document.getTitle());
        System.out.println("태그: " + tags);
        System.out.println("생성일: " + document.getCreatedAt().format(DATE_FORMATTER));
        System.out.println("수정일: " + document.getUpdatedAt().format(DATE_FORMATTER));
        System.out.println("내용:\n" + document.getContent());
    }

    private String prompt(String message) {
        System.out.print(message + ": ");
        return scanner.nextLine().trim();
    }

    private String promptMultiline(String message) {
        System.out.println(message);
        StringBuilder builder = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line == null || line.isBlank()) {
                break;
            }
            builder.append(line).append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private Set<String> parseTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return Set.of();
        }
        Set<String> tags = new LinkedHashSet<>();
        for (String tag : rawTags.split(",")) {
            String normalized = tag.trim();
            if (!normalized.isEmpty()) {
                tags.add(normalized);
            }
        }
        return tags;
    }
}
