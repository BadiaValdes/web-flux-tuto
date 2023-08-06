 # Operadores de creación   
   
 #    
**JUST** → Permite definir una estructura Mono o Flux en correspondencia a una serie de datos que se le pasen por parámetro. ( `fromIterable` tiene el mismo funcionamiento pero es para usarlo con colecciones de datos)   
```java
public void justFrom(){
        Mono.just(new Persona(1, "heri", 27));
        //Flux.fromIterable(coleccion);
        //Observable.just(item);

    }


```
**EMPTY** → Devuelve un flujo vacío   
```java
public void empty(){
        Mono.empty();
        Flux.empty();
        Observable.empty();
    }


```
**RANGE** → Crea un flujo de datos a partir de un rango   
```java
public void range(){
        /el primer numero se considera >= y el segundo numero es <,
        o sea que el ultimo valor no se va a considerar/
        Flux.range(0, 3)
                .doOnNext(i -> log.info("i:" + i))
                .subscribe();
    }


```
**REPEAT** → Repite un flujo de datos una X cantidad de veces.   
```java
public void repeat(){

        List<Persona> personas = new ArrayList<>();

        personas.add(new Persona(1, "heri", 27));

        personas.add(new Persona(2, "may", 25));

        personas.add(new Persona(3, "pepe", 29));



        /*Flux.fromIterable(personas)

                .repeat(3)

                .subscribe(p -> log.info(p.toString()));*/



        Mono.just(new Persona(1, "heri", 27))

                .repeat(3)

                .subscribe(x -> log.info(x.toString()));



    }
```
**fromCallable** → Crear un mono desde la llamada de un método   
```java
public void fromCallable(){
	Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Jhon", "Dee"));
}
```
** Create → Permite la creación de un emisor de observables.**   
```java
// En este caso se esta emitiendo un número cada cierto tiempo. Este método emite de forma infinita. 
Flux.create(emit -> {
	Timer timer = new Timer();
	timer.schedule(new TimerTask() {
		private Integer cont = 0;
		@Override
		public void run(){
			emit.next(++cont);
			// Para pararlo solo debemos añadir
			if(cont > 10) {
				timer.cancel(); // Detiene el temporizador
				emit.complete(); // Detiene la emisión
			} elseif(cont == 9) {
				timer.cancel(); // Detiene el temporizador
				emitter.error("Texto"); // Permite emitir un error en el flujo
			}
		}
	})
}).subscribe(
	next -> {...} // Que sucede cada vez que se emite un elemento
	error -> {...} // manejador de error en el suscripción 
	() -> {} // OnComplete, solo se ejecuta cuando no exista error en el flujo
)
```
   
   
