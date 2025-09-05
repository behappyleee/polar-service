package be.com.orderservice.order.event;

public record OrderAcceptedMessage (
    Long orderId
) {}
