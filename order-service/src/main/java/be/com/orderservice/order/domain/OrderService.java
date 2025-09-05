package be.com.orderservice.order.domain;

import be.com.orderservice.book.Book;
import be.com.orderservice.config.BookClient;
import be.com.orderservice.order.event.OrderAcceptedMessage;
import be.com.orderservice.order.event.OrderDispatchedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final BookClient bookClient;
    private final StreamBridge streamBridge;

    public OrderService(BookClient bookClient, OrderRepository orderRepository, StreamBridge streamBridge) {
        this.orderRepository = orderRepository;
        this.bookClient = bookClient;
        this.streamBridge = streamBridge;
    }

    public Flux<Order> getALlOrders() {
        return orderRepository.findAll();
    }

//    public Mono<Order> submitOrder(String isbn, int quantity) {
//        return bookClient.getBookByIsbn(isbn)
//                .map(book -> buildAcceptedOrder(book, quantity))
//                .defaultIfEmpty(
//                    buildRejectOrder(isbn, quantity)
//                )
//                .flatMap(orderRepository::save);
//    }

    public static Order buildAcceptedOrder(Book book, int quantity) {
        return Order.of(book.isbn(), book.title() + " - " + book.author(), book.price(), quantity, OrderStatus.ACCEPTED);
    }

    public static Order buildRejectOrder(String bookIsbn, int quantity) {
        return Order.of(bookIsbn, null, null, quantity, OrderStatus.REJECTED);
    }

    public Flux<Order> consumeOrderDispatchedEvent(Flux<OrderDispatchedMessage> flux) {
        return flux.flatMap(message -> orderRepository.findById(message.orderId())
        ).map(this::buildDispatchedOrder).flatMap(orderRepository::save);
    }

    private Order buildDispatchedOrder(Order order) {
        return new Order(
            order.id(),
            order.bookIsbn(),
            order.bookName(),
            order.bookPrice(),
            order.quantity(),
            OrderStatus.DISPATCHED,
            order.createdDate(),
            order.lastModifiedDate(),
            order.version()
        );
    }

    // 주문을 데이터 베이스에 저장 후 주문이 접수 되면 이벤트를 발행 -> 한 트랜잭션으로 묶어준다.
    @Transactional
    public Mono<Order> submitOrder(String isbn, int quantity) {
        return bookClient.getBookByIsbn(isbn)
                .map(book -> buildAcceptedOrder(book, quantity))
                .defaultIfEmpty(buildRejectOrder(isbn, quantity))
                .flatMap(orderRepository::save)
                .doOnNext(this::publishOrderAcceptedEvent);
    }

    private void publishOrderAcceptedEvent(Order order) {
        // 주문의 상태가 ACCEPTED 가 아니면 이벤트를 발행하지 않음
        if (!order.status().equals(OrderStatus.ACCEPTED)) {
            return;
        }

        var orderAcceptedMessage = new OrderAcceptedMessage(order.id());
        log.info("Publishing the order accepted event for the order with id {}", order.id());

        var result = streamBridge.send("acceptedOrder-out-0", orderAcceptedMessage);
        log.info("Result of sending data for order with id {} : {}", order.id(), result);
    }
}

