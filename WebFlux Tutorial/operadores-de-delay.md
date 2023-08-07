 # Operadores de delay   
   
delayElements → Para demorar la emisión del elemento.   
```java
Flux<Integer> rango = Flux.range(0,4).delayElements(Durations.ofSeconds(1))
```
interval → Permite crear un objeto de tipo observable.   
```java
Flux<Integer> rango = Flux.range(0,4);
Flux<Long> retraso = Flux.interval(Durations.ofSeconds(1))

// En este caso, solo se emite ra cuando se realiza el zipWith. Pq??
// Sencillo, retraso solo emite valores de tipo long cada un segundo y solo nos interesa conocer el valor del rango por cada emisión de retraso.
rango.zipWith(retraso, (ra, re) -> ra).doOnNext(i -> log.info(i.toString()))

```
   
