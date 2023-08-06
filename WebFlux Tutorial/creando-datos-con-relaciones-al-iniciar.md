 # Creando datos con relaciones al iniciar   
   
En el documento anterior [Ejecutando instrucciones al le…](ejecutando-instrucciones-al-levantar-la-aplicaci.md) hablamos de como crear datos al inicializar la aplicación. En esa ocasión solo teníamos una colección creada (Product). A medida que la aplicación creció, se añadió una nueva llamada Category. La relación entre ambas colecciones consiste en que un producto tiene una categoría y una categoría puede estar en varios productos. Cómo trabajamos el tema de relaciones de mongo con Spring Boot lo podemos ver en el documento [Mongo DB relations](mongo-db-relations.md). A continuación veremos varios de los acercamientos que se tuvieron en cuenta para la creación de los datos iniciales:   
 ## Datos iniciales:   
Como punto de partida definiremos los datos a insertar en las colecciones y para evitar datos duplicados, estaremos eliminando las colecciones:   
```java
reactiveMongoTemplate.dropCollection(CollectionNames.PRODUCT_COLLECTION).subscribe();        reactiveMongoTemplate.dropCollection(CollectionNames.CATEGORY_COLLECTION).subscribe();

Flux<Category> categoryFlux = Flux.just(
      Category.builder().name("Games").build(),
      Category.builder().name("Grocery").build(),
      Category.builder().name("Computer").build()
 );

Flux<Product> productFlux =  Flux.just(
      Product.builder().price(200.0).name("Monitor RCA").build(),
      Product.builder().price(210.0).name("Monitor Samsung").build(),
      Product.builder().price(120.0).name("Monitor AOC").build(),
      Product.builder().price(120.0).name("Monitor BQ").build()
);
```
Para eliminar las colecciones hacemos uso de `reactiveMongoTemplate` que nos da acceso directo a todas las funciones disponibles sobre las colecciones de MongoDB. Posteriormente solo creamos los Flux correspondientes a categoría y productos. Para la creación de los datos en MongoDB debemos tener en cuenta que se deben guardar primero las categorías y posteriormente los productos. Esto se debe a que los productos dependen de la categoría para poder ser almacenados correctamente.   
 ## Variante 1 - Productos por cada categoría   
Este enfoque de guardado es el menos complejo pero crea información innecesario dentro de nuestra base de datos. Como bien su nombre lo dice, por cada categoría que tengamos crearemos 4 productos; por lo que al final tendremos 12 productos almacenados y con datos erróneos:   
```java
categoryFlux
        .flatMap(category -> reactiveMongoTemplate.save(category))
        .flatMap(data -> productFlux)
        .flatMap(product -> productRepository.save(product))
        .subscribe(product -> log.info(String.format("Producto id: %s", product.getId())));
```
Bastante sencillo de entender no?? Si tienes problemas entendiendo el uso de `flatMap`, puede ver los [Operadores](operadores.md) que nos brinda webFlux. La idea general de este método es recorrer las categorías mediante el operador mencionado anteriormente e irlas guardando en la base de datos. Posteriormente usando otro `flatMap` devolvemos un `Flux` de productos y de la misma forma que hicimos con la categoría, salvamos los productos.   
Esta variante tiene como principal problema que no almacena la categoría dentro del producto, por lo que no estaríamos cumpliendo con nuestro principal objetivo. Para resolverlo, podríamos agregar un `flatMap` a la variable `productFlux` y realizar la asignación; pero esto conlleva una mala práctica (operadores anidados). En la variante dos veremos como evitar el uso de operadores anidados para resolver este inconveniente.   
 ## Variante 2 -  Cantidad de productos por categoría   
En este caso, estaremos utilizando el operador `ZipWith` para anidar nuestros flujos y de esta forma asignar la categoría al producto. El defecto que podemos encontrar a esta implementación es, como bien lo dice su título, que solo se crearan tantos productos como categorías tengamos. Es decir, si tenemos 3 categorías y 4 productos; solo serán almacenados en base de datos 3 productos. De esta forma, podremos decir que se crearán, del segundo flujo, a lo sumo la misma cantidad de elementos que existan en el primer flujo.   
```java
categoryFlux
        .flatMap(category -> reactiveMongoTemplate.save(category))
        .zipWith(productFlux)
        .flatMap(data -> {
            data.getT2().setCategory(data.getT1());
            return productRepository.save(data.getT2());
        })
        .subscribe(product -> log.info(String.format("Producto created")));
```
El primer `flatMap` no es diferente al visto en la variante 1, por lo que pasaremos directamente a `zipWith`. Este operador, como bien podemos leer en [Operadores](operadores.md), nos permite unir dos flujos formando una tupla de datos. Dicha tupla la estamos utilizando en el `flatMap` para la modificación de la categoría de los productos. Al principio de esta variante comentamos la deficiencia que posee utilizar este enfoque, por lo que le daremos solución en la variante 3.   
 ## Variante 3 - Flux To Mono   
En esta variante solo asignaremos la categoría computadora a los productos. Para lograr este cometido, debemos convertir todo el flujo en Mono y no Flux. Este enfoque no es erróneo, pero la forma en que lo implementamos posee una muy mala práctica en la programación reactiva. Ya varemos posteriormente en la variante 4 como resolverlo.   
```java
categoryFlux
        .flatMap(category -> reactiveMongoTemplate.save(category))
        .filter(category -> category.getName().equalsIgnoreCase("Computer"))
        .next()
        .zipWith(productFlux.collectList(), (t1, t2) -> {
            log.info("Inside zipWith");
            log.info(String.format("Array size %s", t2.size()));
            for (Product t:t2) {
                log.info("Inside loop");
                t.setCategory(t1);
                reactiveMongoTemplate.save(t).subscribe();
            }
            return Mono.just(t1);
        })
        .subscribe(product -> log.info(String.format("Producto created")));
```
Antes de realizar la operación `zipWith`, vemos como utilizamos el operador filter para buscar la categoría correspondiente a "**Computer**". Usamos el operador `next()` para solo obtener el primer valor del flujo; este es el que se encarga de convertir en Mono todo el flujo. Posteriormente usamos el `zipWith`, pero a diferencia de la variante anterior, podemos observar que la variable `productFlux` se convierte en una lista gracias a la llamada al método `collectList`; de esta forma estamos convirtiendo el `productFlux` en `productMono` por así representarlo.   
El `zipWith` además de permitir la unión de dos flujos, nos permite interactuar, mediante una función lambda, con las tuplas generadas. Esta función se encarga de recorrer toda la lista de productos y asignarle el primer valor de la tupla (categoría). Este acercamiento no es el más adecuado ya que estamos devolviendo un Mono en vez de un Flux como debería ser; además que estamos realizando una subscripción directamente dentro de otra subscripción.   
 ## Variante 4 - Flux To Mono To Flux   
Esta variante viene a resolver el problema creado anteriormente (operadores anidados):   
```java
categoryFlux
        .flatMap(category -> reactiveMongoTemplate.save(category))
        .filter(category -> category.getName().equalsIgnoreCase("Computer"))
        .next()
        .zipWith(productFlux.collectList())
        .flatMapMany(t1 -> Flux.fromIterable(t1.getT2())
                    .doOnNext(data -> data.setCategory(t1.getT1()))
        )
        .flatMap(data -> productRepository.save(data))
        .subscribe(product -> log.info("Producto created"));
```
Mantenemos la conversión de `Flux` a `Mono`, pero mediante el operador `flatMapMany` volvemos a convertir el flujo en `Flux`. Para evitar la subscripción, dentro del `flatMapMany`  utilizamos el `doOnNext` que nos permitirá realizar una operación por cada elemento que se emita en nuestra conversión de `Mono` a `Flux` (`Flux.formIterable`).   
En nuestro proyecto nos quedamos con la variante 4.    
   
   
