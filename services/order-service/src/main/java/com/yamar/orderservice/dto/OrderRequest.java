package com.yamar.orderservice.dto;

import com.yamar.orderservice.model.PaymentMethod;
import lombok.*;

import jakarta.validation.constraints.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    private Integer id;

    @NotNull(message = "Customer should be present")
    @NotEmpty(message = "Customer should be present")
    @NotBlank(message = "Customer should be present")
    private String customerId;

    private String orderNumber;

    @NotNull(message = "Payment method should be precised")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "You should at least purchase one product")
    private List<PurchasedRequest> products;
}
