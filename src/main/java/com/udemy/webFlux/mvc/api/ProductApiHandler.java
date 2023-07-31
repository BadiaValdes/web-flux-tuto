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
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

/***
 * Esta clase es la que se utilizará en ApiRouter.
 * No es un controller como estamos acostumbrados a ver. Este recibe el nombre de handler ya que
 * su inica función es manejar el proceso de llamada al service; no se encarga del mapeo de URL.
 * Es obligatorio que todos los métodos que se implementen aquí, tengan como parámetro ServerRequest req
 * ya que mediante este podremos acceder a las variables por parámetros o del cuerpo.
 *
 * @author Emilio
 */
@Component
public class ProductApiHandler {

    @Autowired
    private IProductService productService;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    /**
     * Este método se encarga de obtener todos los productos.
     *
     * @param req
     * @return ServerResponse
     */
    public Mono<ServerResponse> getAllProducts(ServerRequest req){
        return ServerResponse.ok().body(productService.getAllProducts(), ProductDTO.class);
    }

    /***
     * Devuelve un producto mediante el id pasado por parámetros. Para acceder al id
     * utilizamos el ServerRequest req. Especificamente req.pathVariable("id")
     *
     * @param req
     * @return
     */
    public Mono<ServerResponse> getOneProduct(ServerRequest req){
        return ServerResponse.ok().body(productService.getOneProduct(req.pathVariable("id")), ProductDTO.class);
    }

    /**
     * Este método es un poco más complejo. Su funcionamiento es crear un producto, pero en este caso
     * debemos trabajar con un formData por lo que tenemos que utilizar req.body(BodyExtractors.toMultipartData())
     * para obtener el cuerpo y esto lo que devuelve es un mono.
     *
     * @param req
     * @return
     */
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
