 # Creando API   
   
Vamos a dividir este apartado en 2. El primero se lo dedicaremos a la creación de la API pero con rutas mediante función y la segunda a la forma clásica de crear una API. De paso, veremos como documentar nuestros endpoints en ambos casos. Para ello, utilizaremos la siguiente librería:   
```xml
<dependency>
     <groupId>org.springdoc</groupId>
     <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
     <version>2.1.0</version>
</dependency>
```
Algunos compañeros mios utilizaron otras dependencias para agregar swagger y documentar la API. En mi caso, ninguna de esas dependencias funcionaron correctamente. En caso de la que puse anteriormente no funcione, a continuación le comparto las dependencias de mis compañeros:   
```xml
<dependency>
   <groupId>org.springdoc</groupId>
   <artifactId>springdoc-openapi-webflux-core</artifactId>
   <version>1.6.15</version>
</dependency>
<dependency>
   <groupId>org.springdoc</groupId>
   <artifactId>springdoc-openapi-webflux-ui</artifactId>
   <version>1.6.15</version>
</dependency>
```
Teniendo las bases para la documentación, vamos a comenzar con la variante 1. Las rutas funcionales son parte del ecosistema reactivo de WebFlux. Su propósito es brindar un acercamiento más funcional a la creación de rutas para la API siendo una alternativa a la creación de rutas mediante decoradores. La diferencia es que en la segunda opción, cada método puede devolver un tipo de dato diferente; pero todos los métodos utilizados en las rutas funcionales deben ser del mismo tipo de dato. Otro tema a tener en cuenta es la no existencia de controladores o propiamente dicho la sustitución de los controladores por manejadores (Handlers). Es decir, tendremos un método que se encarga de recibir las peticiones, pero ya no será un controlador como estábamos acostumbrados a ver, ahora será una función normal que llamaremos cuando sea necesario.   
Comencemos por la declaración de la clase, ya que estas rutas deben ser declaradas en un archivo de configuración o en el principal de nuestra aplicación.    
```java
@Configuration
public class ApiRouter {}
```
Dentro, tendremos un único método encargado del ruteo de la aplicación. Este método debe estar decorado con @Bean para decirle a SpringBoot que debe manejarlo en su contexto. Más adelante veremos como debemos agregar la documentación para este caso:   
```java
@Bean
RouterFunction<ServerResponse> routerProduct (ProductApiHandler productApiHandler){
        RouterFunction<ServerResponse> person = RouterFunctions
                .route()
                .path(ApiNames.API_PRODUCT, 
                        builder -> 
                                builder
                                        .GET(productApiHandler::getAllProducts) 
                                        .GET("/{id}", productApiHandler::getOneProduct)
										.POST(RequestPredicates.contentType(MediaType.MULTIPART_FORM_DATA), productApiHandler::createProduct))
                .build();

        return RouterFunctions.route() 
                .path(ApiNames.API_BASE_R,
                        builder ->
                                builder.add(person) 
                )
                .build();
    }

```
El tipo de dato de esta función es `RouterFunction` y a su vez, esta clase es genérica, por lo que podemos decirle que se crearan funciones de rutas que devolverán datos del tipo `ServerResponse`.  Por parámetro le pasamos nuestro `Handler`; más adelante veremos esta clase.    
Dentro simplemente devolvemos las rutas creadas. Para construir las rutas utilizamos el patrón builder propio de `RouterFunction` y como si fuesen bloques de legos añadimos nuestros endpoints. Además utilizamos un sistema de rutas anidados para reutilizar secciones de url:   
- **path**: Nos permite definir una ruta padre. Es decir, todas las rutas declaradas debajo comenzarán con el nombre declarado en el path. Ejemplo:   
    - path → api   
    - ruta hija → persona   
    - acceder a persona desde el navegador → api/persona   
- **builder**: El path como segundo parámetro recibe lo que se conoce como callback. Esta función tiene como parámetro el mismo builder del route que desencadenó el proceso, por lo que podemos seguir construyendo las rutas a nuestro gusto.   
- **GET **\| **POST**: Estos métodos hacer referencia a la acción http a realizar. Por parámetro se le pueden pasar diferentes parámetros, pero el más importante y que no puede faltar es el callback hacia el handler. Otros parámetros presentes en el código anterior son:   
    - La URL siendo el primero y siempre el primero.   
    - La configuración del header de la respuesta; en este caso estamos modificando el `contentType`.    
    - Hay que aclarar, el último parámetro debe ser siempre el handler.   
- **add**: En el return tenemos dentro del builder la llamada a este método. Su función es concatenar diferentes `RouterFunctions`.   
   
Una versión sencilla y resumida del código anterior es el siguiente. Pero la desventaja que tiene con el anterior es que no usamos rutas anidadas.   
```java
RouterFunction<ServerResponse> routerProduct (ProductApiHandler productApiHandler){
        return RouterFunctions.route() 
                .GET(ApiNames.API_BASE + ApiNames.API_PRODUCT, productApiHandler::getAllProducts) 
                .GET(ApiNames.API_BASE + ApiNames.API_PRODUCT + "/{id}", productApiHandler::getOneProduct)
                .POST(ApiNames.API_BASE + ApiNames.API_PRODUCT, RequestPredicates.contentType(MediaType.MULTIPART_FORM_DATA), productApiHandler::createProduct)
                .build();
    }
```
Habiendo visto como podemos conformar las rutas; podemos dar paso a ver como crearemos las funciones de manejo. Estas funciones no distan mucho de lo que haría un controller clásico de SpringBoot. Pero la forma de construirlos es un poco diferente, ya que solo recibirán un parámetro que albergará toda la información que venga de la petición.   
```java
@Component
public class ProductApiHandler {
    @Autowired
    private IProductService productService;
    
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
```
- **@Component**: Utilizamos este decorador para definir la clase como parte de los componentes de SpringBoot.   
- **@Autowired**: Nos permite delegar a SpringBoot la creación de la instancia de un objeto.   
- `private IProductService productService`: Es la declaración del servicio que estaremos utilizando.   
- **ServerResponse**: En este caso, todos los métodos deben devolver este tipo de dato. Esto se debe a que en las rutas declaramos que esta sería el tipo de dato a devolver.   
- ServerRequest: Esta clase es utilizada como tipo de dato en todos los parámetros de nuestros manejadores. Esto se debe a que las llamadas mediante funciones pasan todos los datos provenientes del frontend dentro de una variable de este tipo. Por lo tanto, si queremos acceder al cuerpo o cabeza de la petición, debemos hacer uso de la misma.   
- **BodyExtractors**: Esta clase nos brinda una serie de funciones que nos permitirán obtener datos específicos de los cuerpos de las peticiones entrantes. En este caso estamos utilizando `.toMultipartData()` para acceder al multipart-form de la petición.   
   
Para terminar con el apartado reactivo, veamos como vamos a documentar nuestros endpoints:   
```java
@RouterOperations( 
            {
                    @RouterOperation( 
                            path = ApiNames.API_BASE_R + ApiNames.API_PRODUCT, 
                            produces = {
                                    MediaType.APPLICATION_JSON_VALUE 
                            },
                            method = RequestMethod.GET, 
                            beanClass = ProductApiHandler.class, 
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
             }
    )
    RouterFunction<ServerResponse> routerProduct (ProductApiHandler productApiHandler){
```
- RouterOperations: permite definir el inicio de la documentación y por parámetros recibe un objeto. Hay que recalcar que toda la documentación se debe hacer dentro de este decorador, por lo que debe estar ordenada y en correspondencia a los datos del router.   
- RouterOperation: permite definir la documentación de una operación:   
    - path: URL interna que tendrá el endpoint.    
    - produces: define el tipo de dato que se devolverá.   
    - method: define el método HTTP a utilizar para realizar la llamada.   
    - beanClass: define la clase Handler que estaremos utilizando para manejar la ruta.   
    - beanMethod: define el método de la clase Handler encargado de manejar la ruta.   
    - operation: nos permite definir las diferentes respuestas que puede devolver la ruta.   
        - responses: almacena todas las posibles respuestas de la ruta.   
        - ApiResponse: define la documentación para una respuesta en específico.   
            - responseCode: código que devolverá si esa respuesta es retornada.   
            - description: descripción de la respuesta a devolver.   
            -  content: contenido de la respuesta. Normalmente es el objeto a devolver.   
   
               
   
Terminamos con las rutas reactivas y damos paso a la conformación de los endpoint mediante el @RestController.   
```java
@RestController
@RequestMapping(ApiNames.API_BASE + ApiNames.API_PRODUCT)
public class ProductApiController {
    @Autowired
    private IProductService productService;

    @Operation( 
            operationId = "getAllProducts",
            summary = "List All Products",
            tags = {"product"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDTO.class))),
            }
    )
    @GetMapping(produces = {"application/json"})
    public Flux<ProductDTO> getAllProducts() {       
        return productService.getAllProducts();
    }
}
```
En este caso, las clases que se dedicarán a almacenar los controladores deben estar decoradas con `@RestController`; de esta forma el informan a SpringBoot su propósito. Además, agregamos el decorador `@RequestMapping` al inicio de la clase para definir una URL base.   
Dentro vemos algo bastante parecido a los controladores del MVC; por lo tanto se puede definir que su comportamiento no es muy diferentes. Cuál es la mayor diferencia?, el tipo de dato que se retorna. Donde antes retornábamos un String, ahora retornamos el tipo de dato del objeto a mostrar.    
En la documentación del endpoint, podemos ver que el trabajo en el `RestController` es un poco más organizado. Esto es debido que a cada función le corresponde su propia documentación. En este caso vemos la documentación aplicada únicamente al método `getAllProducts`.   
- **Operation**: nos permite crear la documentación de una operación.   
- **operationId**: hace referencia al nombre del método encargado de la operación.   
- **summary**: define la descripción del endpoint.   
- **tags**: nos permite definir diferentes etiquetas para identificar el endpoint.   
- **responses**: definimos las distintas respuestas que puede devolver el endpoint.   
    - **@ApiResponse**: define la documentación para un tipo de respuesta en específico.   
        - **responseCode**: código HTTP de la respuesta.   
        - **description**: descripción de los datos de respuesta.   
        - **content**: contenido de la respuesta. Aquí declaramos el tipo de respuesta a devolver y el objeto que almacenará.   
   
           
   
   
   
