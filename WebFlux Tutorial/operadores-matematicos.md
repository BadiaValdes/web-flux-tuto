 # Operadores Matemáticos   
   
**Average** → promedio de un conjunto de datos   
```java
public void average(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 28));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .collect(Collectors.averagingInt(Persona::getEdad))
                .subscribe(x -> log.info(x.toString()));
    }


```
**Count** → Cuanta la cantidad de elementos en el flujo   
```java
public void count(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 28));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .count()
                .subscribe(x -> log.info("Cantidad:" + x));
    }


```
Min → obtienes el menor elemento   
```java
public void min(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 28));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .collect(Collectors.minBy(Comparator.comparing(Persona::getEdad)))
                .subscribe(p -> log.info(p.get().toString()));

    }


```
**Sum** → sumas los elementos de un flujo de datos   
```java
public void sum(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 28));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .collect(Collectors.summingInt(Persona::getEdad))
                .subscribe(x -> log.info("suma: " + x));

    }


```
**Summarizing** → Realiza un resumen de operaciones   
```java
public void summarizing(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 28));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .collect(Collectors.summarizingInt(Persona::getEdad))
                .subscribe(x -> log.info("Resumen: " + x));
    }


```
