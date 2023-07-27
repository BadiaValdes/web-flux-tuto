package com.udemy.webFlux.mvc.dto;

import com.udemy.webFlux.mvc.models.Category;
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
    private String id;
    private String name;
    private Double price;
    private String categoryId;
    private Category category;
    private String image;
}
