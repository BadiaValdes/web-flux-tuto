 # Operadores de Error   
   
> el concatWith permite la concatenación de operadores.   

**Retry** → Si el código falla, lo ejecuta una cantidad de veces definida:   
```java
public void retry(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .concatWith(Flux.error(new RuntimeException("ERROR")))
                .retry(1)
                .doOnNext(x -> log.info(x.toString()))
                .subscribe();
    }


```
**errorReturn** → Permite controlar que lanzar en caso de que exista un error.   
```java
public void errorReturn(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .concatWith(Flux.error(new RuntimeException("ERROR")))
                .onErrorReturn(new Persona(0, "xyz", 99))
                .subscribe(x -> log.info(x.toString()));

    }


```
**errorResume** → En caso de que falle, permite devolver un valor de respaldo y continuar el proceso.   
```java
public void errorResume(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .concatWith(Flux.error(new RuntimeException("ERROR")))
                .onErrorResume(e -> Mono.just(new Persona(0, "xyz", 99)))
                .subscribe(x -> log.info(x.toString()));
    }


```
**errorMap** → En caso de que ocurra un error, permite mapearlo y modificarlo en caso de ser necesario.   
```java
public void errorMap(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .concatWith(Flux.error(new RuntimeException("ERROR")))
                .onErrorMap(e -> new InterruptedException(e.getMessage()))
                .subscribe(x -> log.info(x.toString()));
    }


```
**errorContinue** → permite continuar el flujo de dato si existe un error. El método recibe por parámetros, el error y el próximo elelmento:   
```java
public Flux<String> fruitsFluxOnErrorContinue() {
        return Flux.just("Apple","Mango","Orange")
                .map(s -> {
                    if (s.equalsIgnoreCase("Mango"))
                        throw new RuntimeException("Exception Occurred");
                    return s.toUpperCase();
                })
                .onErrorContinue((e,f) -> {
                    System.out.println("e = " + e);
                    System.out.println("f = " + f);
                });
    }


```
**onError** → Permite realizar una operación cuando surja un error.   
```java
public Flux<String> fruitsFluxOnError() {
        return Flux.just("Apple","Mango","Orange")
                .map(s -> {
                    if (s.equalsIgnoreCase("Mango"))
                        throw new RuntimeException("Exception Occurred");
                    return s.toUpperCase();
                })
                .doOnError(throwable -> {
                    System.out.println("throwable = " + throwable);

                });
    }


```
