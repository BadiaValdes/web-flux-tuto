package com.udemy.webFlux.mvc.repository;

import com.udemy.webFlux.mvc.models.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoryRepository  extends ReactiveMongoRepository<Category, String> {
}
