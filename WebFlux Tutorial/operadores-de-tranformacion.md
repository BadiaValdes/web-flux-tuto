 # Operadores de tranformación   
   
**Map** → Permite transformar los elementos del flujo de forma síncrona. Devuelve un Mono +-   
```java
 public void map(){
        /List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .map(p -> {
                    p.setEdad(p.getEdad() + 10);
                    return p;
                })
                .subscribe(p -> log.info(p.toString()));/

        //crea un flujo a partir de un rango de numeros y luego usa map para modificar los datos en el
        Flux<Integer> fx = Flux.range(0,10);
        Flux<Integer> fx2 = fx.map(x -> x + 10);
        fx2.subscribe(x -> log.info("X :" + x));
    }


```
**FlatMap** → Mapea de forma asíncrona y además permite devolver otro flujo de datos. Devuelve un flux.   
```java
public void flatMap(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .flatMap(p -> {
                    p.setEdad(p.getEdad()+10);
                    return Mono.just(p);
                })
                .subscribe(p -> log.info(p.toString()));
    }


```
**FlatMapMany** → Similar al anterior, pero permite la transformación de Mono a Flux.   
```java
public Flux<String> fruitMonoFlatMapMany() {
        return Mono.just("Mango")
                .flatMapMany(s -> Flux.just(s.split("")))
                .log();
    }


```
**GroupBy** → Agrupa los datos del flujo según una condición   
```java
public void groupBy(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(1, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .groupBy(Persona::getIdPersona)
                .flatMap(idFlux -> idFlux.collectList())
                .subscribe(x -> log.info(x.toString()));
    }


```
**switchIfEmpty** → Cambiamos a un publicador alternativo en caso de que exista un error (dato vacio):   
```java
public Flux<String> fruitsFluxTransformSwitchIfEmpty(int number) {

        Function<Flux<String>,Flux<String>> filterData
                = data -> data.filter(s -> s.length() > number);

        return Flux.fromIterable(List.of("Mango","Orange","Banana"))
                .transform(filterData)
                .switchIfEmpty(Flux.just("Pineapple","Jack Fruit")
                            .transform(filterData))
                .log();

    }


```
**transform** → Transforma un flux para generar otro. Le aplica un filtro al flux.   
```java
public Flux<String> fruitsFluxTransform(int number) {

        Function<Flux<String>,Flux<String>> filterData
                = data -> data.filter(s -> s.length() > number);

        return Flux.fromIterable(List.of("Mango","Orange","Banana"))
                .transform(filterData)
                .log();
                //.filter(s -> s.length() > number);
    }


```
**defaultIfEmpty** → Provee un valor en caso que la secuencia se complete sin emitir ningún dato   
```java
public Flux<String> fruitsFluxTransformDefaultIfEmpty(int number) {

        Function<Flux<String>,Flux<String>> filterData
                = data -> data.filter(s -> s.length() > number);

        return Flux.fromIterable(List.of("Mango","Orange","Banana"))
                .transform(filterData)
                .defaultIfEmpty("Default")
                .log();

    }


```
collectList() → Convierte a Mono el flujo que se esté trabajando   
```java
public Mono<List<String>> convertFluxToMono() {
	List<Usuario> userList = new ArrayList<>();
	userList.add("Pedro","Pica Piedra");
	Flux.fromIterable(userList).collectList().subscribe(...);
}
```
   
