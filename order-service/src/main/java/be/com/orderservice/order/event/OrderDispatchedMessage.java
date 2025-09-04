package be.com.orderservice.order.event;

public record OrderDispatchedMessage(
    Long orderId
) {}
