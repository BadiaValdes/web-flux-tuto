 # Llamadas reactivas HTTP con WebClient   
   
En webFlux tenemos una herramienta llamada WebClient que nos permite realizar llamadas HTTP a APIS de 3ros. Esta herramienta al ser parte del entorno reactivo, devuelve Mono o Flux en cada una de sus peticiones, por lo que se integra sin ningún problema con el ecosistema de trabajo. En este caso, creamos una clase encargada de realizar las llamadas HTTP y dentro configuramos nuestra instancia de webClient.   
```java
@Service
public class CustomWebClient {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final WebClient webClient;
    private HttpClient httpClient = HttpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500)
            .responseTimeout(Duration.ofMillis(500))
            .compress(true);

    public CustomWebClient(){
       this.webClient = WebClient
                .builder() 
                .baseUrl("https://hub.dummyapis.com/") 
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "testA", fallbackMethod = "fallBackMethod")
    public Flux<WebClientProduct> getProducts(){
        return webClient.get()
                .uri("/products?noofRecords=1&currency=usd")
                .retrieve()
                .bodyToFlux(WebClientProduct.class);
    }

    public Flux<WebClientProduct> fallBackMethod(Exception e) {
        WebClientProduct p = WebClientProduct.builder().name("FROM CB").build();
        log.info("FROM fallBack");
        return Flux.just(p);
    }
}
```
   
Vamos directo a las partes que interesan:    
   
- Comencemos con la declaración de HTTPClient:   
    - Su función es la declaración de la configuración básica que tendrá nuestro web client.   
   
```java
private HttpClient httpClient = HttpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500)
            .responseTimeout(Duration.ofMillis(500))
            .compress(true);
```
- Continuamos con la declaración del constructor:   
    - Aquí definimos la instancia de webClient y realizamos las configuraciones pertinentes.   
   
```java
public CustomWebClient(){
       this.webClient = WebClient
                .builder() 
                .baseUrl("https://hub.dummyapis.com/") 
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
```
Mediante el builder de la clase WebClient vamos pasando las diferentes configuraciones que deseamos realizar:   
  - baseUrl: La url base que se utilizará en todas las llamdas a realizar mediante webClient.   
  - clientConnector: Nos permite utilizar las configuraciones creadas mediante HttpClient.   
  - defaultHeader: que datos por defecto tendremos en el header de cada petición.   
- Seguido tenemos el método encargado de utilizar la instancia de webClient para realizar las llamadas HTTP:   
   
```java
public Flux<WebClientProduct> getProducts(){
        return webClient.get()
                .uri("/products?noofRecords=1&currency=usd")
                .retrieve()
                .bodyToFlux(WebClientProduct.class);
    }
```
El proceso es bastante sencillo. Utilizamos nuestra instancia, llamamos el tipo de petición HTTP que queramos realizar. En caso de ser necesario mediante el método `uri` podemos pasar un fragmento extra de `url`. Con retrieve estamos cerrando la llamada http para dar paso a la conversión. En este caso estamos utilizando `bodyToFlux` ya que recibiremos una lista de elementos.   
   
Ya con esto estamos utilizando webClient para llamadas HTTP. No es difícil como se puede observa. Pero, en el código que vimos anteriormente hay más elementos. De esta forma estamos introduciendo a un patrón de microservicios llamado CircuitBreaker; que nos permitirá devolver un valor diferentes en caso de no recibir la respuesta de una llamada a otro servicio en un tiempo definido.   
Antes de continuar debemos mostrar el apartado de configuración de CircuitBreaker y la librería utilizada:   
```XML
<!-- librerias -->
		<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
            <version>3.1.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
            <version>3.0.3</version>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-reactor</artifactId>
            <version>2.0.2</version>
        </dependency>

```
``` YAML
// configuracion
resilience4j:
  circuitbreaker:
    instances:
      testA:
        register-health-indicator: true
        sliding-window-size: 100
        sliding-window-type: COUNT_BASED
        permitted-number-of-calls-in-half-open-state: 5
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        event-consumer-buffer-size: 10
        record-exceptions:
          - org.springframework.web.client.HttpServerErrorException
          - org.springframework.web.reactive.function.client.WebClientRequestException
          - java.io.IOException
          - java.util.concurrent.TimeoutException

```
La configuración va dentro del archivo application.yml y puede ser mucho más flexible con respecto a lo que mostramos en pantalla. Para conocer un poco más de circuit breaker puede ver el siguiente documento [WebFlux Circuit Breaker](webflux-circuit-breaker.md). Ahora seguimos en el código. Sobre la función para realizar la llamada http agregaremos el siguiente decorador:   
```java
@io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "testA", fallbackMethod = "fallBackMethod")
```
Este nos permite mediante decoradores aplicar el patrón sobre un método en especifico. Para su correcto funcionamiento, recibe el nombre de la configuración de la instancia de `circuitBreaker` creada en `application.yml`. De segundo parámetro (opcional) recibe una función de respaldo. A continuación mostramos esta función:   
```java
public Flux<WebClientProduct> fallBackMethod(Exception e) {
        WebClientProduct p = WebClientProduct.builder().name("FROM CB").build();
        log.info("FROM fallBack");
        return Flux.just(p);
    }
```
Las restricciones para escribir esta función son las siguientes:   
- Debe recibir la misma cantidad de parámetros (+ 1 parámetro de error) que la función sobre la que se esté llamando.   
- El tipo de dato de la función de respaldo debe ser el mismo de la función sobre la que lo estamos llamando.   
   
Cumpliendo estas dos restricciones, podemos realizar cualquier operaciones que deseemos dentro de esta función. Y de esta forma ya tenemos aplicado el patrón CircuitBreaker dentro de nuestro proyecto.   
   
