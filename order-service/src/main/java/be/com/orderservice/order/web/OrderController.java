package be.com.orderservice.order.web;

import be.com.orderservice.order.domain.Order;
import be.com.orderservice.order.domain.OrderService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Flux<Order> getAllOrders() {
        return orderService.getALlOrders();
    }

    @PostMapping
    public Mono<Order> createOrder(@RequestBody OrderRequest order) {
        return orderService.submitOrder(
            order.isbn(),
            order.quantity()
        );
    }
}
