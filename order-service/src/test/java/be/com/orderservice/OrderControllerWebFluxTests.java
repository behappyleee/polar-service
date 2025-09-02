package be.com.orderservice;

import be.com.orderservice.order.domain.Order;
import be.com.orderservice.order.domain.OrderService;
import be.com.orderservice.order.domain.OrderStatus;
import be.com.orderservice.order.web.OrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebFluxTest(controllers = OrderControllerWebFluxTests.class)
public class OrderControllerWebFluxTests {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private OrderService orderService;

    @Test
    void whenBookNotAvailableThenRejectOrder() {
        var orderRequest = new OrderRequest("1234567890", 3);
        var expectedOrder = OrderService.buildRejectOrder(orderRequest.isbn(), orderRequest.quantity());

        given(
            orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity())
        ).willReturn(Mono.just(expectedOrder));

        webClient
            .post()
            .uri("/orders")
                .bodyValue(orderRequest)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Order.class).value(actualOrder -> {
                    assertThat(actualOrder.status()).isEqualTo(expectedOrder.status());
                    assertThat(actualOrder.status()).isEqualTo(OrderStatus.REJECTED);
                });
    }
}

