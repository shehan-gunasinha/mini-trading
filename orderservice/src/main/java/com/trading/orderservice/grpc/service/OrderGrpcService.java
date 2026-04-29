package com.trading.orderservice.grpc.service;

import com.trading.orderservice.dto.CreateOrderRequest;
import com.trading.orderservice.dto.UpdateOrderRequest;
import com.trading.orderservice.dto.OrderResponse;
import com.trading.orderservice.grpc.*;
import com.trading.orderservice.service.OrderService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    private final OrderService orderService;

    // ---- CREATE ----
    @Override
    public void createOrder(
            com.trading.orderservice.grpc.CreateOrderRequest request,
            StreamObserver<com.trading.orderservice.grpc.OrderResponse> responseObserver) {
        try {
            CreateOrderRequest dto = new CreateOrderRequest();
            dto.setSymbol(request.getSymbol());
            dto.setSide(request.getSide());
            dto.setQuantity(BigDecimal.valueOf(request.getQuantity()));
            dto.setPrice(BigDecimal.valueOf(request.getPrice()));

            OrderResponse result = orderService.createOrder(dto);
            responseObserver.onNext(toProto(result));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC createOrder failed", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    // ---- GET by ID ----
    @Override
    public void getOrder(
            GetOrderRequest request,
            StreamObserver<com.trading.orderservice.grpc.OrderResponse> responseObserver) {
        try {
            OrderResponse result = orderService.getOrderById(UUID.fromString(request.getId()));
            responseObserver.onNext(toProto(result));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC getOrder failed: id={}", request.getId(), e);
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    // ---- GET ALL ----
    @Override
    public void getAllOrders(
            GetAllOrdersRequest request,
            StreamObserver<OrderListResponse> responseObserver) {
        try {
            List<com.trading.orderservice.grpc.OrderResponse> protoList =
                    orderService.getAllOrders().stream()
                            .map(this::toProto)
                            .toList();

            OrderListResponse listResponse = OrderListResponse.newBuilder()
                    .addAllOrders(protoList)
                    .build();

            responseObserver.onNext(listResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC getAllOrders failed", e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    // ---- UPDATE ----
    @Override
    public void updateOrder(
            com.trading.orderservice.grpc.UpdateOrderRequest request,
            StreamObserver<com.trading.orderservice.grpc.OrderResponse> responseObserver) {
        try {
            UpdateOrderRequest dto = new UpdateOrderRequest();
            dto.setStatus(request.getStatus());

            OrderResponse result = orderService.updateOrder(UUID.fromString(request.getId()), dto);
            responseObserver.onNext(toProto(result));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC updateOrder failed: id={}", request.getId(), e);
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    // ---- DELETE ----
    @Override
    public void deleteOrder(
            DeleteOrderRequest request,
            StreamObserver<DeleteOrderResponse> responseObserver) {
        try {
            orderService.deleteOrder(UUID.fromString(request.getId()));
            DeleteOrderResponse response = DeleteOrderResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Order deleted successfully")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC deleteOrder failed: id={}", request.getId(), e);
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    // ---- DTO → Proto mapper ----
    private com.trading.orderservice.grpc.OrderResponse toProto(OrderResponse dto) {
        return com.trading.orderservice.grpc.OrderResponse.newBuilder()
                .setId(dto.getId().toString())
                .setSymbol(dto.getSymbol())
                .setSide(dto.getSide())
                .setQuantity(dto.getQuantity().doubleValue())
                .setPrice(dto.getPrice().doubleValue())
                .setStatus(dto.getStatus())
                .setCreatedAt(dto.getCreatedAt().toString())
                .build();
    }
}