package com.yamar.inventoryservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchStockRequest {

    @NotEmpty(message = "Stock request list cannot be empty")
    @Valid
    private List<StockRequest> items;
}
