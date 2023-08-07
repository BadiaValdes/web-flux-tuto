 # Controlador básico de Producto   
   
En este apartado estaremos viendo como implementar un controlador básico en MVC con spring boot y estaremos utilizando el servicio de producto <[link](servicio-basico-de-producto.md)>. Antes de pasar al código de lleno, debemos mencionar que el controlador en el patrón MVC es el intermediario entre la vista y el modelo. Este se encarga de recibir las peticiones del navegador, realizar un conjunto de operaciones (según la petición) y devolver un resultado, en este caso es enviar datos a la vista.   
La declaración básica de un Controller es:   
```java
@Controller
public class ProductController {}
```
El código no dista de una clase de Java normal, simplemente le estamos agregando un decorador llamado `@Controller`. Este **estereotipo **nos permite definir una clase como controlador dentro del ecosistema de **SpringBoot**. Además, se le agregarán los siguientes decoradores sobre `@Controller`:   
```java
@AllArgsConstructor
@RequestMapping("product")
```
- `@AllArgsConstructor` → Anteriormente hemos mencionado que pertenece a la librearía de Lombok y su función principal es crear un constructor con todos los datos que le estemos pasando.   
- `@RequestMapping("product")` → El `RequestMapping` nos permite definir una ruta (URL) de acceso al controlador. Dicho de otra forma, estamos diciéndole al controlador que se encargará de todas las peticiones que provengan de una ruta en específico. En este caso, cualquier petición que provenga de una ruta (URL) que posea `product` como inicio de la misma, será manejada por `ProductoController`.   
   
Al ser una clase de Java es posible la creación dentro de atributos y métodos dentro. Comencemos por los atributos que estaremos manejando:   
```java
private final IProductService productService;
```
Es una línea sencilla, declaramos un atributo del tipo `IProductService`, recuerden que ese es el nombre de la interfaz que nos conectará con el servicio `ProductService`. Estamos usando la interfaz en vez de la clase servicio directamente para evitar el acceso a métodos que no son del interés del controlador. La forma que utilizaríamos para la creación de esta instancia si tuviéramos constructor sería:   
```java
private final IProductService productService;
public ProductController(ProductService productService) {
	this.productService = productService;
}
```
Gracias a la DI (Inyección de dependencias, por sus siglas en ingles) no es necesario realizar una compleja instanciación de los elementos. DI es un patrón utilizado por muchos frameworks en la actualidad, su principal función es otorgarle al framework la capacidad de resolver los objetos que sean necesarios dentro del constructor (en el código anterior sería el parámetro). De esta forma, los programadores pueden con concentrarse en la lógica del negocio y dejar que el sistema se encargue de tareas menores.    
Ahora, porqué no hacemos esto en el código. Es sencillo, estamos dejando que lombok se encargue del proceso de creación del constructor. Otra forma de realizar esta operación sin `@AllArgsConstructor` es mediante la anotación `@Autowire`.   
Habiendo revisado las propiedades, podemos comenzar a indagar en los métodos que se han declarado:   
```java
 @GetMapping()
    public String listNormal(Model model) {
        Flux<Product> productFlux = productService.getAllProducts();
        model.addAttribute("products", productFlux);
        model.addAttribute("page", "pages/product/list"); 
        return LayoutNames.LAYOUT_NAME;
    }

@GetMapping("/back-pressure")
    public String listBackP(Model model) {
        Flux<Product> productFlux = productService.getAllProducts()
                .delayElements(Duration.ofSeconds(1)); 
        model.addAttribute("products", new ReactiveDataDriverContextVariable(productFlux, 1)); 
        model.addAttribute("page", "pages/product/list"); 
        return LayoutNames.LAYOUT_NAME;
    }

@PostMapping("/create")
    public String createProduct(@ModelAttribute ProductDTO productDTO) {
        log.info("I am creating and object");
        Product newProduct = ProductConverter.productDtoToProduct(productDTO); 
        productService.createProduct(newProduct).subscribe(data -> log.info("Data saved")); 
        return LayoutNames.REDIRECT_PRODUCT;
    }

@PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") String id, @ModelAttribute ProductDTO productDTO) {
        Product product = ProductConverter.productDtoToProduct(productDTO);
        product.setId(id);
        productService.createProduct(product).subscribe(data -> log.info("Elemento modificado"));
        return LayoutNames.REDIRECT_PRODUCT;
    }

```
Esto es una parte del código utilizado en el proyecto, iremos desarmándolo poco a poco y comenzaremos con las anotaciones:   
- `@GetMapping()` → Esta anotación nos permite definir que el método declarado posteriormente se encargará de manejar un tipo especifico de petición que llegue al controlador. En este caso controlaremos la petición de tipo `GET` y la ruta será, por ejemplo `product` (el nombre que pusimos en el `RequestMapping`).   
    - Qué pasa si queremos manejar otra ruta por `GET`? La respuesta es bastante sencilla y ya la vimos en la declaración del constructor. Solo tendríamos que agregar dentro de los prentices de la anotación un nombre `@GetMapping("/back-pressure")`. En este ejemplo estamos diciendo que el método manejará las peticiones que provengan de `product/back-pressure`. En caso que queramos declarar más de una ruta para el mismo método, los nombres deben ir encerrados dentro de llaves ({`"/back-pressure", "/bp"`}).   
- `@PostMapping("/create")` → A diferencia del `GET`, esta anotación se encarga de manejar todas las peticiones de tipo `POST` que provengan de una `URL` en específico. En este caso sería cualquier petición `POST` que apunte a `product/create`.   
- `@PostMapping("/update/{id}")` → Similar a la petición anterior. En esta caso, estamos definiendo que dentro de la `URL` irá un parámetro que no tenemos definido, similar a una variable. Esto lo hacemos agregando al nombre dentro de la anotación `{variable\_aqui}`. Esto funciona para cualquier tipo de petición que se realice.    
- `@PathVariable("id")` → Partiendo de la explicación anterior, donde tenemos una variable dentro de la U`RL,` podemos agregar esta anotación. Su función principal es capturar las variables que se pasen como parte de la U`RL;` nos podemos fijar que en este caso estamos diciendo que capture la variable con nombre `id`. Esta anotación puede ser utilizada tantas veces como variables tengamos dentro de nuestra `URL`.   
- `@ModelAttribute` → Cuando hacemos una petición POST desde el front, siempre enviamos un conjunto de datos. Estos pueden ser parte de un formulario o datos incrustados dentro del código. Sin importar de que forma sea, spring boot nos proporciona una anotación para capturarlos. Esta anotación captura todos los datos que provengan desde una petición post.    
   
De la declaración de los métodos no hay que hablar mucho, ya que es la forma básica utilizada en java. Lo que si podemos agregar es el uso de un tipo de dato en específico:   
- `Model`: Este tipo de dato pertenece a Spring y su función principal es armar el contexto (datos a manejar en el front) de nuestra aplicación. De esta forma, podemos declarar cualquier variable que queramos y almacenarla dentro del mismo en forma de `clave: valor`. Posteriormente podrán ser accedidos mediante la `clave` en thymeleaf.   
   
Pasemos a la lógica del negocio, explicaremos elementos resaltables no el código completo:   
```java
Flux<Product> productFlux = productService.getAllProducts();
model.addAttribute("products", productFlux);
```
- Este primer fragmento se encarga de buscar mediante nuestro servicio (`productService.getAllProducts()`) todos los productos almacenados en mongo retornando un `FLUX` de tipo `Product`.   
- Para enviar los datos hacia el front hacemos uso del objeto `model` (declarado como parámetro del método). Declaramos que debe guardarse el resultado bajo el nombre de `products`. Posteriormente accederemos en el front a todos los productos mediante ese nombre.   
   
```java
model.addAttribute("products", new ReactiveDataDriverContextVariable(productFlux, 1)); 
```
- En el código anterior debíamos esperar a que todos los productos se cargaran para poder mostrarle algo al usuario. Esto puede traerle inconvenientes al mismo, ya que si son una cantidad los suficientemente grande para demorar la petición varios segundos,  deberá esperar para ver los datos. La solución se encuentra en lo que llamamos en secciones anteriores `back pressure`. Mediante la creación de un objeto nuevo `(new ReactiveDataDriverContextVariable(productFlux, 1)`) le decimos a `thymeleaf` que recibirá los datos por lote. En este caso, decidimos que los datos guardados en `productFlux` serán mostrados de `1` en `1`; es decir, en el momento que se cargue un elemento se le será mostrado al usuario. De esta forma, no será necesario esperar para ver los datos. Si se desea mostrarlos de `n` en `n`, solo se tiene que cambiar el segundo parámetro del objeto.   
   
```java
Product newProduct = ProductConverter.productDtoToProduct(productDTO); 
```
- En algunos casos nos veremos obligado en convertir los datos que provengan del front para ser utilizados en los servicios. Es decir, nuestros servicios utilizan Product, pero el valor que recibido desde el front es un `ProductDTO`. En este caso se creo una clase llamada `ProductConverter` que se encarga de forma manual (el propio desarrollador se encarga del mapeo) de convertir los datos según el resultado que se desea obtener. Aunque no es una mala práctica, se recomiendo utilizar librerías como `Jackson` para este propósito.   
   
Para culminar tenemos que hablar de los tipos de datos devueltos por los métodos. En la mayoría de los casos estaremos devolviendo un `String` debido a que Spring Boot es capaz de interpretar el string según la acción que se requiera realizar. Si el `String` es una sola palabra, será interpretado como un archivo HTML que en nuestro caso es `"layout"`. Si el objetivo es redireccionar a otra URL, debemos utilizar el comando `"redirect:/LA\_URL"`.    
   
