package com.udemy.webFlux.mvc.api;

import com.udemy.webFlux.mvc.controller.ProductController;
import com.udemy.webFlux.mvc.dto.ProductDTO;
import com.udemy.webFlux.mvc.models.Product;
import com.udemy.webFlux.mvc.service.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class ProductApiComponent {

    @Autowired
    private IProductService productService;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    public Mono<ServerResponse> getAllProducts(ServerRequest req){
        return ServerResponse.ok().body(productService.getAllProducts(), ProductDTO.class);
    }

    public Mono<ServerResponse> getOneProduct(ServerRequest req){
        return ServerResponse.ok().body(productService.getOneProduct(req.pathVariable("id")), ProductDTO.class);
    }

    public Mono<ServerResponse> createProduct(ServerRequest req){
        return req.body(BodyExtractors.toMultipartData()).flatMap(
                data -> {
                    Map<String, Part> map = data.toSingleValueMap();
                    Part filePart = map.get("file");

                    FormFieldPart name = (FormFieldPart) data.getFirst("name");
                    FormFieldPart price = (FormFieldPart) data.getFirst("price");
                    FormFieldPart categoryId = (FormFieldPart) data.getFirst("categoryId");
                    FormFieldPart test = (FormFieldPart) data.getFirst("test");

                    log.info(name.value());
                    log.info(price.value());
                    log.info(categoryId.value());
                    log.info(test.value());

                    Product p = Product
                            .builder()
                            .name(name.value())
                            .price(Double.parseDouble(price.value()))
                            .categoryId(categoryId.value())
                            .build();

                    return ServerResponse.ok().body(productService.createProduct(p,(FilePart)filePart), ProductDTO.class);
                }
        );
    }
}
