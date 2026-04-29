package com.trading.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    private String symbol;
    private String side;
    private BigDecimal quantity;
    private BigDecimal price;
}
