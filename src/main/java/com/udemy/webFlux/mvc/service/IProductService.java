package com.udemy.webFlux.mvc.service;

import com.udemy.webFlux.mvc.models.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Product Service Interface
 *
 * En esta interfaz declaramos la estructura que seguirá nuestro Product Service
 *
 * @author Emilio
 */
public interface IProductService {
    Mono<Product> createProduct(Product product); // Mono debido a que solo devolveremos un valor
    Mono<Product> getOneProduct(String id); // Mono debido a que devolveremos un solo valor
    Flux<Product> getAllProducts(); // Aquí flux para devolver los valores de forma asíncrona. Si le ponemos Mono, devemos hacer un collect al final.
    Mono<Void> deleteProduct(String id); // En este caso vamos a probar devolver un Mono.Empty
    Mono<Void> deleteManyProduct(List<String> id); // En este caso borraremos varios a la vez.
}
