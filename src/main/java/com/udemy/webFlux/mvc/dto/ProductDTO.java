package com.udemy.webFlux.mvc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Esta clase es la representación del objeto Producto
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductDTO {
    public String id;
    private String name;
    private Double price;
}
