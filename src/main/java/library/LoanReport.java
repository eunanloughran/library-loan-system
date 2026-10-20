package library;

public record LoanReport(int totalLoans, int bookLoans, int multimediaLoans, int renewedMoreThanOnce) {

    public double renewedMoreThanOncePercent() {
        return totalLoans == 0 ? 0 : 100.0 * renewedMoreThanOnce / totalLoans;
    }
}
