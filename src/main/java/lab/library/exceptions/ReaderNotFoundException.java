package lab.library.exceptions;

public class ReaderNotFoundException extends RuntimeException {

    public ReaderNotFoundException(Long id) {
        super("Reader with ID " + id + " not found");
    }

    public ReaderNotFoundException(String message) {
        super(message);
    }
}