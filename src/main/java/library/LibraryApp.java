package library;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/** The console menu. All the rules live in {@link Library}; this class only reads input and prints results. */
public class LibraryApp {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final Library library;
    private final Scanner in;

    public LibraryApp(Library library, Scanner in) {
        this.library = library;
        this.in = in;
    }

    public static void main(String[] args) {
        Path dataDir = Path.of(args.length > 0 ? args[0] : "data").toAbsolutePath();
        if (!Files.isRegularFile(dataDir.resolve("items.csv"))) {
            System.out.println("Couldn't find the data files in " + dataDir);
            System.out.println("Run the program from the project folder (the one containing data/),");
            System.out.println("or pass the data folder's path: java -cp out library.LibraryApp path/to/data");
            return;
        }
        new LibraryApp(new Library(dataDir), new Scanner(System.in)).run();
    }

    public void run() {
        System.out.println("Ulster Library loan system");
        while (true) {
            System.out.println();
            System.out.println("1. Issue item");
            System.out.println("2. Renew loan");
            System.out.println("3. Return item");
            System.out.println("4. Show current loans");
            System.out.println("5. Search for an item by barcode");
            System.out.println("6. Loan report");
            System.out.println("7. Exit");
            String choice = prompt("Choose an option: ");
            if (choice == null || choice.equals("7")) {
                System.out.println("Goodbye.");
                return;
            }
            try {
                switch (choice) {
                    case "1" -> issue();
                    case "2" -> renew();
                    case "3" -> returnItem();
                    case "4" -> showLoans();
                    case "5" -> search();
                    case "6" -> report();
                    default -> System.out.println("Please enter a number from 1 to 7.");
                }
            } catch (LibraryException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void issue() {
        String barcode = prompt("Barcode: ");
        String userId = prompt("User ID: ");
        Loan loan = library.issueItem(barcode, userId, LocalDate.now());
        System.out.println("Issued \"" + titleOf(barcode) + "\". Due back " + loan.getDueDate().format(DATE) + ".");
    }

    private void renew() {
        String barcode = prompt("Barcode: ");
        Loan loan = library.renewLoan(barcode);
        System.out.println("Renewed. Now due back " + loan.getDueDate().format(DATE) + ".");
    }

    private void returnItem() {
        String barcode = prompt("Barcode: ");
        long daysLate = library.returnItem(barcode, LocalDate.now());
        System.out.println("Returned \"" + titleOf(barcode) + "\""
                + (daysLate > 0 ? ", " + daysLate + " day(s) late." : " on time."));
    }

    private void showLoans() {
        List<Loan> loans = library.getCurrentLoans();
        if (loans.isEmpty()) {
            System.out.println("There are no items on loan.");
            return;
        }
        for (Loan loan : loans) {
            String borrower = library.findUser(loan.getUserId()).map(User::getFullName).orElse(loan.getUserId());
            long late = loan.daysOverdue(LocalDate.now());
            System.out.printf("%s, borrowed by %s, due %s%s%n",
                    library.findItem(loan.getBarcode()).map(LoanableItem::toString).orElse(loan.getBarcode()),
                    borrower, loan.getDueDate().format(DATE), late > 0 ? " (" + late + " days overdue)" : "");
        }
    }

    private void search() {
        String barcode = prompt("Barcode: ");
        LoanableItem item = library.findItem(barcode)
                .orElseThrow(() -> new LibraryException("No item found with barcode " + barcode + "."));
        System.out.println("Title:  " + item.getTitle());
        System.out.println(item instanceof Book ? "Author: " + item.getCreator() : "Artist: " + item.getCreator());
        System.out.println("Type:   " + item.getType());
        System.out.println("Year:   " + item.getYear());
        if (item instanceof Book book) {
            System.out.println("ISBN:   " + book.getIsbn());
        }
    }

    private void report() {
        LoanReport r = library.generateReport();
        System.out.println("Loan report");
        System.out.println("Total loans:      " + r.totalLoans());
        System.out.println("Book loans:       " + r.bookLoans());
        System.out.println("Multimedia loans: " + r.multimediaLoans());
        System.out.printf("Renewed more than once: %.1f%%%n", r.renewedMoreThanOncePercent());
    }

    private String titleOf(String barcode) {
        return library.findItem(barcode).map(LoanableItem::getTitle).orElse(barcode);
    }

    private String prompt(String message) {
        System.out.print(message);
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }
}
