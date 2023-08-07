 # Operadores doOn   
   
doOnNext → permite realizar una operación por cada elemento que provenga del observable.   
```java
Flux.range(0,4).doOnNext(i -> log.info(i));
```
**doOnTerminate **→ permite realizar una operación cuando el flujo terminó de emitirse. Este va a funcionar sin importar si existe algún fallo en el flujo o no.   
```java
Flux.range(0,4).doOnTerminate(i -> log.info(i));
```
   
