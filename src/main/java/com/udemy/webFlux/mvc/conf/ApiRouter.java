package com.udemy.webFlux.mvc.conf;

import com.udemy.webFlux.mvc.api.ProductApiComponent;
import com.udemy.webFlux.mvc.core.constant.ApiNames;
import com.udemy.webFlux.mvc.dto.ProductDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class ApiRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = ApiNames.API_BASE + ApiNames.API_PRODUCT,
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = ProductApiComponent.class,
                            beanMethod = "getAllProducts",
                            operation = @Operation(
                                    operationId = "getAllProducts",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = ProductDTO.class
                                                    ))
                                            )
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = ApiNames.API_BASE + ApiNames.API_PRODUCT + "/{id}",
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = ProductApiComponent.class,
                            beanMethod = "getOneProduct",
                            operation = @Operation(
                                    operationId = "getOneProduct",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "id")
                                    },
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = ProductDTO.class
                                                    ))
                                            )
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = ApiNames.API_BASE + ApiNames.API_PRODUCT,
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = ProductApiComponent.class,
                            beanMethod = "createProduct",
                            operation = @Operation(
                                    operationId = "createProduct",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = MultipartRequest.class))),
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = ProductDTO.class
                                                    ))
                                            )
                                    }
                            )
                    ),
            }
    )
    RouterFunction<ServerResponse> routerProduct (ProductApiComponent productApiComponent){
        return RouterFunctions.route()
                .GET(ApiNames.API_BASE + ApiNames.API_PRODUCT, productApiComponent::getAllProducts)
                .GET(ApiNames.API_BASE + ApiNames.API_PRODUCT + "/{id}", productApiComponent::getOneProduct)
                .POST(ApiNames.API_BASE + ApiNames.API_PRODUCT, RequestPredicates.contentType(MediaType.MULTIPART_FORM_DATA), productApiComponent::createProduct)
                .build();
    }
}
