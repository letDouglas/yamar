package com.yamar.orderservice.mapper;

import com.yamar.orderservice.client.user.AddressResponse;
import com.yamar.orderservice.dto.OrderAddressDto;
import com.yamar.orderservice.dto.OrderRequest;
import com.yamar.orderservice.dto.OrderResponse;
import com.yamar.orderservice.model.Address;
import com.yamar.orderservice.model.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {

    public Order toOrder(OrderRequest request) {
        return Order.builder()
                .customerId(request.getCustomerId())
                .customerEmail(request.getCustomerEmail())
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

    public Address toEntity(OrderAddressDto dto) {
        if (dto == null) return null;
        return Address.builder()
                .street(dto.getStreet())
                .city(dto.getCity())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .build();
    }

    public Address toEntity(AddressResponse response) {
        if (response == null) return null;
        return Address.builder()
                .street(response.street())
                .city(response.city())
                .zipCode(response.zipCode())
                .country(response.country())
                .build();
    }

}
