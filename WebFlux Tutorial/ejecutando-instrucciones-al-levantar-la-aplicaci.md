 # Ejecutando instrucciones al levantar la aplicación.   
   
SpringBoot nos permite utilizar una interfaz para la ejecución de comandos al inicio de la aplicación. Esta interfaz lleva por nombre `CommandLineRunner` y su función principal es proporcionar el método `run` que permitirá la ejecución de nuestros códigos de inicio:   
```java
@SpringBootApplication
public class MvcApplication implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication.run(MvcApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
	}
}
```
En este caso, estaremos utilizando la clase run para crear los valores iniciales que poblarán nuestra base de datos:   
```java
@Override
public void run(String... args) throws Exception {
	mongoTemplate.dropCollection("product").subscribe(); // Eliminar la coleción creada en mongo para limpiar datos

	Flux.just(
			Product.builder().price(200.0).name("Monitor RCA").build(),
			Product.builder().price(210.0).name("Monitor Samsung").build(),
			Product.builder().price(120.0).name("Monitor AOC").build()
		)
			.flatMap(product -> productRepository.save(product))
			.subscribe(product -> log.info("Producto: " + product.getId()));
}
```
Para poder utilizar productRepository en el main, tuvimos que añadir un par de líneas de código:   
```java
@AllArgsConstructor 
public class MvcApplication //...
	// ...
	private final ProductRepository productRepository;
	private final ReactiveMongoTemplate mongoTemplate; // Permite realizar acciones sobre las colecciones de mongo
	// ...

```
   
