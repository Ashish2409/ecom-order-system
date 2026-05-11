package com.ashish.ecom.product_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @Size(min = 2, max = 200)
    private String name;

    @Size(max = 5000)
    private String description;

    @Size(max = 100)
    private String category;

    @DecimalMin(value = "0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal price;

    @Min(0) @Max(1_000_000)
    private Integer stock;

    private Boolean active;
}