package com.yamar.orderservice.service;

import com.yamar.orderservice.dto.OrderLineRequest;
import com.yamar.orderservice.dto.OrderRequest;
import com.yamar.orderservice.dto.OrderResponse;
import com.yamar.orderservice.exception.EntityNotFoundException;
import com.yamar.orderservice.mapper.OrderLineMapper;
import com.yamar.orderservice.mapper.OrderMapper;
import com.yamar.orderservice.model.OrderLine;
import com.yamar.orderservice.repository.OrderRepository;
import com.yamar.orderservice.utils.OrderNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderLineMapper orderLineMapper;
    private final OrderTotalCalculator totalCalculator;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // TODO: Call Customer service

        var order = orderMapper.toOrder(request);
        order.setOrderNumber(orderNumberGenerator.generate());

        List<OrderLine> orderLines = request.getProducts().stream()
                .map(product -> {
                    OrderLineRequest orderLineRequest = OrderLineRequest
                            .builder()
                            .productId(product.getProductId())
                            .quantity(product.getQuantity())
                            .pricePerUnit(BigDecimal.valueOf(10))
                            .build();

                    OrderLine orderLine = orderLineMapper.toOrderLine(orderLineRequest);
                    orderLine.setOrder(order);
                    return orderLine;
                })
                .toList();

        order.setOrderLines(orderLines);

        BigDecimal calculatedTotalAmount = totalCalculator.calculatePrices(order);
        order.setTotalAmount(calculatedTotalAmount);

        var savedOrder = orderRepository.save(order);

        // TODO: Call PaymentService
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
