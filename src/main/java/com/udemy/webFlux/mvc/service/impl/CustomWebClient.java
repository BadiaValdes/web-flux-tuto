package com.udemy.webFlux.mvc.service.impl;

import com.udemy.webFlux.mvc.dto.WebClientProduct;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.netty.channel.ChannelOption;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CustomWebClient {
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final WebClient webClient;

    private HttpClient httpClient = HttpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500) // Set Time Out For read
            .responseTimeout(Duration.ofMillis(500)) // Set time out for return value
            .compress(true);


    public CustomWebClient(){
       this.webClient = WebClient // Accedemos al webClient
                .builder() // Utilizamos el patron builder
                .baseUrl("https://hub.dummyapis.com/") // Ponemos la url de base
                .clientConnector(new ReactorClientHttpConnector(httpClient)) // Le pasamos la configuracion HTTP
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }



    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "testA", fallbackMethod = "fallBackMethod")
    public Flux<WebClientProduct> getProducts(){
        return webClient.get()
                .uri("/products?noofRecords=1&currency=usd")
                .retrieve()
                .bodyToFlux(WebClientProduct.class);
    }

    public Flux<WebClientProduct> fallBackMethod(Exception e) {
        WebClientProduct p = WebClientProduct.builder().name("FROM CB").build();
        log.info("FROM fallBack");
        return Flux.just(p);
    }


}
