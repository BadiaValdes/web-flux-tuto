package com.udemy.webFlux.mvc.service;

import com.udemy.webFlux.mvc.dto.ProductDTO;
import com.udemy.webFlux.mvc.models.Product;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Product Service Interface
 *
 * En esta interfaz declaramos la estructura que seguirá nuestro Product Service
 *
 * @author Emilio
 */
public interface IProductService {
    Mono<Product> createProduct(Product product, FilePart file); // Mono debido a que solo devolveremos un valor
    Mono<Product> updateProduct(Product product); // Mono debido a que solo devolveremos un valor
    Mono<Product> getOneProduct(String id); // Mono debido a que devolveremos un solo valor
    Flux<ProductDTO> getAllProducts(); // Aquí flux para devolver los valores de forma asíncrona. Si le ponemos Mono, devemos hacer un collect al final.
    Mono<Void> deleteProduct(String id); // En este caso vamos a probar devolver un Mono.Empty
    Mono<Void> deleteManyProduct(List<String> id); // En este caso borraremos varios a la vez.
}
