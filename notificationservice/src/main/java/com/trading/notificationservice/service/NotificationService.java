package com.trading.notificationservice.service;

import com.trading.notificationservice.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    // ---- ORDER CREATED ----
    public void handleOrderCreated(OrderEvent event) {
        OrderEvent.OrderData order = event.getOrder();

        // In a real app: send email, push notification, SMS etc.
        // For learning: we just log it clearly
        log.info("===========================================");
        log.info("  NEW ORDER NOTIFICATION");
        log.info("===========================================");
        log.info("  Order ID  : {}", order.getId());
        log.info("  Symbol    : {}", order.getSymbol());
        log.info("  Side      : {}", order.getSide());
        log.info("  Quantity  : {}", order.getQuantity());
        log.info("  Price     : {}", order.getPrice());
        log.info("  Status    : {}", order.getStatus());
        log.info("  Created At: {}", order.getCreatedAt());
        log.info("===========================================");
    }

    // ---- ORDER UPDATED ----
    public void handleOrderUpdated(OrderEvent event) {
        OrderEvent.OrderData order = event.getOrder();

        log.info("===========================================");
        log.info("  ORDER UPDATE NOTIFICATION");
        log.info("===========================================");
        log.info("  Order ID  : {}", order.getId());
        log.info("  Symbol    : {}", order.getSymbol());
        log.info("  New Status: {}", order.getStatus());
        log.info("  Updated At: {}", order.getUpdatedAt());
        log.info("===========================================");
    }

    // ---- ORDER DELETED ----
    public void handleOrderDeleted(String orderId) {
        log.info("===========================================");
        log.info("  ORDER DELETED NOTIFICATION");
        log.info("===========================================");
        log.info("  Order ID  : {}", orderId);
        log.info("  Action    : Order has been removed");
        log.info("===========================================");
    }
}