package be.com.catalogservice.domain;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String isbn) {
        super("The Book ISBN" + isbn + " was not found");
    }
}
