 # Creando nuestro primer modelo   
   
Comencemos creando nuestro primer modelo y posteriormente explicaremos cada uno de los eleméntenos que lo componen:   
```java
@Document(collation = "products")
@NoArgsConstructor()
@AllArgsConstructor()
@Data()
@Builder()
public class Product {
    @Id
    private String id;

    private String name;
    private Double price;
    private Date createdAt;
}
```
- `@Document`: esta anotación nos permite definir que la clase que vamos a crear a continuación será tratada como un documento de MongoDB. Como parámetro, se le pasa el nombre de la colección; **este debe ser en plural**.   
- `@NoArgsConstructor`: pertenece a las anotaciones de lombook. Esta permite la declaración de decoradores que permiten la creación de constructores, getters y setters sin la necesidad que el programador tenga que codificarlos. Esta en específico nos permite crear un constructor sin argumentos.   
- `@AllArgsConstructor`: parte de lombook y nos permite crear un constructor con todas las propiedades.   
- `@Data`: lombook y permite la declaración de todos los setters y getters.   
- `@Builder`: parte de lombook y nos permite implementar de forma sencilla el patrón builder. Este patrón nos permite construir objetos sin la necesidad de utilizar específicamente el constructor.   
- `@Id`: esta anotación permite definir un valor, dentro de las propiedades de la clase, como llave primaria de la colección.   
 --- 
   
 ## Repositorio   
Los repositorios en Spring Boot son un patrón de diseño que nos permite crear una interfaz intermedia entre el ORM y nuestra aplicación. En este caso, crearemos el repositorio que nos permitirá realizar consultas a la colección anteriormente mostrada:   
```java
@Repository // Es un estereotipo para asignar responsabilidades a una clase
public interface ProductRepository extends ReactiveMongoRepository<Product, String> {}

```
Como podemos observar, estamos creando la interfaz `ProductRepository` y hacemos que este extienda de `ReactiveMongoRepository<T, E>`. Al hacer esta extensión, le estamos permitiendo a `ProductRepository` acceder a todos los métodos ya implementados dentro de `ReactiveMongoRepository`. Además, esta ultima clase es genérica `<T, E>`, por lo que podemos utilizarla en todos nuestros repositorios; T hace referencia a la coleccion y E al tipo de dato que tiene el decorador `@Id`.   
Esta es la implementación más básica que existe de repositorio en SpringBoot. Más adelante veremos como declarar métodos dentro de la interfaz que nos permitan obtener datos dado el valor de un campo.   
   
