package com.yamar.orderservice.service;

import com.yamar.orderservice.dto.OrderRequest;
import com.yamar.orderservice.dto.OrderResponse;
import com.yamar.orderservice.exception.EntityNotFoundException;
import com.yamar.orderservice.mapper.OrderMapper;
import com.yamar.orderservice.model.Order;
import com.yamar.orderservice.repository.OrderRepository;
import com.yamar.orderservice.utils.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderNumberGenerator orderNumberGenerator;

    public OrderResponse createOrder(OrderRequest request) {
        //1 TODO - Validate customer
        var order = orderMapper.toOrder(request);

        //2 TODO - Check inventory for products
        //3 TODO - Implement Orderline saving logic

        //4 TODO - Implement calculate total amount
        var savedOrder = orderRepository.save(order);
        savedOrder.setOrderNumber(orderNumberGenerator.generate());

        //5 TODO - Create payment by calling Payment Service
        return orderMapper.toDto(savedOrder);
    }

    public List<OrderResponse> findAllOrders() {
        return this.orderRepository.findAll()
                .stream()
                .map(this.orderMapper::toDto)
                .toList();
    }

    public OrderResponse findById(Long id) {
        return this.orderRepository.findById(id)
                .map(this.orderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", id)));
    }
}
