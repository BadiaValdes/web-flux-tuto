package com.udemy.webFlux.mvc.core.converter;

import com.udemy.webFlux.mvc.dto.ProductDTO;
import com.udemy.webFlux.mvc.models.Product;

/**
 * Clase dedicada a la conversion entre Producto y ProductoDTO
 *
 * @author Emilio
 */
public class ProductConverter {

    private ProductConverter(){}

    /**
     * Convierte de producto a productoDto
     * @param product
     * @return ProductDTO
     */
    public static ProductDTO productToProductDto(Product product){
        return ProductDTO
                .builder()
                .id(product.get_id())
                .name(product.getName())
                .image(product.getImage())
                .categoryId(product.getCategoryId())
                .price(product.getPrice())
                .build();
    }

    /**
     * Convierte de productDto a product
     * @param productDTO
     * @return Product
     */
    public static Product productDtoToProduct(ProductDTO productDTO){
        return Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .categoryId(productDTO.getCategoryId())
                .build();
    }
}
