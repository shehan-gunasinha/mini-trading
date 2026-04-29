package com.trading.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Ignore unknown fields safely — future-proofing
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderEvent {

    private String eventType;   // ORDER_CREATED, ORDER_UPDATED, ORDER_DELETED
    private OrderData order;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderData {
        private String id;
        private String symbol;
        private String side;
        private BigDecimal quantity;
        private BigDecimal price;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}