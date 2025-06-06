package com.yamar.orderservice.service;

import com.yamar.orderservice.model.Order;
import com.yamar.orderservice.model.OrderLine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class OrderTotalCalculator {

    public BigDecimal calculateSubTotal(OrderLine orderLine) {
        if (orderLine.getPricePerUnit() == null || orderLine.getQuantity() <= 0) {
            return BigDecimal.ZERO;
        }
        return orderLine.getPricePerUnit()
                .multiply(BigDecimal.valueOf(orderLine.getQuantity()));
    }

    public BigDecimal calculateTotalAmount(List<OrderLine> orderLines) {
        return orderLines.stream()
                .map(this::calculateSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculatePrices(Order order) {
        order.getOrderLines().forEach(line ->
                line.setSubTotal(calculateSubTotal(line))
        );

        return calculateTotalAmount(order.getOrderLines())
                .setScale(2, RoundingMode.HALF_UP);
    }
}