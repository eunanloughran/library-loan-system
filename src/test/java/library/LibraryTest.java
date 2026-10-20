package library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Each test gets its own copy of a small data set in a temporary folder, so the real data is never touched. */
class LibraryTest {
    private static final String BOOK = "1001";
    private static final String CD = "2001";
    private static final String USER = "B001";
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 2);

    @TempDir
    Path dir;
    Library library;

    @BeforeEach
    void setUp() throws IOException {
        Files.write(dir.resolve("items.csv"), List.of(
                "Barcode,Author/Artist,Title,Type,Year,ISBN",
                BOOK + ",Ann Author,A Good Book,Book,2001,123-4",
                CD + ",Art Artist,A Good Album,Multimedia,1999,567-8"));
        Files.write(dir.resolve("users.csv"), List.of(
                "User_id,first_name,last_name,email",
                USER + ",Sam,Reader,sam@example.com"));
        library = new Library(dir);
    }

    @Test
    void booksAreLentForFourWeeks() {
        Loan loan = library.issueItem(BOOK, USER, TODAY);
        assertEquals(TODAY.plusWeeks(4), loan.getDueDate());
    }

    @Test
    void multimediaIsLentForOneWeek() {
        Loan loan = library.issueItem(CD, USER, TODAY);
        assertEquals(TODAY.plusWeeks(1), loan.getDueDate());
    }

    @Test
    void cannotIssueToUnknownUserOrUnknownItem() {
        assertThrows(LibraryException.class, () -> library.issueItem(BOOK, "NOBODY", TODAY));
        assertThrows(LibraryException.class, () -> library.issueItem("9999", USER, TODAY));
    }

    @Test
    void cannotIssueAnItemThatIsAlreadyOnLoan() {
        library.issueItem(BOOK, USER, TODAY);
        assertThrows(LibraryException.class, () -> library.issueItem(BOOK, USER, TODAY));
    }

    @Test
    void bookRenewalsAddTwoWeeksUpToThreeTimes() {
        library.issueItem(BOOK, USER, TODAY);
        for (int i = 1; i <= 3; i++) {
            Loan loan = library.renewLoan(BOOK);
            assertEquals(TODAY.plusWeeks(4 + 2 * i), loan.getDueDate());
            assertEquals(i, loan.getRenewals());
        }
        assertThrows(LibraryException.class, () -> library.renewLoan(BOOK));
    }

    @Test
    void multimediaRenewalsAddOneWeekUpToTwice() {
        library.issueItem(CD, USER, TODAY);
        library.renewLoan(CD);
        assertEquals(TODAY.plusWeeks(3), library.renewLoan(CD).getDueDate());
        assertThrows(LibraryException.class, () -> library.renewLoan(CD));
    }

    @Test
    void cannotRenewOrReturnAnItemThatIsNotOnLoan() {
        assertThrows(LibraryException.class, () -> library.renewLoan(BOOK));
        assertThrows(LibraryException.class, () -> library.returnItem(BOOK, TODAY));
    }

    @Test
    void returningOnTimeEndsTheLoan() {
        library.issueItem(BOOK, USER, TODAY);
        assertEquals(0, library.returnItem(BOOK, TODAY.plusDays(3)));
        assertTrue(library.getCurrentLoans().isEmpty());
    }

    @Test
    void lateItemsCanStillBeReturnedAndReportDaysLate() {
        library.issueItem(CD, USER, TODAY);
        assertEquals(5, library.returnItem(CD, TODAY.plusWeeks(1).plusDays(5)));
        assertTrue(library.getCurrentLoans().isEmpty());
    }

    @Test
    void loansAreSavedAndReloaded() {
        library.issueItem(BOOK, USER, TODAY);
        library.renewLoan(BOOK);

        Loan reloaded = new Library(dir).getCurrentLoans().get(0);
        assertEquals(BOOK, reloaded.getBarcode());
        assertEquals(USER, reloaded.getUserId());
        assertEquals(TODAY.plusWeeks(6), reloaded.getDueDate());
        assertEquals(1, reloaded.getRenewals());
    }

    @Test
    void reportCountsLoansByTypeAndRenewals() {
        library.issueItem(BOOK, USER, TODAY);
        library.issueItem(CD, USER, TODAY);
        library.renewLoan(BOOK);
        library.renewLoan(BOOK);

        LoanReport report = library.generateReport();
        assertEquals(2, report.totalLoans());
        assertEquals(1, report.bookLoans());
        assertEquals(1, report.multimediaLoans());
        assertEquals(50.0, report.renewedMoreThanOncePercent(), 0.001);
    }

    @Test
    void reportWithNoLoansIsZeroNotNaN() {
        assertEquals(0.0, library.generateReport().renewedMoreThanOncePercent());
    }

    @Test
    void searchFindsItemDetails() {
        LoanableItem item = library.findItem(BOOK).orElseThrow();
        assertEquals("A Good Book", item.getTitle());
        assertEquals("Ann Author", item.getCreator());
        assertEquals("123-4", ((Book) item).getIsbn());
        assertTrue(library.findItem("9999").isEmpty());
    }
}
