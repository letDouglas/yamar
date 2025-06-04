package com.yamar.inventoryservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "productId cannot be blank")
    private String productId;

    @NotNull(message = "quantity cannot be null")
    @Min(value = 0, message = "quantity must be >= 0")
    private Integer quantity;

    @NotNull(message = "threshold cannot be null")
    @Min(value = 0, message = "threshold must be >= 0")
    private Integer threshold;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "status cannot be null")
    private InventoryStatus status;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
}
