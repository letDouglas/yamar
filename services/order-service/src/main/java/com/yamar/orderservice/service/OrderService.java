package com.yamar.orderservice.service;

import com.yamar.orderservice.client.inventory.BatchStockRequest;
import com.yamar.orderservice.client.inventory.InventoryClient;
import com.yamar.orderservice.client.inventory.StockRequest;
import com.yamar.orderservice.client.product.ProductBatchRequest;
import com.yamar.orderservice.client.product.ProductClient;
import com.yamar.orderservice.client.product.ProductResponse;
import com.yamar.orderservice.client.user.AddressType;
import com.yamar.orderservice.client.user.UserClient;
import com.yamar.orderservice.client.user.UserResponse;
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
import com.yamar.orderservice.model.Order;
import com.yamar.orderservice.model.OrderLine;
import com.yamar.orderservice.model.OrderStatus;
import com.yamar.orderservice.repository.OrderRepository;
import com.yamar.orderservice.utils.OrderNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final UserClient userClient;
    private final OrderEventProducer orderEventProducer;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {

        // TODO: 1. [UPDATED/COMPLETED] Customer validation → Handled via populateUserInfo (Supports Guest & User via Feign)
        populateUserInfo(request);

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

        // TODO: 3. [SYNC] Verify stock availability (Note: Stock is NOT decremented here, only checked)
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
        order.setOrderStatus(OrderStatus.PENDING);

        // TODO: 4. [NEW/COMPLETED] Data Integrity → Populate Address Snapshot (Shipping/Billing)
        populateAddressSnapshot(order, request);

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

        var savedOrder = orderRepository.save(order);

        // TODO: 5. [ASYNC] Publish Event → Inventory Service must consume this to decrement stock!
        log.info("Order {} saved. Event publication request forwarded.", savedOrder.getOrderNumber());
        orderEventProducer.sendOrderPlacedEvent(savedOrder);

        // TODO: 6. [MISSING] Payment Integration → Need PaymentService to switch status from PENDING to PAID
        // TODO: 7. [CLEANUP] Refactor → move validations to separate services (SRP)

        return orderMapper.toDto(savedOrder);
    }

    /**
     * Identifies whether the user is Guest or Logged in.
     * If logged in, fetches the profile from User Service.
     */
    private void populateUserInfo(OrderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            // LOGGED USER SCENARIO
            String userId = jwt.getSubject();
            log.info("Authenticated user detected: {}", userId);

            try {
                UserResponse userProfile = userClient.getCurrentUser();
                request.setCustomerId(userId);
                request.setCustomerEmail(userProfile.email());
            } catch (Exception e) {
                log.error("Failed to fetch user profile for ID: {}", userId, e);
                throw new EntityNotFoundException("Could not verify user identity");
            }

        } else {
            // GUEST USER SCENARIO
            log.info("Guest checkout detected");
            if (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank()) {
                throw new IllegalArgumentException("Email is required for guest checkout");
            }
            request.setCustomerId(null);
        }
    }

    /**
     * Handles the Address Snapshot logic.
     * Priority: 1. Request Body (Guest/Override) -> 2. User Profile (Logged fallback)
     */
    private void populateAddressSnapshot(Order order, OrderRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. JSON input takes precedence
        if (request.getShippingAddress() != null) {
            order.setShippingAddress(orderMapper.toEntity(request.getShippingAddress()));
        }
        if (request.getBillingAddress() != null) {
            order.setBillingAddress(orderMapper.toEntity(request.getBillingAddress()));
        }

        // 2. Fallback to User Profile if logged in and JSON is missing
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            try {
                UserResponse profile = userClient.getCurrentUser();

                if (order.getShippingAddress() == null) {
                    profile.addresses().stream()
                            .filter(a -> a.type() == AddressType.SHIPPING)
                            .findFirst()
                            .ifPresent(a -> order.setShippingAddress(orderMapper.toEntity(a)));
                }

                if (order.getBillingAddress() == null) {
                    profile.addresses().stream()
                            .filter(a -> a.type() == AddressType.BILLING || a.type() == AddressType.SHIPPING)
                            .findFirst()
                            .ifPresent(a -> order.setBillingAddress(orderMapper.toEntity(a)));
                }
            } catch (Exception e) {
                log.warn("Could not fetch user addresses for snapshot backup", e);
            }
        }

        // 3. Final validation
        if (order.getShippingAddress() == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        // Clone shipping address if billing is missing
        if (order.getBillingAddress() == null) {
            order.setBillingAddress(order.getShippingAddress());
        }
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
