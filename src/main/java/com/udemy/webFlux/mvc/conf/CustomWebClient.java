package com.udemy.webFlux.mvc.conf;

import com.udemy.webFlux.mvc.dto.WebClientProduct;
import io.netty.channel.ChannelOption;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Service
public class CustomWebClient {
    private final WebClient webClient;

    private HttpClient httpClient = HttpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 50000) // Set Time Out For read
            .responseTimeout(Duration.ofMillis(50000)) // Set time out for return value
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

    public Flux<WebClientProduct> getProducts(){
        return webClient.get()
                .uri("/products?noofRecords=1&currency=usd")
                .retrieve()
                .bodyToFlux(WebClientProduct.class);
    }
}
