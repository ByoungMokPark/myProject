package cafe.dev.test_git;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        DocumentRepository repository = new DocumentRepository();
        DocumentService service = new DocumentService(repository);
        DocumentMarkdownFormatter formatter = new DocumentMarkdownFormatter();
        GitHubPublisher publisher = new GitHubPublisher();
        try (Scanner scanner = new Scanner(System.in)) {
            DocumentConsoleUI ui = new DocumentConsoleUI(service, formatter, publisher, scanner);
            ui.run();
        }
    }
}
