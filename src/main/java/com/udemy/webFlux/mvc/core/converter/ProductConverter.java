package com.udemy.webFlux.mvc.core.converter;

import com.udemy.webFlux.mvc.dto.ProductDTO;
import com.udemy.webFlux.mvc.models.Product;

public class ProductConverter {

    private ProductConverter(){}

    public static ProductDTO productToProductDto(Product product){
        return ProductDTO
                .builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build();
    }

    public static Product productDtoToProduct(ProductDTO productDTO){
        return Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .build();
    }
}
