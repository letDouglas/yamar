package com.yamar.orderservice.mapper;

import com.yamar.orderservice.dto.OrderRequest;
import com.yamar.orderservice.dto.OrderResponse;
import com.yamar.orderservice.model.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {

    public Order toOrder(OrderRequest request) {
        return Order.builder()
                .customerId(request.getCustomerId())
                .paymentMethod(request.getPaymentMethod())
                .build();
    }

    public OrderResponse toDto(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getPaymentMethod()
        );
    }

}
