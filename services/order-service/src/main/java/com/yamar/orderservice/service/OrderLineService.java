package com.yamar.orderservice.service;

import com.yamar.orderservice.dto.OrderLineRequest;
import com.yamar.orderservice.dto.OrderLineResponse;
import com.yamar.orderservice.mapper.OrderLineMapper;
import com.yamar.orderservice.repository.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderLineService {

    private final OrderLineRepository repository;
    private final OrderLineMapper mapper;

    public OrderLineResponse saveOrderLine(OrderLineRequest request) {
        var orderline = mapper.toOrderLine(request);
        var savedOrderLine = repository.save(orderline);
        return mapper.toOrderLineResponse(savedOrderLine);
    }

    public List<OrderLineResponse> findAllByOrderId(Integer orderId) {
        return repository.findAllByOrderId(orderId)
                .stream()
                .map(mapper::toOrderLineResponse)
                .collect(Collectors.toList());
    }
}
