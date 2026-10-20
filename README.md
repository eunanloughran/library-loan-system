# Library Loan System

A console app in Java for running a small library's loans. Staff can issue, renew and return books and multimedia items, see what's on loan and who has it, search the catalogue, and print a loan report. Data is stored in CSV files, so loans survive between runs.

## Loan rules

| | Book | Multimedia |
| --- | --- | --- |
| Loan length | 4 weeks | 1 week |
| Renewals allowed | 3 | 2 |
| Each renewal adds | 2 weeks | 1 week |

An item can only be on one loan at a time. Late items can still be returned, and the app says how many days late they are.

## Running it

Requires Java 17+ and Maven.

```bash
mvn compile exec:java   # start the menu (reads and writes the files in data/)
mvn test                # run the unit tests
```

## Design

| Class | Responsibility |
| --- | --- |
| `LoanableItem` | Abstract base for anything that can be lent. Each subclass sets its own loan length and renewal rules. |
| `Book`, `Multimedia` | The two item types and their rules |
| `User` | A library member |
| `Loan` | One loan: who has what, when it's due, and how many times it's been renewed |
| `Library` | Loads the CSV files, applies the loan rules and saves loans after every change |
| `LoanReport` | Totals by type and the share of loans renewed more than once |
| `LibraryApp` | The console menu. It only reads input and prints results. |

Because the loan rules live on the item classes, `Library` never checks an item's type, and adding a new kind of item means adding one class. The rules are kept separate from console input, so the tests call `Library` directly. Each test runs against its own copy of the data in a temporary folder.

## Data files

`data/items.csv`, `data/users.csv` and `data/loans.csv`. Item, user and author names are made-up sample data.
