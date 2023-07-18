package com.udemy.webFlux.mvc.repository;

import com.udemy.webFlux.mvc.models.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
// Al extender de la interfaz ReactiveMongoRepository, podemos acceder a varios métodos predefinidos para realizar operaciones CRUD
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}
