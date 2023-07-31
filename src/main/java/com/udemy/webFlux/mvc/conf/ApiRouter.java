package com.udemy.webFlux.mvc.conf;

import com.udemy.webFlux.mvc.api.ProductApiHandler;
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


/***
 * Esta clase está creada para probar el uso del ruteo mediante función en WebFlux.
 * Además vemos como documentar nuestros endpoint.
 *
 * @author Emilio
 */
@Configuration
public class ApiRouter {

    @Bean
    @RouterOperations( // Nos permite crear un objeto de documentación de operaciones de rutas
            {
                    @RouterOperation( // Esto lo hacemos por cada ruta que queramos documentar.
                            path = ApiNames.API_BASE + ApiNames.API_PRODUCT, // El path que estamos utilizando aqui es el mismo que debemos poner en la ruta
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE // Formato del dato que vamos a devolver
                            },
                            method = RequestMethod.GET, // Método a utilizar
                            beanClass = ProductApiHandler.class, // Clase que se encarga del manejo de la ruta
                            beanMethod = "getAllProducts", // Nombre del método que trabaja con la ruta
                            operation = @Operation( // Infor de la operación a realizar
                                    operationId = "getAllProducts", // Nombre de la operacion
                                    responses = { // Las diferentes respuestas que se pueden dar
                                            @ApiResponse( // Esta respuesta corresponde a que todo esté bien
                                                    responseCode = "200", // Codigo de respuesta
                                                    description = "successful operation", // Descripcion de lo que deberia devolver
                                                    content = @Content(schema = @Schema( // Aqui definimos el objeto que va a devolver nuestro endpoint
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
                            beanClass = ProductApiHandler.class,
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
                            beanClass = ProductApiHandler.class,
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
    RouterFunction<ServerResponse> routerProduct (ProductApiHandler productApiHandler){
        return RouterFunctions.route() // En este caso estamos siguiendo un patron Builder para constuir las rutas
                .GET(ApiNames.API_BASE + ApiNames.API_PRODUCT, productApiHandler::getAllProducts) // Definimos las rutas a nuestro gusto
                .GET(ApiNames.API_BASE + ApiNames.API_PRODUCT + "/{id}", productApiHandler::getOneProduct)
                .POST(ApiNames.API_BASE + ApiNames.API_PRODUCT, RequestPredicates.contentType(MediaType.MULTIPART_FORM_DATA), productApiHandler::createProduct)
                .build();
    }
}
