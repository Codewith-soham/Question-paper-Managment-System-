// Main program that shows the menu and calls service methods


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        QuestionPaperService service = new QuestionPaperService();

        while (true) {
            System.out.println();
            System.out.println(" QUESTION PAPER MANAGEMENT ");
            System.out.println();
            System.out.println("1. Add Question Paper");
            System.out.println("2. Search Question Paper");
            System.out.println("3. View All Question Papers");
            System.out.println("4. Delete Question Paper");
            System.out.println("5. Send Question Paper via Email");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1: service.addPaper(); break;
                case 2: service.searchPaper(); break;
                case 3: service.viewAllPapers(); break;
                case 4: service.deletePaper(); break;
                case 5: service.sendPaperByEmail(); break;
                case 6:
                    System.out.println("Goodbye!");
                    sc.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }
}
