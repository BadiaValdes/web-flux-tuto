 # WebFlux Circuit Breaker   
   
 ## Circuit Breaker   
En la arquitectura de microservicios, puede existir muchos problemas, de conexión por ejemplo, entre dos servicios; lo que llevaría a un error del sistema o demora en la respuesta de la petición. Pensémoslo por un momento, si tenemos varios microservicios y uno de ellos falla, puede provocar un efecto en cascada que inhabilitaría por completo el sistema.   
Dicho escenario puede ser evitado gracias al patrón `circuit breaker`. Este entra en la categoría de los patrones de sostenibilidad y su función principal es prevenir el fallo en cascada a los cuales se pueden ver sujetos los microservicios. Si queremos buscar una analogía con un patrón ya conocido, podemos mencionar el patrón Proxy. Este crea un punto de entrada y salida de la información; y extendiendo este comportamiento, podemos prevenir que existan errores en las peticiones entre microservicios.   
Pero, cómo funciona este patrón? En párrafo anterior nos permitió crearnos una idea, pero en si, todavía no sabemos que hace este patrón. Como bien se mencionó, este crea una especie de proxy de acceso al microservicio y cuenta la cantidad de peticiones fallidas que se han  realizado. En caso que las peticiones hayan llegado a una cantidad en específico (definido por el programador), el microservicio objetivo dejará de estar accesible para los demás y pasado un tiempo (definido por el programador) volverá a intentar hacer peticiones a ese microservicio.  Si después de intentar conectar nuevamente se mantiene inaccesible, volverá a realizar todo el proceso descrito anteriormente.   
Parecido a los circuitos eléctricos de la vida real, este patrón posee tres estados: Cerrado, abierto y semi abierto.   
- Cerrado   
![Untitled.png64b04f89206f632fc44b6f48](files\untitled.png64b04f89206f632fc44b6f48)    
- Abierto   
![Untitled 1.png64b04f89206f632fc44b6f49](files\untitled-1.png64b04f89206f632fc44b6f49)    
- Semi abierto   
![Untitled 2.png64b04f89206f632fc44b6f4a](files\untitled-2.png64b04f89206f632fc44b6f4a)    
   
 ## WebFlux + Circuit Breaker   
 ### Librerías para implementar este patrón (JAVA)   
- hystrix (depreciado) → Orientada a objeto   
- resilence4j (el nuevo) → Este es el que vamos a utilizar   
   
 ### Dependencias de spring boot   
- `spring-cloud-starter-circuitbreaker-reactor-resilience4j`   
- `spring-boot-starter-aop` → Muy importante debido a que el patrón no funcionará sin esto.   
   
 ## Variante 1   
 ### Configuración de circuit breaker (YAML)   
``` YAML
resilience4j:
    circuitbreaker:
        instances:
            mockService:
                slidingWindowSize: 3
                slidingWindowType: COUNT_BASED
                #waitDurationInOpenState: 5
                waitInterval: 10000
                failureRateThreshold: 50
                permittedNumberOfCallsInHalfOpenState: 5
                registerHealthIndicator: true
                #register-health-indicator: true
                allowHealthIndicatorToFail: true
        configs:
            default:
                registerHealthIndicator: true

management.health.circuitbreakers.enabled: true
management:
  endpoint:
    health:
        show-details: always

downstream:
    mock:
        base: http://localhost:63553/
        path: v1/mock/downstream
        url: ${downstream.mock.base}${downstream.mock.path}

```
 ### Ejemplo de uso en el código   
``` java
@CircuitBreaker(name = "mockService", fallbackMethod = "fallback")
    public Mono<MockServiceResponse> getMockServiceResponse() {
        return mockServiceWebClient.get()
                .uri(DOWNSTREAM_PATH)
                .retrieve()
                .bodyToMono(MockServiceResponse.class)
                .doOnError(ex -> {
                    throw new RuntimeException("the exception message is - "+ex.getMessage());
                });
    }

    public Mono<MockServiceResponse> fallback(Throwable ex) {
        //Arrays.stream(ex.getStackTrace()).forEach(System.out::println);
        System.out.println("---> "+ex.getMessage());
        MockServiceResponse mockServiceResponse = new MockServiceResponse();
        mockServiceResponse.setError(true);
        return Mono.just(mockServiceResponse);
    }

```
 ## Variante 2 → Usada en betterprogramming y me gusta más.   
 ### Bean   
Primero debemos registrar nuestra configuración global de Circuit Breaker. En este caso, estaremos utilizando la configuración que viene por defecto en la librería:   
``` java
@Bean
public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build()).build());
}

```
Del código podemos extraer dos elementos importantes:   
- `circuitBreakerConfig` → Aquí dentro va la configuración que utilizará la librería para trabajar con el patrón.   
- `timeLimiterConfig` → Aquí definimos el tiempo que demorará en cerrarse el circuito nuevamente.   
   
En caso de querer crear una configuración propia, debemos añadir el `circuitBreakerConfig`  el nombre que estará utilizando la misma:   
``` java
@Bean
public Customizer<ReactiveResilience4JCircuitBreakerFactory> customerServiceCusomtizer() {
  return factory -> {
    factory.configure(builder -> builder
      .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(2)).build())
      .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults()), "customer-service");
  };
}

```
A pesar que es la misma configuración que vimos anteriormente, estamos creando una nueva. Es decir, podemos tener varias configuraciones de Circuit Breaker en nuestro proyecto, lo que una solo puede ser default, mientras que las otras deben nombrarse (`"customer-service"`).   
Como no puede faltar, debemos añadir la configuración correspondiente en el archivo `applications.properties`:   
 ## Application.properties   
``` yaml
resilience4j.circuitbreaker:
  instances:
    customer-service:
      failureRateThreshold: 50
      minimumNumberOfCalls: 10
      slidingWindowType: TIME_BASED
      slidingWindowSize: 10
      waitDurationInOpenState: 50s
      permittedNumberOfCallsInHalfOpenState: 3

```
Solo se están definiendo las propiedades para el patrón que se haya registrado bajo el nombre de `"customer-service"`. También hay que tener en cuenta que resilience4j no solo nos permite trabajar directamente con `circuitbreaker`; sino también con `ratelimiter` y `bulkhead`. Nos desviamos un poco del tema. Sigamos con las propiedades vistas en el código anterior:   
- `failureRateThreshold` → define el porciento de peticiones que pueden fallar. Al alcanzar el porcentaje definido, el circuito pasará al estado abierto.   
- `minimumNumberOfCalls` → define el número mínimo de peticiones que se deben realizar antes de comenzar a calcular el valor anteriormente mencionado.   
- `slidingWindowType` → define la forma de guardar las llamadas realizadas antes de pasar al estado cerrado. Puede poseer uno de los siguiente valores:   
    - `TIME\_BASED`   
    - `COUNT\_BASE`   
- `waitDurationInOpenState`→ el tiempo que debe esperar el circuito para pasar de abierto a semi abierto.   
- `permittedNumberOfCallsInHalfOpenState`→ cuantas llamadas se pueden realizar cuando el estado esté semi abierto. Este propiedad va en conjunto con `slidingWindowType` ya que en el ejemplo anterior, solo se pueden realizar 3 llamadas a la api en un plazo de 10 segundos.   
   
 ## Implementación   
En este caso se muestra un ms que utiliza webFlux y webClient para conectar con otro microservicio construido mediante una RestApi convencional.   
``` java
@RestController
@Slf4j
@RequiredArgsConstructor
public class CustomerClientController {

    private final WebClient webClient;
    private final ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;

    @PostMapping("/customers")
    public Mono<CustomerVO> createCustomer(CustomerVO customerVO){
        return webClient.post()
                .uri("/customers")
                //.header("Authorization", "Bearer MY_SECRET_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(customerVO), CustomerVO.class)
                .retrieve()
                .bodyToMono(CustomerVO.class)
                .timeout(Duration.ofMillis(10_000))
                .transform(it -> {
                    ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create("customer-service");
                    return rcb.run(it, throwable -> Mono.just(CustomerVO.builder().build()));
                });
    }

    @GetMapping("/customers/{customerId}")
    public Mono<CustomerVO> getCustomer(@PathVariable String customerId) {
        return webClient
                .get().uri("/customers/" + customerId)
                .retrieve()
                .bodyToMono(CustomerVO.class)
                .transform(it -> {
                    ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create("customer-service");
                    return rcb.run(it, throwable -> Mono.just(CustomerVO.builder().build()));
                });
    }

    @PutMapping("/customers/{customerId}")
    public Mono<CustomerVO> updateCustomer(@PathVariable String customerId, CustomerVO customerVO){
        return webClient.put()
                .uri("/customers/" + customerVO.getCustomerId())
                //.header("Authorization", "Bearer MY_SECRET_TOKEN")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(customerVO), CustomerVO.class)
                .retrieve()
                .bodyToMono(CustomerVO.class)
                .transform(it -> {
                    ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create("customer-service");
                    return rcb.run(it, throwable -> Mono.just(CustomerVO.builder().build()));
                });
    }

    @DeleteMapping("/customers/{customerId}")
    public Mono<String> deleteCustomer(@PathVariable String customerId){
        return webClient.delete()
                .uri("/customers/" + customerId)
                .retrieve()
                .bodyToMono(String.class)
                .transform(it -> {
                    ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create("customer-service");
                    return rcb.run(it, throwable -> Mono.just(customerId));
                });
    }
}

```
En el código se utiliza la librería `webClient` (más adelante veremos el código) para realizar llamadas a la `restApi`. Lo más importante en este caso es como una vez recibida la información, se realiza la llamada al circuit breaker para controlar el tiempo de espera de la petición.   
``` java
ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create("customer-service");

```
Se crea primero una instancia de ReactiveCircuitBreaker y como parámetro, se le pasa el nombre de la configuración creada; en este caso se está utilizando la configuración personalizada. Posteriormente se realiza la acción de ejecución del circuito:   
``` java
rcb.run(it, throwable -> Mono.just(customerId));

```
Donde recibe primero los valores provenientes de la petición realizada a la API y en caso de fallar o cumplirse cualquiera de las restricciones creadas en la configuración, devolverá un error. En todos los casos, menos en el eliminar, si surge un error, se devuelve una instancia vacia del objeto que se estaba buscando.   
Como se prometió anteriormente, a continuación se muestra la configuración del `WebClient`:   
``` java
@Bean
    public WebClient getWebClient(){
        return WebClient.builder()
                .baseUrl("http://localhost:8500")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

```
Antes de terminar con esta variante, veamos como implementar el patrón cuando el endpoint al que estamos llamando es void. Supongamos que tenemos el siguiente endpoint para eliminar:   
``` java
@DeleteMapping(value = "/{customerId}")
public ResponseEntity deleteCustomer(@PathVariable String customerId) throws Exception {
    customerService.deleteCustomer(customerId);
    return ResponseEntity.noContent().build();
}

```
Este método devuelve una respuesta sin body, vacía por así decirlo. Para lograr utilizar este patrón, tenemos dos enfoques, el que viene por defecto con la libraría y utilizado en ambientes no reactivos:   
``` java
CircuitBreaker circuitBreaker = circuitBreakerFactory.create("customer-service");
CheckedRunnable runnable = () -> customerClient.deleteCustomer(customerId);
Try.run(circuitBreaker.decorateCheckedRunnable(runnable)).get();

```
Aunque a nosotros solo nos incumbe el código para las llamadas reactivas. En este caso, no existe una solución por defecto y la que mostraremos a continuación, es tomada del blog `betterprogramming` que se encuentra en la bibliografía:   
``` java
public Mono<String> deleteCustomer(@PathVariable String customerId){
    return webClient.delete()
            .uri("/customers/" + customerId)
            .retrieve()
            .bodyToMono(String.class)
            .transform(it -> {
                ReactiveCircuitBreaker rcb = reactiveCircuitBreakerFactory.create("customer-service");
                return rcb.run(it, throwable -> Mono.just(customerId));
            });
}

```
El cambio realizado se basa en vez de retornar un `Mono<void>` se decide utilizar un `String`. Ya, con eso tenemos el atajo para lograr trabajar con el patrón y métodos que no retornen nada.   
 ## Bibliografía   
[Attention Required! \| Cloudflare](https://resilience4j.readme.io/docs/getting-started-3)    
[Attention Required! \| Cloudflare](https://neuw.medium.com/resilence4j-circuit-breaker-spring-webflux-6731f7257965)    
[spring-boot-webclient-resilience4j](https://github.com/wenqiglantz/spring-boot-webclient-resilience4j)    
