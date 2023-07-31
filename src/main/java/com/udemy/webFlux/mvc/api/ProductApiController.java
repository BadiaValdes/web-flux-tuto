package com.udemy.webFlux.mvc.api;

import com.udemy.webFlux.mvc.core.constant.ApiNames;
import com.udemy.webFlux.mvc.dto.ProductDTO;
import com.udemy.webFlux.mvc.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Clasico rest controller
 *
 * @author Emilio
 */
@RestController
@RequestMapping(ApiNames.API_BASE + ApiNames.API_PRODUCT)
public class ProductApiController {
    @Autowired
    private IProductService productService;

    @Operation( // Para los rest controller debemos utilizar esta anotacion para la documentación
            operationId = "getAllProducts",
            summary = "List All Products",
            tags = {"product"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ServerResponse.class))),
            }
    )
    @GetMapping(produces = {"application/json"})
    public Mono<ServerResponse> getAllProducts() {
        return ServerResponse.ok().body(productService.getAllProducts(), ProductDTO.class);
    }
}
