package com.udemy.webFlux.mvc.service.impl;

import com.udemy.webFlux.mvc.MvcApplication;
import com.udemy.webFlux.mvc.models.Product;
import com.udemy.webFlux.mvc.repository.ProductRepository;
import com.udemy.webFlux.mvc.service.IProductService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Servicio de acceso al repositorio de Product
 *
 * @author Emilio
 */
@AllArgsConstructor
@Service
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    /**
     * Este método permite la creación de un producto en base de datos
     * @param product El producto que debe ser convertido en el controlador de DTO a DAO
     * @return Mono-Product
     */
    @Override
    public Mono<Product> createProduct(Product product) {
        return productRepository.save(product);
    }

    /**
     * Obtener un prodcuto dado un id. En este caso estamos utilizando dos vias. La primera es la del return.
     * Una búsequda sencilla a mono.
     * La segunda es utilizar un flux, filtrar los datos y solo obtener la primera ocurrencia.
     * @param id El id del objeto a buscar
     * @return Mono product
     */
    @Override
    public Mono<Product> getOneProduct(String id) {

        // Option 2
        /* Mono<Product> productMono = productRepository
                .findAll() // Hacemos una busqueda general.
                .filter(data -> data.getId().equals(id)) // Filtramos los datos que vengan del flujo.
                .next(); // El next hace que devuelva un valor. En este caso el primero. Miralo como la implementación de un iterador.
        */
        return productRepository.findById(id); // Option 1
    }

    /**
     * Buscar todos los productos almacenados en la colección
     * @return Flux product
     */
    @Override
    public Flux<Product> getAllProducts() {
        log.info("Realizando la operación findAll");
        return productRepository.findAll();
    }

    /**
     * Este metodo se encargará de borrar en base de datos el elemento pasado por parámetros.
     * @param id Representa el id del objeto a eliminar
     * @return Empty
     */
    @Override
    public Mono<Void> deleteProduct(String id) {
        return productRepository.deleteById(id);
    }

    /**
     * Se eliminaran varios productos de forma simultanea.
     * @param id Un arreglo que posee todos los id de los elementos a eliminar
     * @return Empty
     */
    @Override
    public Mono<Void> deleteManyProduct(List<String> id) {
        return productRepository.deleteAllById(id);
    }
}
