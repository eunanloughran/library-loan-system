package library;

/** A request the library can't carry out, with a message that can be shown to the user. */
public class LibraryException extends RuntimeException {
    public LibraryException(String message) {
        super(message);
    }
}
