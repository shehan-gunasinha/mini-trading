package com.trading.orderservice.controller;

import com.trading.orderservice.dto.CreateOrderRequest;
import com.trading.orderservice.dto.OrderResponse;
import com.trading.orderservice.dto.UpdateOrderRequest;
import com.trading.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CacheManager cacheManager;

    // GET cache status
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {

        Map<String, Object> stats = new HashMap<>();

        // ===== ORDERS CACHE =====
        var springCache = cacheManager.getCache("orders");

        if (springCache != null) {
            var cache = (org.infinispan.Cache<?, ?>) springCache.getNativeCache();
            var statsObj = cache.getAdvancedCache().getStats();

            Map<String, Object> orderStats = new HashMap<>();

            orderStats.put("size", cache.size());
            orderStats.put("hits", statsObj.getHits());
            orderStats.put("misses", statsObj.getMisses());
            orderStats.put("stores", statsObj.getStores());
            orderStats.put("evictions", statsObj.getEvictions());

            //calculate hit ratio manually
            long hits = statsObj.getHits();
            long misses = statsObj.getMisses();
            double hitRatio = (hits + misses) == 0 ? 0 : (double) hits / (hits + misses);

            orderStats.put("hitRatio", hitRatio);

            stats.put("orders", orderStats);
        }

        //ORDERS LIST CACHE
        var listSpringCache = cacheManager.getCache("orders-list");

        if (listSpringCache != null) {
            var cache = (org.infinispan.Cache<?, ?>) listSpringCache.getNativeCache();
            var statsObj = cache.getAdvancedCache().getStats();

            Map<String, Object> listStats = new HashMap<>();

            listStats.put("size", cache.size());
            listStats.put("hits", statsObj.getHits());
            listStats.put("misses", statsObj.getMisses());

            long hits = statsObj.getHits();
            long misses = statsObj.getMisses();
            double hitRatio = (hits + misses) == 0 ? 0 : (double) hits / (hits + misses);

            listStats.put("hitRatio", hitRatio);

            stats.put("orders-list", listStats);
        }

        return ResponseEntity.ok(stats);
    }

    // POST
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET by id
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // GET all
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable UUID id,
            @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}