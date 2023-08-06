 # Mongo DB relations   
   
Las base de datos creadas con Mongo DB son documentales; permitiendo almacenar los datos dentro de archivos en formato JSON. Este acercamiento tiene muchas ventajas, así como varias formas de crear relaciones entre los documentos. La primera es almacenar toda la información en un mismo documento, de esta forma, no será necesario la creación de consultas complejas; pero existen varios problemas si utilizamos este acercamiento. Estos varían en cuanto a problema con tamaño de documentos o a datos duplicados. La segunda opción es mediante referencia, es decir, solo almacenar el id para después realizar una unión entre documentos. Pero en general, el propio MongoDB sugiere el uso de la primera opción siempre y cuando sea posible.   
Para mostrar las diferentes formas que podemos utilizar en Spring Boot para realizar la relación entre objetos nos apoyaremos en las siguientes dos clases:   
```java
public class Category {
    private String id;
    private String name;
    private Double price;
    private Date createdAt;
    private Date lastModifiedAt;
}

public class Product {
    private String id;
    private String name;
    private Double price;
    private Date createdAt;
}

```
Comencemos por la opción de almacenar los datos en un solo documento. En este caso tendríamos que ver primero quien tendría mayor cantidad de datos; en este caso todos estamos de acuerdo que sería `Product`. Por lo tanto, la relación embebida quedaría de la siguiente forma:   
```java
public class Product {
    //...
	private Category category;
}
```
Esta forma, a pesar de ser bastante flexible y brindar la capacidad de evitar la vinculación entre documentos, no vale la pena cuando se ve la cantidad de veces que tendríamos que actualizar los datos de la categoría si cambiase. Qué pasa si lo hacemos al revés?   
```java
public class Category{
    //...
	private Product product;
}
```
En este caso pensaríamos: nos evitamos la duplicidad de datos y mantenemos la capacidad de no tener que vincular documentos. Tenemos razón en esto, pero este acercamiento trae un problema más grave y es la creación de documentos muy largos. Una categoría con 500 productos sería muy grande y por lo tanto haría nuestro sistema lento. Cuando estamos frente a este tipo de situación, es preferible (aunque nos duela) relizar la vinculación de docuemntos.   
La primera forma de vinculación que veremos será a trabes de la anotación `DBRefs`. Aunque a partir de ahora tenemos que ver donde realizar la referencia, es decir, que documento necesita almacenar la referencia. En nuestro caso, diremos que la clase Category debe almacenar la referencia a Product; debido a que nos es más factible buscar los productos por la categoría que de forma contraria:   
```java
public class Category{
    //...
	@DBRef
	private List<Product> products;
}

// Ejemplo de productos
// "products" : [
// 		{
//			"$ref":"product", 
//			"$id":"617cfb
//		}
// 	]
```
De esta forma no tenemos que preocuparnos por el tamaño que alcance el documento, ya que al almacenar esta poca cantidad de datos es muy poco probable que se llene la base de datos. Aunque con este acercamiento perdemos la posibilidad desde productos buscar la categoría.    
Pero no debemos preocuparnos por esto, ya que podemos seguir un acercamiento bastante parecido a una base de datos relacional. Este acercamiento es crear una referencia inversa en el documento producto hacia la categoría:   
```java
public class Product{
    //...
	private Category category;
}
```
Mientras que la opción de `DBRef` es bastante buena, su uso es mejor visto cuando trabajamos con vinculación entre diferentes colecciones; lo que no quiere decir que no la debamos utilizar. En caso que queramos evitar el uso del decorador visto anteriormente, podemos realizar una `referencia manual`; es decir, remover por completo el decorador y dejar la declaración unicamente:   
```java
public class Category{
    //...
	private List<Product> products;
}

// Ejemplo de productos
// "products" : [ "617cfb"]
```
Este acercamiento nos obliga que a la hora de actualizar los datos, por ejemplo, debamos almacenar el id de forma manual:   
```java
template.update(Category.class)
	.matching(where("id").is(category.id))
	.apply(new Update().push("products", product.id))

```
No es un gran inconveniente la opción anterior aunque mediante un decorador podemos evitarnos el uso de product.id y dejar que el mismo Spring Boot se encargue de buscarnos la variable de referencia. Por cierto, en este caso estamos utilizando `template` que es un objeto propio de **mongo** que nos da acceso global a nuestras colecciones; no es nuestro repositorio.   
El decorador al que hacemos referencia es `@DocumentReference` y se vería de la siguiente forma:   
```java
public class Category{
    //...
	@DocumentReference
	private List<Product> products;
}

public class Product {
	@DocumentReference(lazy = true) // Para evitar cargar los datos de la categoría antes de acceder al objeto.
	private Category category;
}
```
La diferencia con la forma manual es prácticamente indistinguible. Donde único podría divisarse es en el método de update anterior; donde debíamos definir dentro del objeto producto cual era el valor a almacenar. En este caso, nos quitamos esa responsabilidad como programadores y se la damos al framework:   
```java
template.update(Category.class)
	.matching(where("id").is(category.id))
	.apply(new Update().push("products", product))

```
En algunos casos es preferible no almacenar la lista de elementos; es decir, no almacenar el listado de productos dentro del objeto Category. Para esto solo debemos adicionar el decorador `ReadOnlyProperty` y agregarle una opción a `DocumentReference`:   
```java
public class Category{
    //...
	@ReadOnlyProperty
	@DocumentReference(lookup="{'category':?#{#self._id}}")
	private List<Product> products;
}
```
Si utilizamos este acercamiento, entonces evitamos el trabajo de tener que actualizar la categoría cada vez que un producto sea añadido. Además, gracias al añadido lookup, cuando accedamos a una categoría, el framework se encargará de buscar los productos que posean en el atributo `category` el id de la categoría actual (`#self.\_id` hace referencia al id de la categoría que estemos llamando en el momento).    
Desgraciadamente, después de varias pruebas y búsquedas, no es posible utilizar `@DocumentRef` en nuestra aplicación. Esto se debe a que por defecto, el lookup es una acción bloqueante y va en contra de los principios de `webFlux`. Por lo tanto, nunca nos devolverá los datos correspondientes a la categoría. Cuáles son las alternativas? Podemos utilizar la opción de incrustar directamente la categoría dentro del producto y fue lo primero que pensamos. No seleccionamos esta opción no porque fuese mala idea, sino porque queremos hacerlo de la forma tradicional por si en algún momento nos encontramos proyectos con estas características.   
Entonces, la idea fue mantener la estructura que teníamos pero quitamos la anotaciones `@DocumentRef` del código.   
   
