package com.udemy.webFlux.mvc.service.impl;

import com.udemy.webFlux.mvc.models.Category;
import com.udemy.webFlux.mvc.repository.CategoryRepository;
import com.udemy.webFlux.mvc.service.ICategoryService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Servicio de acceso al repositorio de Category
 *
 * @author Emilio
 */
@AllArgsConstructor
@Service
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    /**
     * Este método permite la creación de un categoryo en base de datos
     * @param category El categoryo que debe ser convertido en el controlador de DTO a DAO
     * @return Mono-Category
     */
    @Override
    public Mono<Category> createCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Obtener un prodcuto dado un id. En este caso estamos utilizando dos vias. La primera es la del return.
     * Una búsequda sencilla a mono.
     * La segunda es utilizar un flux, filtrar los datos y solo obtener la primera ocurrencia.
     * @param id El id del objeto a buscar
     * @return Mono category
     */
    @Override
    public Mono<Category> getOneCategory(String id) {

        // Option 2
        /* Mono<Category> categoryMono = categoryRepository
                .findAll() // Hacemos una busqueda general.
                .filter(data -> data.getId().equals(id)) // Filtramos los datos que vengan del flujo.
                .next(); // El next hace que devuelva un valor. En este caso el primero. Miralo como la implementación de un iterador.
        */
        return categoryRepository.findById(id); // Option 1
    }

    /**
     * Buscar todos los categoryos almacenados en la colección
     * @return Flux category
     */
    @Override
    public Flux<Category> getAllCategories() {
        log.info("Realizando la operación findAll");
        return categoryRepository.findAll();
    }

    /**
     * Este metodo se encargará de borrar en base de datos el elemento pasado por parámetros.
     * @param id Representa el id del objeto a eliminar
     * @return Empty
     */
    @Override
    public Mono<Void> deleteCategory(String id) {
        return categoryRepository.deleteById(id);
    }

    /**
     * Se eliminaran varios categoryos de forma simultanea.
     * @param id Un arreglo que posee todos los id de los elementos a eliminar
     * @return Empty
     */
    @Override
    public Mono<Void> deleteManyCategory(List<String> id) {
        return categoryRepository.deleteAllById(id);
    }
}
