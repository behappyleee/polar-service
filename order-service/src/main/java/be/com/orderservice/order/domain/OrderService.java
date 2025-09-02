package be.com.orderservice.order.domain;

import be.com.orderservice.book.Book;
import be.com.orderservice.config.BookClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookClient bookClient;

    public OrderService(BookClient bookClient, OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.bookClient = bookClient;
    }

    public Flux<Order> getALlOrders() {
        return orderRepository.findAll();
    }

    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn)
                .map(book -> buildAcceptedOrder(book, quantity))
                .defaultIfEmpty(
                    buildRejectOrder(isbn, quantity)
                )
                .flatMap(orderRepository::save);
    }

    public static Order buildAcceptedOrder(Book book, int quantity) {
        return Order.of(book.isbn(), book.title() + " - " + book.author(), book.price(), quantity, OrderStatus.ACCEPTED);
    }

    public static Order buildRejectOrder(String bookIsbn, int quantity) {
        return Order.of(bookIsbn, null, null, quantity, OrderStatus.REJECTED);
    }
}

