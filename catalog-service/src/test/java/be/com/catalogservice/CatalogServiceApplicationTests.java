package be.com.catalogservice;

import be.com.catalogservice.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class CatalogServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void whenPostRequestThenBookCreated() {
        var extectedBook = Book.of("1111111111", "Title", "Author", 9.90, "Publisher");

        webTestClient
            .post()
            .uri("/books")
            .bodyValue(extectedBook)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Book.class).value(actualBook -> {
               assertThat(actualBook).isNotNull();
               assertThat(actualBook.isbn())
                       .isEqualTo(extectedBook.isbn());
            });
    }

    @Test
    void contextLoads() {
    }
}
