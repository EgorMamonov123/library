package lab.library.exceptions;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super("Book with id " + id + " not found");
    }

    public BookNotFoundException(String isbn) {
        super("Book with ISBN " + isbn + " not found");
    }

    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}