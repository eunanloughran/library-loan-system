package library;

/** A book: lent for 4 weeks and renewable 3 times, 2 weeks each time. */
public class Book extends LoanableItem {
    private final String isbn;

    public Book(String barcode, String title, String author, int year, String isbn) {
        super(barcode, title, author, year);
        this.isbn = isbn;
    }

    public String getIsbn() {
        return isbn;
    }

    @Override
    public String getType() {
        return "Book";
    }

    @Override
    public int getLoanWeeks() {
        return 4;
    }

    @Override
    public int getMaxRenewals() {
        return 3;
    }

    @Override
    public int getRenewalWeeks() {
        return 2;
    }
}
