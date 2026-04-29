package com.trading.orderservice.repository;

import com.trading.orderservice.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@Repository
@RequiredArgsConstructor

public class OrderRepository {

    //maps a DB row with Order Entity
    private final RowMapper<Order> orderRowMapper = (rs, rowNum) -> Order.builder()
            .id(UUID.fromString(rs.getString("id")))
            .symbol(rs.getString("symbol"))
            .side(rs.getString("side"))
            .quantity(rs.getBigDecimal("quantity"))
            .price(rs.getBigDecimal("price"))
            .status(rs.getString("status"))
            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
            .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
            .build();
    private final JdbcTemplate jdbcTemplate;

    //create
    public Order save(Order order){
        String sql= """
                INSERT INTO orders (id,symbol,side,quantity,price,status,created_at,updated_at)
                VALUES (?::uuid, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                order.getId().toString(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus(),
                Timestamp.valueOf(order.getCreatedAt()),
                Timestamp.valueOf(order.getUpdatedAt())

        );
        log.debug("Saved order: {}",order.getId());
        return order;
    }

    //read by ID
    public Optional<Order> findById(UUID id){
        String sql = "SELECT * FROM orders WHERE id = ?::UUID";
        List<Order> results = jdbcTemplate.query(sql,orderRowMapper,id.toString());
        return results.isEmpty() ? Optional.empty() :Optional.of(results.get(0));
    }

    // read all
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, orderRowMapper);
    }

    //update status
    public Optional<Order> updateStatus(UUID id, String status) {
        String sql = """
                UPDATE orders
                SET status = ?, updated_at = NOW()
                WHERE id = ?::uuid
                """;
        int rows = jdbcTemplate.update(sql, status, id.toString());
        if (rows == 0) return Optional.empty();
        return findById(id);
    }

    //delete
    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM orders WHERE id = ?::uuid";
        int rows = jdbcTemplate.update(sql, id.toString());
        return rows > 0;
    }

}
