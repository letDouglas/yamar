package com.yamar.orderservice.dto;

import com.yamar.orderservice.model.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    /**
     * If null, the order is treated as a guest order
     * or the customer id is resolved from the security token.
     */
    private String customerId;

    /**
     * Can be provided in the request payload or
     * resolved from the authenticated user token.
     */
    private String customerEmail;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "You should at least purchase one product")
    private List<PurchasedRequest> products;

    /**
     * Used when the customer is not logged in
     * or does not have a saved address profile.
     */
    private OrderAddressDto shippingAddress;

    /**
     * Falls back to shipping address if not provided.
     */
    private OrderAddressDto billingAddress;
}
