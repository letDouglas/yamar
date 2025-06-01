package com.yamar.orderservice.repository;

import com.yamar.orderservice.model.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface OrderLineRepository extends JpaRepository<OrderLine, String> {
    List<OrderLine> findAllByOrderId(Integer orderId);

}
