 # Operador combinación   
   
 #    
La combinación permite como su nombre indica, combinar varios flujos de datos en uno solo. Es decir, unir varias llamadas flux o mono en una sola e iterar sobre ellas. A continuación vamos a poner ejemplos de este tipo de operador:   
**MERGE**:   
```java
public void merge(){

        List<Persona> personas1 = new ArrayList<>();
        personas1.add(new Persona(1, "heri", 27));
        personas1.add(new Persona(2, "may", 25));
        personas1.add(new Persona(3, "pepe", 29));

        List<Persona> personas2 = new ArrayList<>();
        personas2.add(new Persona(4, "pablito", 27));
        personas2.add(new Persona(5, "clavo", 25));
        personas2.add(new Persona(6, "unclavito", 29));

        List<Venta> ventas =  new ArrayList<>();
        ventas.add(new Venta(1, LocalDateTime.now()));

        Flux<Persona> fx1 = Flux.fromIterable(personas1);
        Flux<Persona> fx2 = Flux.fromIterable(personas2);
        Flux<Venta> fx3 = Flux.fromIterable(ventas);

        Flux.merge(fx1, fx2, fx3)
                .subscribe(p -> log.info(p.toString()));
    }


```
En este ejemplo, se emiten primero todos los flujos de fx1, posteriormente los de fx2 y termina con fx3. Es decir, va emitiendo los valores que se le pasen por parámetro de forma ordenada.   
**MergeWith** → permite que un flujo (`**Flux**`) se una con otro   
```java
public Flux<String> fruitsFluxMergeWith() {
        var fruits = Flux.just("Mango","Orange");
        var veggies = Flux.just("Tomato","Lemon");
        return fruits.mergeWith(veggies);
    }


```
**ZIP** → Se espera que por lo menos todos los flujos tengan valores   
```java
public void zip(){

        List<Persona> personas1 = new ArrayList<>();
        personas1.add(new Persona(1, "heri", 27));
        personas1.add(new Persona(2, "may", 25));
        personas1.add(new Persona(3, "pepe", 29));

        List<Persona> personas2 = new ArrayList<>();
        personas2.add(new Persona(4, "pablito", 27));
        personas2.add(new Persona(5, "clavo", 25));
        personas2.add(new Persona(6, "unclavito", 29));

        List<Venta> ventas =  new ArrayList<>();
        ventas.add(new Venta(1, LocalDateTime.now()));

        Flux<Persona> fx1 = Flux.fromIterable(personas1);
        Flux<Persona> fx2 = Flux.fromIterable(personas2);
        Flux<Venta> fx3 = Flux.fromIterable(ventas);

        Flux.zip(fx1,fx2, fx3)
                .subscribe(x -> log.info(x.toString()));

    }


```
En este caso a pesar de que se muestre que solo se devuelve un valor, internamente x posee la capacidad de llamar a cada una de las emisiones (del momento) de los valores pasados por parámetros. Es decir, que X la primera vez tendrá la primera emisión de fx1, fx2 y fx3.   
**ZipWith** → Similar a los anteriores pero lo desencadena un flujo (es decir, lo llama una variable de tipo Mono o Flux) y no la clase en si:   
```java
public void zipWith(){

        List<Persona> personas1 = new ArrayList<>();
        personas1.add(new Persona(1, "heri", 27));
        personas1.add(new Persona(2, "may", 25));
        personas1.add(new Persona(3, "pepe", 29));

        List<Persona> personas2 = new ArrayList<>();
        personas2.add(new Persona(4, "pablito", 27));
        personas2.add(new Persona(5, "clavo", 25));
        personas2.add(new Persona(6, "unclavito", 29));

        List<Venta> ventas =  new ArrayList<>();
        ventas.add(new Venta(1, LocalDateTime.now()));

        Flux<Persona> fx1 = Flux.fromIterable(personas1);
        Flux<Persona> fx2 = Flux.fromIterable(personas2);
        Flux<Venta> fx3 = Flux.fromIterable(ventas);
		
		// Siempre se debe poner primero, en la función de zip, el valor del flujo principal.
        fx1.zipWith(fx3, (p1, v1) -> String.format("Flux1: %s, Flux3: %s", p1, v1))
                .subscribe(x -> log.info(x.toString()));
    }


```
Emite una tupla con los valores que se estén combinando. Es decir, la primera emisión de fx1 vendrá acompañada de la primera emisión de fx3.   
**ZipWhen** → Combina dos flujos cuando se cumpla una condición en específico   
```java
Flux<Usuario> usuarios = Flux.just(
    new Usuario("Juan", "juan@example.com", "administrador"),
    new Usuario("Pedro", "pedro@example.com", "usuario")
);

Flux<Rol> roles = Flux.just(
    new Rol("administrador", Arrays.asList("crear", "editar", "eliminar")),
    new Rol("usuario", Arrays.asList("ver"))
);

usuarios
    .filter(usuario -> usuario.getRol().equals("administrador")) // Filtrar solo los usuarios con rol "administrador"
    .flatMap(usuario -> roles
        .filter(rol -> rol.getNombre().equals(usuario.getRol())) // Filtrar solo el rol correspondiente al usuario
        .next() // Obtener el primer rol que cumple con el predicado
        .zipWhen(rol -> Mono.just(usuario), (rol, usuario) -> new UsuarioRol(usuario, rol))) // Combinar el usuario y el rol
    .subscribe(usuarioRol -> System.out.println(usuarioRol.getUsuario().getNombre() + " tiene el rol " + usuarioRol.getRol().getNombre()));


```
En este caso solo se unen los flujos cuando se cumpla la condición que el rol es administrador.   
**Concat** → concatena los elementos de varios flujos.   
```java
public Flux<String> fruitsFluxConcat() {
        var fruits = Flux.just("Mango","Orange");
        var veggies = Flux.just("Tomato","Lemon");

        return Flux.concat(fruits,veggies);
    }


```
**ConcatWith** → concatena dos flujos, uno de los flujos es el que llama a este método. En caso que sea un flujo Mono, se vuelve Flux.   
