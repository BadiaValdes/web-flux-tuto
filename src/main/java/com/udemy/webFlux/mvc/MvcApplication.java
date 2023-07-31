package com.udemy.webFlux.mvc;

import com.udemy.webFlux.mvc.core.constant.CollectionNames;
import com.udemy.webFlux.mvc.models.Category;
import com.udemy.webFlux.mvc.models.Product;
import com.udemy.webFlux.mvc.repository.ProductRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.AllArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;


@SpringBootApplication
@EnableReactiveMongoAuditing // Nos permite utilizar las anotaciones de CreationDate
@AllArgsConstructor
@OpenAPIDefinition(info = @Info(
        title = "Spring WebFlux Tutorial",
        version = "1.0",
        description = "Spring WebFlux CRUD Example"
))
public class MvcApplication implements CommandLineRunner { // CommandLineRunner permite la implementación del método run que se ejecuta al inicio del sistema

    private final ProductRepository productRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private static final Logger log = LoggerFactory.getLogger(MvcApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MvcApplication.class, args);
    }

    @Override
    // Dentro de este método, se pueden definir todos los comandos que queramos ejecutar una vez inicializada la aplicación.
    public void run(String... args) throws Exception {
        reactiveMongoTemplate.dropCollection(CollectionNames.PRODUCT_COLLECTION).subscribe();
        reactiveMongoTemplate.dropCollection(CollectionNames.CATEGORY_COLLECTION).subscribe();

        Flux<Category> categoryFlux = Flux.just(
                Category.builder().name("Games").build(),
                Category.builder().name("Grocery").build(),
                Category.builder().name("Computer").build()
        );

        Flux<Product> productFlux =  Flux.just(
                Product.builder().price(200.0).name("Monitor RCA").image("Arquitectura-AiQuip.png").build(),
                Product.builder().price(210.0).name("Monitor Samsung").image("Arquitectura-AiQuip.png").build(),
                Product.builder().price(120.0).name("Monitor AOC").image("Arquitectura-AiQuip.png").build(),
                Product.builder().price(120.0).name("Monitor BQ").image("Arquitectura-AiQuip.png").build()
        );

		// Crear los datos bases con flatMap. Esto reptie la acción de guardar producto por cada elemento de categoría
//        categoryFlux
//                .flatMap(category -> reactiveMongoTemplate.save(category))
//                .flatMap(data -> productFlux)
//                .flatMap(product -> productRepository.save(product))
//                .subscribe(product -> log.info(String.format("Producto id: %s", product.getId())));

        // En este caso estamos uniendo los flujos mediante un zipWith. Lo que nos permite crear una tupla de elementos. Ahora cuando creamos,
        // A cada producto, le corresponderá una única categoría. Por lo tanto, solo se tomaran la menor cantidad de datos disponibles
        // Es decir, tenemos 3 categorias y 4 productos, pero solo se almacenarán en BD 3 productos.
//        categoryFlux
//                .flatMap(category -> reactiveMongoTemplate.save(category))
//                .zipWith(productFlux)
//                .flatMap(data -> {
//                    data.getT2().setCategory(data.getT1());
//                    return productRepository.save(data.getT2());
//                })
//                .subscribe(product -> log.info(String.format("Producto created")));

        // En este caso estamos obteniendo una categoría en específico; pero dicha operación convierte el flujo en mono
        // Lo unimos al flujo de productos convertido en mono
        // Dentro del zipWith hacemos la operación de save, pero agregamos un subscribible
        // Eso queda feo.
//        categoryFlux
//                .flatMap(category -> reactiveMongoTemplate.save(category))
//                .filter(category -> category.getName().equalsIgnoreCase("Computer"))
//                .next()
//                .zipWith(productFlux.collectList(), (t1, t2) -> {
//                    log.info("Inside zipWith");
//                    log.info(String.format("Array size %s", t2.size()));
//                    for (Product t:
//                         t2) {
//                        log.info("Inside loop");
//                        t.setCategory(t1);
//                        reactiveMongoTemplate.save(t).subscribe();
//                    }
//                    return Mono.just(t1);
//                })
//                .subscribe(product -> log.info(String.format("Producto created")));

        // Este sería el método final que utilizaríamos.
        // En este caso primero salvamos las categorías.
        // Filtramos las mismas hasta encontrar la categoría de computadores
        // Tomamos el primer valor del Filtrado -> Esto automaticamente lo cinvierte en Mono
        // Utilizamos Zip With para unirlo con nuestros productos. Estos son flux por lo que usamos collectList() para convertirlos a mono
        // Para volver a convertir el flujo en FLUX debemos utilizar flatMapMany
        // Dentro adicionamos la categoria a nuestros productos
        // Devolvemos un flux from iterable
        // Con el flatMap de siempre guardamos
        categoryFlux
                .flatMap(category -> reactiveMongoTemplate.save(category))
                .filter(category -> category.getName().equalsIgnoreCase("Computer"))
                .next()
                .zipWith(productFlux.collectList())
                .flatMapMany(t1 -> Flux.fromIterable(t1.getT2())
                            .doOnNext(data -> data.setCategoryId(t1.getT1().get_id()))
                )
                .flatMap(data -> productRepository.save(data))
                .subscribe(product -> log.info("Producto created"));
    }
}
