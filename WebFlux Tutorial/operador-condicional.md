 # Operador Condicional   
   
**defaultIfEmpty →** Retorna el elemento indicado en caso que sea vacía la respuesta de flux o mono.   
```java
public void defaultIfEmpty() {
        //Mono.just(new Persona(1, "heri", 27))
        //Mono.empty()
        Flux.empty()
                .defaultIfEmpty(new Persona(0, "default", 99))
                .subscribe(x -> log.info(x.toString()));
    }

```
**takeUntil →** Emite el flujo hasta encontrar un valor definido, en ese caso, para la emisión.   
```java
public void takeUntil(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 28));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .takeUntil(p -> p.getEdad() > 26)
                .subscribe(x -> log.info(x.toString()));
    }

```
**timeout** → Lanza un error cuando el tiempo de espera entre emisiones en mayor que uno determinado por el programador:   
```java
public void timeout() throws InterruptedException {
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 28));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                //si los segundos aca son menores que 2 on va a haber problemas
                .delayElements(Duration.ofSeconds(3))
                .timeout(Duration.ofSeconds(2))
                .subscribe(x -> log.info(x.toString()));

        //para que el hilo principal de la app siga viviendo hasta que los demas procesos asincronos terminen su ejecucion
        Thread.sleep(10000);

    }

```
