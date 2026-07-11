import service.InterviewService;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        InterviewService interviewService = new InterviewService();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Adınızı daxil edin: ");
        String candidateName = scanner.nextLine();

        while (true) {
            System.out.println("\n=== Java Interview Simulator ===");
            System.out.println("1. Müsahibəyə başla");
            System.out.println("2. Zəif mövzuları göstər");
            System.out.println("3. İnkişaf statistikasını göstər");
            System.out.println("4. Çıxış");
            System.out.print("Seçiminiz: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> {
                    System.out.print("Neçə sual istəyirsiniz: ");
                    int questionCount = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    interviewService.startInterview(scanner, candidateName, questionCount);
                }
                case 2 -> interviewService.showWeakTopics(candidateName);
                case 3 -> interviewService.showProgressStatistics(candidateName);
                case 4 -> {
                    System.out.println("Çıxılır... Sağ olun!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Yanlış seçim. Yenidən cəhd edin.");
            }
        }
    }
}
