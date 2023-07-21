package com.udemy.webFlux.mvc;

import com.udemy.webFlux.mvc.conf.WebConfig;
import com.udemy.webFlux.mvc.core.constant.CollectionNames;
import com.udemy.webFlux.mvc.models.Product;
import com.udemy.webFlux.mvc.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Locale;

@SpringBootApplication
@EnableReactiveMongoAuditing // Nos permite utilizar las anotaciones de CreationDate
@AllArgsConstructor
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

		Flux.just(
				Product.builder().price(200.0).name("Monitor RCA").build(),
				Product.builder().price(210.0).name("Monitor Samsung").build(),
				Product.builder().price(120.0).name("Monitor AOC").build()
		)
				.flatMap(product -> productRepository.save(product))
				.subscribe(product -> log.info(String.format("Producto id: %s", product.getId())));
	}



}
