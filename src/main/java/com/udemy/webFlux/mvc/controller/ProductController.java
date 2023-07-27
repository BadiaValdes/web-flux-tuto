package com.udemy.webFlux.mvc.controller;

import com.udemy.webFlux.mvc.core.constant.LayoutNames;
import com.udemy.webFlux.mvc.core.converter.ProductConverter;
import com.udemy.webFlux.mvc.dto.ProductDTO;
import com.udemy.webFlux.mvc.models.Category;
import com.udemy.webFlux.mvc.models.Product;
import com.udemy.webFlux.mvc.service.IProductService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.spi.Resolver;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Esta clase se va a dedicar al control de todas las llamadas que se realicen a objeto producto
 */
@AllArgsConstructor
@RequestMapping("product")
@Controller // Esta estereotipo permite definir que la clase siguiente será un controlador de spring boot
public class ProductController {
    // Si no uso el decorador @AllArgsConstructor entonces debo poner aquí arriba @Autowired
    private final IProductService productService; // Esta es la conexión al servicio de Producto. Se realiza mediante la interfaz para garantizar solo acceder a los métodos necesarios.

    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class); // Logger system.


    /**
     * Este método se encarga de devolver al front el listado de productos.
     *
     * @param model
     * @return El layout
     */
    @GetMapping()
    public String listNormal(Model model) {
        Flux<ProductDTO> productFlux = productService.getAllProducts();
        model.addAttribute("products", productFlux);
        model.addAttribute("page", "pages/product/list"); // Esto de aquí es una simulación de un layout.
        return LayoutNames.LAYOUT_NAME;
    }

    /**
     * Este metodo nos permitirá observar como se puede manejar el contra presión con thymeleaf
     * Recuenden que el Backpressure es para evitar hacer al usuario esperar a que se carguen todos los valores.
     *
     * @param model Modelo para trabajar con la interfaz gráfica
     * @return layout
     */
    @GetMapping("/back-pressure")
    public String listBackP(Model model) {
        Flux<ProductDTO> productFlux = productService.getAllProducts()
                .delayElements(Duration.ofSeconds(1)); // Como los datos cargan rápido, es necesario agregar un método al subscribible para que demore la entrega de cada elemento por 1 segundo

        model.addAttribute("products", new ReactiveDataDriverContextVariable(productFlux, 1)); // En este caso estamos probando el objeto ReactiveDataDriverContextVariable que recibe un Flux por parámetro y una cantidad de elementos a devolver a la vez
        // Existe una forma de hacer esto mismo, pero utilizando el método de arriba. Es pediante la definición de una propiedad. Vean app..properties
        model.addAttribute("page", "pages/product/list"); // Esto de aquí es una simulación de un layout.
        return LayoutNames.LAYOUT_NAME;
    }

    /**
     * Este método se encarga de cargar la página de creación de productos.
     *
     * @param model Modelo para enviar datos hacia el frontend. Es similar a la variable context
     * @return "Layout"
     */
    @GetMapping("/create")
    public String createProduct(Model model) {
        model.addAttribute("product", new ProductDTO()); // Esto me permite pasarle un objeto al formulario, de esta forma, en el front, podemos decir que input pertenece a cada campo
        model.addAttribute("cat", new Category()); // Esto me permite pasarle un objeto al formulario, de esta forma, en el front, podemos decir que input pertenece a cada campo
        model.addAttribute("categories", reactiveMongoTemplate.findAll(Category.class)); // Para no acceder el repositorio de categorias, utilizamos directamente reactiveMongoTemplate para acceder a mongo
        model.addAttribute("page", "pages/product/create"); // Esto de aquí es una simulación de un layout.
        return LayoutNames.LAYOUT_NAME;
    }

    /**
     * Metodo que recibirá del frontend los datos correspondientes al nuevo producto a añadir. Posee el mismo nombre que el método get gracias a la sobrecarga
     *
     * @param productDTO Datos del producto que vienen desde el frontend.
     * @return Redirect a product
     */
    @PostMapping("/create")
    public String createProduct(@ModelAttribute("product") ProductDTO productDTO, @RequestPart("image") FilePart image) {
        log.info("I am creating and object");
        log.info(image.name());
        Product newProduct = ProductConverter.productDtoToProduct(productDTO); // Esto es una clase auxiliar que permite el mapeo manual de objeto. Se aconseja usar Jackson.
        productService.createProduct(newProduct, image).subscribe(data -> log.info("Data saved")); // Salvamos el dato y nos subscribimos a la llamada para comprobar que se realizó satisfactoriamente.
        return LayoutNames.REDIRECT_PRODUCT;
    }

    /**
     * Este metodo está dedicado a actualizar un producto
     *
     * @param id    Este id se recibe de la url y representa el identificador de un objeto
     * @param model Modelo que nos permite pasar datos hacia el front
     * @return layout
     */
    @GetMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") String id, Model model) {
        log.info("Estoy dentro del update");
        Mono<ProductDTO> product = productService.getOneProduct(id).map(data ->
                ProductConverter.productToProductDto(data)
        ); // Para evitar problemas de datos, antes de enviar al front el producto, lo convertimos en el objeto ProductoDTO
        model.addAttribute("product", product); // Esto me permite pasarle un objeto al formulario, de esta forma, en el front, podemos decir que input pertenece a cada campo
        model.addAttribute("page", "pages/product/update"); // Esto de aquí es una simulación de un layout.
        return LayoutNames.LAYOUT_NAME;
    }

    /**
     * Este metodo se encarga de el proceso de modificar los datos que vengan del formulario del front
     *
     * @param id         Representa el id que viene de la url del front. Otra forma de hacer eso es añadir en los input del front un hidden con el id y nos quitamos esta declaradción
     * @param productDTO Representa el producto que proviene del formulario
     * @return Redirect To Product
     */
    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") String id, @ModelAttribute ProductDTO productDTO) {
        Product product = ProductConverter.productDtoToProduct(productDTO);
        product.set_id(id);
        productService.updateProduct(product).subscribe(data -> log.info("Elemento modificado")); // Estamos usando el mismo metodo de crear debido a que no hay cambios significativos entre los dos. Pero en caso que sea necesario, se debe crear otro método.
        return LayoutNames.REDIRECT_PRODUCT;
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") String id) {
        productService.deleteProduct(id).subscribe(data -> log.info("Elemento eliminado"));
        return LayoutNames.REDIRECT_PRODUCT;
    }

    /**
     * Get product image
     **/
    @GetMapping("/uploads/{name:.+}")
    public Mono<ResponseEntity<Resource>> getImage(@PathVariable("name") String name) throws MalformedURLException {

        log.info(name);

        Path imageRoute = Paths.get("./src/main/resources/upload").resolve(name).toAbsolutePath();

        log.info(imageRoute.toString());

        Resource image = new UrlResource(imageRoute.toUri());

        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\" ")
                .body(image));
    }

    /**
     * Get product image
     **/
    @GetMapping("/show/{id}")
    public String showImage(Model model, @PathVariable("id") String id) {
        model.addAttribute("product", productService.getOneProduct(id)); // Esto me permite pasarle un objeto al formulario, de esta forma, en el front, podemos decir que input pertenece a cada campo
        model.addAttribute("page", "pages/product/image");
        return LayoutNames.LAYOUT_NAME;
    }


}
