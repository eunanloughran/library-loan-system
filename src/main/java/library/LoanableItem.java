package library;

/**
 * Anything the library lends out. Each kind of item sets its own loan rules,
 * so the library never has to check what type an item is.
 */
public abstract class LoanableItem {
    private final String barcode;
    private final String title;
    private final String creator;
    private final int year;

    protected LoanableItem(String barcode, String title, String creator, int year) {
        this.barcode = barcode;
        this.title = title;
        this.creator = creator;
        this.year = year;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getTitle() {
        return title;
    }

    /** The author of a book or the artist of a multimedia item. */
    public String getCreator() {
        return creator;
    }

    public int getYear() {
        return year;
    }

    public abstract String getType();

    public abstract int getLoanWeeks();

    public abstract int getMaxRenewals();

    public abstract int getRenewalWeeks();

    @Override
    public String toString() {
        return title + " by " + creator + " (" + getType() + ", " + year + ")";
    }
}
