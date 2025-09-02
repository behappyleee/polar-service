package be.com.orderservice;

import be.com.orderservice.book.Book;
import be.com.orderservice.config.BookClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

public class BookClientTests {
    private MockWebServer mockWebServer;
    private BookClient bookClient;

    @BeforeEach
    public void setup() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        var baseUrl = mockWebServer.url("/").toString();
        var webClient = org.springframework.web.reactive.function.client.WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        bookClient = new BookClient(webClient);
    }

    @Test
    public void whenBookExistsThenReturnBook() {
        // Given
        String bookIsbn = "1234567890";
        var mockResponse = new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                    {
                        "isbn": %s,
                        "title": "Title",
                        "author": "Author",
                        "price": 9.90,
                        "publisher": "Polarsophia"
                    }    
                """.formatted(bookIsbn));

        mockWebServer.enqueue(mockResponse);
        Mono<Book> book = bookClient.getBookByIsbn(bookIsbn);

        StepVerifier.create(book)
            .expectNextMatches(b -> b.isbn().equals(bookIsbn))
            .verifyComplete();
    }

    @AfterEach
    public void cleanup() throws IOException {
        this.mockWebServer.shutdown();
    }
}

