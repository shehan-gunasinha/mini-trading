package com.trading.orderservice.service;


import com.trading.orderservice.dto.CreateOrderRequest;
import com.trading.orderservice.dto.OrderResponse;
import com.trading.orderservice.dto.UpdateOrderRequest;
import com.trading.orderservice.entity.Order;
import com.trading.orderservice.kafka.producer.OrderEventProducer;
import com.trading.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    //  CREATE 
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .symbol(request.getSymbol().toUpperCase())
                .side(request.getSide().toUpperCase())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order saved = orderRepository.save(order);
        orderEventProducer.sendOrderCreatedEvent(saved);  // → Kafka

        log.info("Order created: id={}, symbol={}", saved.getId(), saved.getSymbol());
        return toResponse(saved);
    }

    //  READ by ID 
    public OrderResponse getOrderById(UUID id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    //  READ all 
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //  UPDATE 
    public OrderResponse updateOrder(UUID id, UpdateOrderRequest request) {
        Order updated = orderRepository.updateStatus(id, request.getStatus().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        orderEventProducer.sendOrderUpdatedEvent(updated);  // → Kafka

        log.info("Order updated: id={}, status={}", id, request.getStatus());
        return toResponse(updated);
    }

    // DELET
    public void deleteOrder(UUID id) {
        boolean deleted = orderRepository.deleteById(id);
        if (!deleted) {
            throw new RuntimeException("Order not found: " + id);
        }
        orderEventProducer.sendOrderDeletedEvent(id.toString());  // → Kafka
        log.info("Order deleted: id={}", id);
    }

    //Mapper
    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .symbol(order.getSymbol())
                .side(order.getSide())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}