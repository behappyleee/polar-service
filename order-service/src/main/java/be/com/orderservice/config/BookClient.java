package be.com.orderservice.config;

import be.com.orderservice.book.Book;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class BookClient {

    private static final String BOOKS_ROOT_API = "/books/";
    private final WebClient webClient;

    public BookClient(WebClient webClient) {
        this.webClient = webClient;
    }

    // 핵심 목표는 실패가 발생하더라도 사용자가 이를 알아채지 못하고 서비스를 계속 사용할 수 있을만큼 높은 복원력을 지닌 시스템을 설계하는 것, 이것은 최상의 경우이고
    // 이와 반대로 최악의 경우라도 시스템은 여전히 작동하지만 기능이나 성능이 우아하게 저하 되도록 해야 함
    public Mono<Book> getBookByIsbn(String isbn) {
        return webClient.get()
            .uri(BOOKS_ROOT_API + isbn)
            .retrieve()
            .bodyToMono(Book.class)
            .timeout(Duration.ofSeconds(3), Mono.empty())
            .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty())
            .retryWhen(
                Retry.backoff(3, Duration.ofSeconds(2))
            )
            .onErrorResume(WebClientResponseException.NotFound.class, exception -> Mono.empty());
    }
}

