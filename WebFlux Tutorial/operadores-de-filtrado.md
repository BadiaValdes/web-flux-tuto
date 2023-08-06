 # Operadores de filtrado   
   
**Filter** → filtra los elementos en correspondencia con un criterio   
```java
public void filter(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .filter(p -> p.getEdad() > 28)
                .subscribe(p -> log.info(p.toString()));
    }


```
**Distinct** → devuelve solo aquellos valores que no estén duplicados (se debe implementar tanto el equals como el hashCode en el DTO o DAO).   
```java
public void distinct(){
        //ejemplo 1
        Flux.fromIterable(List.of(1,1,2,2))
                .distinct()
                .subscribe(p -> log.info(p.toString()));

        //ejemplo 2
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(1, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .distinct()
                .subscribe(p -> log.info(p.toString()));
    }


```
**Take** → Toma una X cantidad de elementos del flujo   
```java
public void take(){
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .take(2)
                .subscribe(p -> log.info(p.toString()));
    }


```
**TakeLast** → Toma los últimos X valores del flujo   
```java
public void takeLast() {
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .takeLast(1)
                .subscribe(p -> log.info(p.toString()));
    }


```
**Skip** → Se salta una X cantidad de elementos del flujo   
```java
public void skip() {
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .skip(1)
                .subscribe(p -> log.info(p.toString()));
    }


```
**SkipLast** → Se salta los últimos X elementos.   
```java
public void skipLast() {
        List<Persona> personas = new ArrayList<>();
        personas.add(new Persona(1, "heri", 27));
        personas.add(new Persona(2, "may", 25));
        personas.add(new Persona(3, "pepe", 29));

        Flux.fromIterable(personas)
                .skipLast(1)
                .subscribe(p -> log.info(p.toString()));
    }


```
