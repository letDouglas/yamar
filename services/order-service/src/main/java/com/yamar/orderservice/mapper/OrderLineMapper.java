package com.yamar.orderservice.mapper;

import com.yamar.orderservice.dto.OrderLineRequest;
import com.yamar.orderservice.dto.OrderLineResponse;
import com.yamar.orderservice.model.OrderLine;
import org.springframework.stereotype.Service;

@Service
public class OrderLineMapper {

    public OrderLineResponse toOrderLineResponse(OrderLine orderLine) {
        return new OrderLineResponse(
                orderLine.getId(),
                orderLine.getQuantity(),
                orderLine.getProductId(),
                orderLine.getPricePerUnit(),
                orderLine.getSubTotal()
        );
    }

    public OrderLine toOrderLine(OrderLineRequest request) {
        return OrderLine.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .pricePerUnit(request.getPricePerUnit())
                .build();
    }
}
