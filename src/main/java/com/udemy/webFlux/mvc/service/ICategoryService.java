package com.udemy.webFlux.mvc.service;

import com.udemy.webFlux.mvc.models.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Category Service Interface
 *
 * En esta interfaz declaramos la estructura que seguirá nuestro Category Service
 *
 * @author Emilio
 */
public interface ICategoryService {
    Mono<Category> createCategory(Category category); // Mono debido a que solo devolveremos un valor
    Mono<Category> getOneCategory(String id); // Mono debido a que devolveremos un solo valor
    Flux<Category> getAllCategories(); // Aquí flux para devolver los valores de forma asíncrona. Si le ponemos Mono, devemos hacer un collect al final.
    Mono<Void> deleteCategory(String id); // En este caso vamos a probar devolver un Mono.Empty
    Mono<Void> deleteManyCategory(List<String> id); // En este caso borraremos varios a la vez.
}
