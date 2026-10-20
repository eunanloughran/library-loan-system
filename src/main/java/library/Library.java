package library;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads the items, users and loans from CSV files once, keeps them in memory,
 * and saves the loans file whenever a loan changes.
 */
public class Library {
    private static final String ITEMS_FILE = "items.csv";
    private static final String USERS_FILE = "users.csv";
    private static final String LOANS_FILE = "loans.csv";
    private static final String LOANS_HEADER = "Barcode,User_id,Issue_date,Due_date,Renewals";

    private final Map<String, LoanableItem> items = new LinkedHashMap<>();
    private final Map<String, User> users = new LinkedHashMap<>();
    // Keyed by barcode, since an item can only be on one loan at a time.
    private final Map<String, Loan> loans = new LinkedHashMap<>();
    private final Path loansFile;

    public Library(Path dataDir) {
        this.loansFile = dataDir.resolve(LOANS_FILE);
        for (String[] f : readCsv(dataDir.resolve(ITEMS_FILE))) {
            String barcode = f[0], creator = f[1], title = f[2], type = f[3];
            int year = Integer.parseInt(f[4]);
            LoanableItem item = type.equals("Book")
                    ? new Book(barcode, title, creator, year, f[5])
                    : new Multimedia(barcode, title, creator, year);
            items.put(barcode, item);
        }
        for (String[] f : readCsv(dataDir.resolve(USERS_FILE))) {
            users.put(f[0], new User(f[0], f[1], f[2], f[3]));
        }
        if (Files.exists(loansFile)) {
            for (String[] f : readCsv(loansFile)) {
                Loan loan = Loan.fromCsv(f);
                loans.put(loan.getBarcode(), loan);
            }
        }
    }

    public Loan issueItem(String barcode, String userId, LocalDate date) {
        if (!users.containsKey(userId)) {
            throw new LibraryException("No user found with ID " + userId + ".");
        }
        LoanableItem item = findItem(barcode)
                .orElseThrow(() -> new LibraryException("No item found with barcode " + barcode + "."));
        if (loans.containsKey(barcode)) {
            throw new LibraryException("\"" + item.getTitle() + "\" is already on loan.");
        }
        Loan loan = new Loan(barcode, userId, date, date.plusWeeks(item.getLoanWeeks()), 0);
        loans.put(barcode, loan);
        saveLoans();
        return loan;
    }

    public Loan renewLoan(String barcode) {
        Loan loan = getLoan(barcode);
        LoanableItem item = findItem(barcode)
                .orElseThrow(() -> new LibraryException("Item " + barcode + " is on loan but missing from the catalogue."));
        if (loan.getRenewals() >= item.getMaxRenewals()) {
            throw new LibraryException(item.getType() + "s can only be renewed " + item.getMaxRenewals()
                    + " times, and this loan has reached the limit.");
        }
        loan.renew(item.getRenewalWeeks());
        saveLoans();
        return loan;
    }

    /** Ends the loan and returns how many days late the item came back (0 if on time). */
    public long returnItem(String barcode, LocalDate date) {
        Loan loan = getLoan(barcode);
        loans.remove(barcode);
        saveLoans();
        return loan.daysOverdue(date);
    }

    public List<Loan> getCurrentLoans() {
        return new ArrayList<>(loans.values());
    }

    public Optional<LoanableItem> findItem(String barcode) {
        return Optional.ofNullable(items.get(barcode));
    }

    public Optional<User> findUser(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    public LoanReport generateReport() {
        int books = 0, multimedia = 0, renewedMoreThanOnce = 0;
        for (Loan loan : loans.values()) {
            if (items.get(loan.getBarcode()) instanceof Book) {
                books++;
            } else {
                multimedia++;
            }
            if (loan.getRenewals() > 1) {
                renewedMoreThanOnce++;
            }
        }
        return new LoanReport(loans.size(), books, multimedia, renewedMoreThanOnce);
    }

    private Loan getLoan(String barcode) {
        Loan loan = loans.get(barcode);
        if (loan == null) {
            throw new LibraryException("Item " + barcode + " is not on loan.");
        }
        return loan;
    }

    // Writes to a temporary file first, so a crash mid-save can't leave a half-written loans file.
    private void saveLoans() {
        List<String> lines = new ArrayList<>();
        lines.add(LOANS_HEADER);
        for (Loan loan : loans.values()) {
            lines.add(loan.toCsv());
        }
        try {
            Path temp = Files.createTempFile(loansFile.toAbsolutePath().getParent(), "loans", ".tmp");
            Files.write(temp, lines);
            Files.move(temp, loansFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save " + loansFile, e);
        }
    }

    /** Reads a CSV file and returns every row after the header. */
    private static List<String[]> readCsv(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);
            List<String[]> rows = new ArrayList<>();
            for (String line : lines.subList(Math.min(1, lines.size()), lines.size())) {
                if (!line.isBlank()) {
                    rows.add(line.split(","));
                }
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }
}
