package library;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan {
    private final String barcode;
    private final String userId;
    private final LocalDate issueDate;
    private LocalDate dueDate;
    private int renewals;

    public Loan(String barcode, String userId, LocalDate issueDate, LocalDate dueDate, int renewals) {
        this.barcode = barcode;
        this.userId = userId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.renewals = renewals;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public int getRenewals() {
        return renewals;
    }

    public void renew(int weeks) {
        dueDate = dueDate.plusWeeks(weeks);
        renewals++;
    }

    /** How many days late the item is on the given date, or 0 if it isn't late. */
    public long daysOverdue(LocalDate date) {
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, date));
    }

    public String toCsv() {
        return String.join(",", barcode, userId, issueDate.toString(), dueDate.toString(), String.valueOf(renewals));
    }

    public static Loan fromCsv(String[] f) {
        return new Loan(f[0], f[1], LocalDate.parse(f[2]), LocalDate.parse(f[3]), Integer.parseInt(f[4]));
    }
}
