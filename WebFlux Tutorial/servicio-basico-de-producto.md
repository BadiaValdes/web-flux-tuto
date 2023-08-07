 # Servicio básico de producto   
   
En este apartado veremos como implementar un servicio básico. Este servicio solo realizará las acciones de un CRUD. En otras páginas de este "curso" veremos algunas implementaciones más complejas de servicios (de ser necesario). Comencemos con la forma correcta de declarar un servicio.   
Siguiendo los reglamentos de clean code para Spring Boot, el servicio no debe ser declarado directamente. Este debe ser dividido en 2, una interfaz y la clase correspondiente. Te debes estar preguntando "Porqué debo pasar tanto trabajo?". La respuesta es sencilla. Primero posees un código más ordenado y segundo evitas que el controlador tenga acceso a métodos que no necesita conocer.    
Comencemos con la interfaz:   
```java
public interface IProductService {
    Mono<Product> createProduct(Product product);
    Mono<Product> getOneProduct(String id); 
    Flux<Product> getAllProducts();
    Mono<Void> deleteProduct(String id); 
    Mono<Void> deleteManyProduct(List<String> id);
}

```
Las interfaces son bastante sencillas de crear. En este caso declaramos los métodos que serán implementados dentro de nuestro servicio. Como estamos trabajando con WebFlux los tipos de datos a devolver por cada uno de los servicios debe ser Mono o Flux.    
Mono representa la devolución de 0 o 1 elemento. Por eso se utiliza en el `getOneProduct` y `createProduct`. Flux para devolver 0 o más elementos, su uso puede variar, pero en este caso lo ponemos como tipo de dato del método `getAllProducts`, de esa forma devolveremos cada una de las ocurrencias en base de datos. Más adelante veremos una forma de pasarle al frontend los datos mediante paquetes, es decir usando flux, pero mostrando de 2 en dos hasta que termine de cargar.   
El caso de los eliminar deben devolver un `Void`, debido a que cuando se elimina, no es necesario devolver ningún dato.   
Veamos ahora como quedaría el servicio:   
```java
@AllArgsConstructor
@Service
public class ProductService implements IProductService {

    private final ProductRepository productRepository;

    @Override
    public Mono<Product> createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Mono<Product> getOneProduct(String id) {
        // Option 2
        /* Mono<Product> productMono = productRepository
                .findAll() 
                .filter(data -> data.getId().equals(id)) 
                .next(); 
        */
        return productRepository.findById(id); // Option 1
    }

    @Override
    public Flux<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Mono<Void> deleteProduct(String id) {
        return productRepository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteManyProduct(List<String> id) {
        return productRepository.deleteAllById(id);
    }
}
```
Vamos por parte. Comencemos con la declaración de la clase:   
```java
@AllArgsConstructor
@Service
public class ProductService implements IProductService
```
- Antes de la declaración de la case, utilizamos dos anotaciones:   
    - `@AllArgsContructor` → Es parte de lombok y nos permite la crear el constructor con todos los argumentos (atributos) que se declaren en la clase. Bastante útil para aligerar el código.   
    - `@Service` → Esta anotación (estereotipo) nos permite definir una clase como servicio. Además le permite saber a Spring Boot el uso de esa clase dentro de su infrastructura.   
    - `implements IProductService` → Mediante esta línea de código, estamos diciendo que nuestra clase va a implementar todos los métodos declarados en la interfaz `IProductService`.   
   
Posteriormente declaramos los atributos; en este caso el atributo:   
```java
private final ProductRepository productRepository;
```
- Gracias al uso del decorador `@AllArgsConstructor` podemos declarar sin problemas el atributo `productRepository`. De esta forma, estamos dejando que el sistema se encargue de instanciar las dependencias por nosotros. En caso que no utilicemos `@AllArgsConstructor`, tenemos que utilizar `@Autowired` sobre cada atributo que utilicemos.   
   
Continuemos con los métodos:   
```java
    @Override
    public Mono<Product> createProduct(Product product) {
        return productRepository.save(product);
    }

```
- `@Override` → Debido a que es un método declarado en la interfaz, es necesario ponerle la anotación @Override; de esta forma indicamos que estamos sobrescribiendo el método.   
- `Mono<Product>` → Representa el tipo de dato que vamos a devolver. Mono debido a que solo devolveremos un elemento y Producto por el tipo de elemento a devolver.   
- `productRepository.save` → Gracias a la declaración de la propiedad productRepository podemos acceder a todos los métodos que brinda el JPA de MongoDB. De esta forma, acciones tan sencillas como salvar, buscar o eliminar pueden realizarse sin la necesidad de realizar codificaciones extras.   
   
Antes de terminar con la explicación de un servicio, vamos a revisar un código que está comentado:   
```java
Mono<Product> productMono = productRepository
                .findAll() 
                .filter(data -> data.getId().equals(id)) 
                .next(); 

```
- Aquí decidimos realizar una llamada a un `findAll` que devuelve un `Flux`.    
- Posteriormente filtramos los datos; esto todavía retorna un `Flux`.    
- Ahora, la idea del método es retornar un `Mono`; por lo que se hace necesario el uso del método `next`. Este retorna solamente una ocurrencia de `Flux`.   
   
Para indagar en el uso de este servicio en el controlador, podemos dirigirnos al siguiente <[link>](controlador-basico-de-producto.md).   
   
