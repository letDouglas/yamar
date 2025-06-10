package com.yamar.orderservice.service;

import com.yamar.orderservice.client.inventory.BatchStockRequest;
import com.yamar.orderservice.client.inventory.InventoryClient;
import com.yamar.orderservice.client.inventory.StockRequest;
import com.yamar.orderservice.client.product.ProductBatchRequest;
import com.yamar.orderservice.client.product.ProductClient;
import com.yamar.orderservice.client.product.ProductResponse;
import com.yamar.orderservice.dto.OrderLineRequest;
import com.yamar.orderservice.dto.OrderRequest;
import com.yamar.orderservice.dto.OrderResponse;
import com.yamar.orderservice.dto.PurchasedRequest;
import com.yamar.orderservice.exception.EntityNotFoundException;
import com.yamar.orderservice.exception.InsufficientStockException;
import com.yamar.orderservice.exception.ProductNotFoundException;
import com.yamar.orderservice.kafka.OrderEventProducer;
import com.yamar.orderservice.mapper.OrderLineMapper;
import com.yamar.orderservice.mapper.OrderMapper;
import com.yamar.orderservice.model.OrderLine;
import com.yamar.orderservice.model.OrderStatus;
import com.yamar.orderservice.repository.OrderRepository;
import com.yamar.orderservice.utils.OrderNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderLineMapper orderLineMapper;
    private final OrderTotalCalculator totalCalculator;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // TODO: 1. [SYNC] Customer validation → call CustomerService (not yet implemented)
        // TODO: 2. [SYNC] Check product existence
        List<PurchasedRequest> purchasedProducts = request.getProducts();
        List<String> requestedProductIds = purchasedProducts.stream()
                .map(PurchasedRequest::getProductId)
                .toList();

        ProductBatchRequest requestedProducts = ProductBatchRequest.builder()
                .productIds(requestedProductIds)
                .build();

        List<ProductResponse> verifiedProducts = productClient.getProductsByIds(requestedProducts);
        if (verifiedProducts.isEmpty()) {
            throw new ProductNotFoundException("One or more products are not found");
        }

        Map<String, BigDecimal> productPriceMap = verifiedProducts.stream()
                .collect(Collectors.toMap(ProductResponse::id, ProductResponse::price));

        // TODO: 3. [SYNC] Verify stock availability
        List<StockRequest> stockRequests = purchasedProducts.stream()
                .map(p -> StockRequest.builder()
                        .productId(p.getProductId())
                        .quantity(p.getQuantity())
                        .build())
                .toList();

        Boolean stocksStatus = inventoryClient.checkStockForProducts(
                BatchStockRequest.builder()
                        .items(stockRequests)
                        .build()
        );
        if (Boolean.FALSE.equals(stocksStatus)) {
            throw new InsufficientStockException("One or more products are out of stock");
        }

        // Mapping + order construction
        var order = orderMapper.toOrder(request);
        order.setOrderNumber(orderNumberGenerator.generate());

        List<OrderLine> orderLines = request.getProducts().stream()
                .map(product -> {
                    OrderLineRequest orderLineRequest = OrderLineRequest.builder()
                            .productId(product.getProductId())
                            .quantity(product.getQuantity())
                            .pricePerUnit(productPriceMap.get(product.getProductId()))
                            .build();

                    OrderLine orderLine = orderLineMapper.toOrderLine(orderLineRequest);
                    orderLine.setOrder(order);
                    return orderLine;
                }).toList();

        order.setOrderLines(orderLines);

        // Calculate total and save order
        BigDecimal calculatedTotalAmount = totalCalculator.calculatePrices(order);
        order.setTotalAmount(calculatedTotalAmount);
        order.setOrderStatus(OrderStatus.PENDING);

        var savedOrder = orderRepository.save(order);

        // OrderPlaced event (Kafka or RabbitMQ) consumed by payment and inventory.
        log.info("Ordine {} salvato. Richiesta di pubblicazione dell'evento inoltrata.", savedOrder.getOrderNumber());
        orderEventProducer.sendOrderPlacedEvent(savedOrder);

        // TODO: 5. [CLEANUP] Refactor → move validations to separate services (SRP)
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
