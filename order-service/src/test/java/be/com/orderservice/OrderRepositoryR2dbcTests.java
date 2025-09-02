package be.com.orderservice;

import be.com.orderservice.config.DataConfig;
import be.com.orderservice.order.domain.OrderRepository;
import be.com.orderservice.order.domain.OrderService;
import be.com.orderservice.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import(DataConfig.class)
@Testcontainers
public class OrderRepositoryR2dbcTests {

    @Container
    static PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));

    @Autowired
    private OrderRepository orderRepository;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", OrderRepositoryR2dbcTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgresql.withUsername("user")::getUsername);
        registry.add("spring.r2dbc.password", postgresql.withPassword("password")::getPassword);
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
    }

    private static String r2dbcUrl() {
        return String.format(
            "r2dbc:postgresql://%s:%d/%s",
            postgresql.getHost(),
            postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
            postgresql.getDatabaseName()
        );
    }

    @Test
    public void createRejectedOrder() {
        var rejectedOrder = OrderService.buildRejectOrder("1234567890", 3);
        StepVerifier
            .create(orderRepository.save(rejectedOrder))
            .expectNextMatches(
                order -> order.status().equals(OrderStatus.REJECTED)
            )
            .verifyComplete();
    }
}

