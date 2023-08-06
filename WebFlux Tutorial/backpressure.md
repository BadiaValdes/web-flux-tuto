 # BackPressure   
   
Backpressure o contra presión nos permite definir la cantidad máxima que se le puede pedir al productor. Esto es utilizado para poder modificar la forma en que se quieren tratar los datos que se envíen del observable para evitar una sobrecarga de los recursos de la computadora.   
   
Variante 1:   
```java
Flux.range(1,10).subscribe(new Subscriber<Integer>() {
	private Subscription s;

	private Integer x = 5; // Cantidad de elementos que vamos a pedir por vez
	private Integer procesados = 0; // Me permitirá conocer cuantos elementos del request han sido procesado, para cuando llegue al máximo, volver a definir cuantos elementos deben consumirse.
	
	@Override 
	// Aquí definimos que va a suceder cuando nos suscribamos al observable
	public void onSubscribe(Subscription s){
		this.s = s;
		s.request(x) // Nos permite definir cuantos elementos queremos procesar de la suscripción. Estos elementos son fijos, es decir, si definimos 2, solo se manejaran 2 elementos.
// Para poder recorrer la suscripción hasta el final mediante el uso del request, tenemos que reasignarlo en el onNext
	}

	// Si no queremos tocar algún comportamiento, simplemente no lo sobrescribimos.
	@Override
	// Comportamiento por cada elemento que se reciba
	public void OnNext(Integer t){
		log.info(t.toString());
		procesados++;
		if(procesados == x){
			procesados = 0; // Reiniciamos el contador.
			s.request(x); // Le decimos que debe pedir X cantidad de elementos nuevamente.
		}
	}

	@Override
	// Comportamiento cuando se encuentre un error
	public void onError(Throwable t){
		// Codificar algo aquí
	}

	@Override
	// Que sucede cuando el sistema se completa de forma satisfactoria.
	public void onComplete() {
		// Codificar algo aquí
	}
})
```
   
Variante 2 → Esta variante es la más corta y sencilla, pero se puede utilizar la de arriba si quieres modificar el comportamiento de los suscribibles.   
```java
Flux.range(1,10)
	.limitRate(5) //Permite definir la cantidad de elementos que se van a llamar por lote. Lo mismo de la variante 1 pero más bonito 
	.subscribe(data -> log.Info(data)); 
```
   
