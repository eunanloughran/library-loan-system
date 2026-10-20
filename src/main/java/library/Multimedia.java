package library;

/** A CD, DVD or similar: lent for 1 week and renewable twice, 1 week each time. */
public class Multimedia extends LoanableItem {

    public Multimedia(String barcode, String title, String artist, int year) {
        super(barcode, title, artist, year);
    }

    @Override
    public String getType() {
        return "Multimedia";
    }

    @Override
    public int getLoanWeeks() {
        return 1;
    }

    @Override
    public int getMaxRenewals() {
        return 2;
    }

    @Override
    public int getRenewalWeeks() {
        return 1;
    }
}
