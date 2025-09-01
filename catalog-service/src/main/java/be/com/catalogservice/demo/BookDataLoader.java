package be.com.catalogservice.demo;

import be.com.catalogservice.domain.Book;
import be.com.catalogservice.domain.BookRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("testdata")
@Component
public class BookDataLoader {
    private final BookRepository bookRepository;

    public BookDataLoader(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadBookTestData() {
        String isbn10First = String.format("%010d", (long)(Math.random() * 1_000_000_0000L));
        String isbn10Second = String.format("%010d", (long)(Math.random() * 1_000_000_0000L));

        var book1 = Book.of(isbn10First, "Northern Lights", "Lyra Silverstar", 9.90, "Publisher1");
        var book2 = Book.of(isbn10Second, "Polar Journey", "Iorek Ploarson", 12.90, "Publisher2");

        bookRepository.saveAll(List.of(book1, book2));
    }
}
