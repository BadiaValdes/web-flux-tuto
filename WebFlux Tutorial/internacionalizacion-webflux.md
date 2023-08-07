 # Internacionalización WebFlux   
   
Antes de comenzar con la internacionalización con WebFlux hay que es similar a la vista para Serverlet en Spring Boot pero posee algunas diferentes. Si quieres ver la forma de hacerlo en Serverlet te recomendamos la siguiente página:   
- [https://lokalise.com/blog/spring-boot-internationalization/](https://lokalise.com/blog/spring-boot-internationalization/)    
   
   
Ahora si, comencemos con la configuración de la internacionalización. Aquí debemos hacer una configuración un poco larga, debido a que a la hora de extender del paquete de configuración de WebFlux, se pierden las referencias a thymeleaf. Pero vamos por parte; iniciemos con la forma a utilizar para capturar el lenguaje a utilizar:   
```java
public class QueryParamLocaleContextResolver implements LocaleContextResolver{
    @Override
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        Locale target = Locale.getDefault(); 
        List<String> langs = exchange.getRequest().getQueryParams().get("lang"); 
        if(langs != null && !langs.isEmpty()){ 
            target = Locale.forLanguageTag(langs.get(0)); 
        }
        return new SimpleLocaleContext(target); 
    }

    @Override
    public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {
        throw new UnsupportedOperationException("Not Supported");
    }
}
```
La primera clase que veremos se llama `QueryParamLocaleContextResolver` y su función principal es capturar de la URL el lenguaje seleccionado mediante un `queryParam`. Para poder realizar esta operación, debemos implementar la interfaz `LocaleContextResolver`, que nos da acceso a los métodos necesarios para crear el contexto de nuestra aplicación.   
Vamos a hablar del primer método (`resolveLocaleContext`), ya que el segundo se utilizaría para cambiar el contexto manual en la aplicación. El primero recibe como parámetros una variable de tipo `ServerWebExchange`, dedicada a poseer los datos de las peticiones que se realizan a los controles; gracias a esto, tenemos acceso a los datos de las URL.   
Dentro, creamos la variable encargada de poseer el valor de la "localidad" o mejor dicho, la zona donde está el cliente de la aplicación (US, ES, etc.). Por defecto le asignamos el valor del servidor (`target = Locale.getDefault()`), por si no se envía ningún query por la URL. Posteriormente capturamos el `queryParam` mediante la siguiente línea de código:   
```java
exchange.getRequest().getQueryParams().get("lang");
```
 El valor que proviene de esa línea debemos guardarlo en una lista de `String`; debido a que o devuelve `null` o una lista vacía. Seguido comprobamos si dicha lista no es vacía y no es null; en caso de que cumpla las condiciones, podremos acceder a su valor y utilizarlo para declarar una nueva "`zona`":   
```java
target = Locale.forLanguageTag(langs.get(0));
```
En cualquier caso, la idea es devolver un contexto por lo que no podemos realizar un `return` directo de la variable target. Para ellos nos auxiliamos de una clase que permite crear contextos de forma sencilla:   
```java
new SimpleLocaleContext(target)
```
Con esto solo tenemos el primer paso de la declaración del contexto. Ahora nos queda decirle a la aplicación como debe manejarlo. Para ello hacemos uso de una clase de configuración llamada `LocaleSupportConfig`:   
```java
@Configuration
public class LocaleSupportConfig extends DelegatingWebFluxConfiguration {
    @Override
    protected LocaleContextResolver createLocaleContextResolver() {
        return new QueryParamLocaleContextResolver();
    }
}

```
La única responsabilidad de la misma es la creación de una instancia de la clase `QueryParamLocaleContextResolver` para modificar el contexto de la aplicación. Antes de continuar, vamos a ver varios temas interesantes en este apartdo:   
- `@Configuration` → En Spring Boot, las clases que tengan este decorador son consideradas parte de la configuración del sistema, por lo tanto, cuando el servidor comience a cargar las configuraciones, debe utilizar los métodos que se declaren en estas clases.    
- `DelegatingWebFluxConfiguration` → Al extender de esta clase, estamos accediendo directamente a los métodos de configuración de WebFlux y a su vez, es la que nos obliga a crear configuraciones adicionales para utilizar Thymeleaf como motor de plantillas. El uso de esta clase es obligatorio para poder acceder al contexto de la aplicación.   
   
La siguiente clase que veremos es la principal de configuración. Aquí se pondrán todas las configuraciones que se realizarán dentro de nuestra aplicación. Comencemos viendo el código:   
```java
@Configuration
@EnableWebFlux
public class WebConfig implements ApplicationContextAware, WebFluxConfigurer  {
    private ApplicationContext ctx;
    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.ctx = context;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**") 
                .addResourceLocations("/public", "classpath:/static/") 
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
    }


    @Bean
    public SpringResourceTemplateResolver thymeleafTemplateResolver() {
        final SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver(); 
        resolver.setApplicationContext(this.ctx); 
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html"); 
        resolver.setTemplateMode(TemplateMode.HTML); 
        resolver.setCacheable(false);
        resolver.setCheckExistence(false);
        return resolver;
    }

    @Bean 
    public ISpringWebFluxTemplateEngine thymeleafTemplateEngine() {
        final SpringWebFluxTemplateEngine templateEngine = new SpringWebFluxTemplateEngine(); 
        templateEngine.setTemplateResolver(thymeleafTemplateResolver()); 
        return templateEngine;
    }

    @Bean
    public ThymeleafReactiveViewResolver thymeleafChunkedAndDataDrivenViewResolver() {
        final ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver(); 
        viewResolver.setTemplateEngine(thymeleafTemplateEngine()); 
        viewResolver.setResponseMaxChunkSizeBytes(8192); 
        return viewResolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(thymeleafChunkedAndDataDrivenViewResolver());
    }


    @Bean
    public MessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource(); 

        messageSource.setBasenames("classpath:lang/messages"); 
        messageSource.setUseCodeAsDefaultMessage(true); 
        messageSource.setDefaultEncoding("UTF-8"); 
        messageSource.setCacheSeconds(5); 
        return messageSource;
    }
}
```
Como pueden observar es bastante grande, pero no hay de que preocuparse, es sencilla de entender. Iniciemos con la declaración de la clase:   
```java
@Configuration
@EnableWebFlux
public class WebConfig implements ApplicationContextAware, WebFluxConfigurer

```
- `@Configuration` → Anteriormente mencionamos el uso de este decorador, pero por si te lo saltaste aquí va de nuevo. Define una clase creada como parte de la configuración de Spring Boot.   
- `@EnableWebFlux` → De forma automática importa las configuraciones presentes en la clase `[WebFluxConfigurationSupport](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/reactive/config/WebFluxConfigurationSupport.html)`.    
- `ApplicationContextAware` → Esto es muy importante ya que permite el acceso al contexto de la aplicación.   
- `WebFluxConfigurer` → Permite la implementación de métodos de configuración de WebFlux.   
   
Las primeras líneas de código posteriores a la declaración de la clase son las más sencillas de entender ya que se encargan de declarar la propiedad de contexto y asignarla:   
```java
private ApplicationContext ctx;
@Override
public void setApplicationContext(ApplicationContext context) {
   this.ctx = context;
}
```
Seguimos y vemos dos métodos sumamente importantes para el trabajo con el Template:   
```java
 	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**") 
                .addResourceLocations("/public", "classpath:/static/") 
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
    }

    @Bean
    public SpringResourceTemplateResolver thymeleafTemplateResolver() {
        final SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver(); 
        resolver.setApplicationContext(this.ctx); 
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html"); 
        resolver.setTemplateMode(TemplateMode.HTML); 
        resolver.setCacheable(false);
        resolver.setCheckExistence(false);
        return resolver;
    }
```
- `addResourceHandlers` → Como se puede ver es un método que sobrescribe el comportamiento del `ResourceHandler` de webFlux. Su función principal es configurar la ubicación de los archivos `CSS` y `JS` de nuestra aplicación, así como cualquier archivo de tipo imagen.    
- `thymeleafTemplateResolver` → En este caso estamos en presencia de una función decorada con `@Bean`. No trabaja directamente con la configuración, ya que el objetivo de la misma es solo la declaración de la ubicación de los archivos de plantillas y la configuración para el uso de los mismos. Pero es solo el primer paso de la configuración de thymeleaf.   
   
Los métodos que si se encargan de la configuración del motor de plantillas son los siguientes. De igual forma, los dos primeros son configuraciones generales y el último es el que se encarga de decirle a WebFlux que debe usar un motor de plantillas en específico.   
```java
	@Bean 
    public ISpringWebFluxTemplateEngine thymeleafTemplateEngine() {
        final SpringWebFluxTemplateEngine templateEngine = new SpringWebFluxTemplateEngine(); 
        templateEngine.setTemplateResolver(thymeleafTemplateResolver()); 
        return templateEngine;
    }

    @Bean
    public ThymeleafReactiveViewResolver thymeleafChunkedAndDataDrivenViewResolver() {
        final ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver(); 
        viewResolver.setTemplateEngine(thymeleafTemplateEngine()); 
        viewResolver.setResponseMaxChunkSizeBytes(8192); 
        return viewResolver;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(thymeleafChunkedAndDataDrivenViewResolver());
    }
```
- `thymeleafTemplateEngine` → Encargado de asignar la configuración de las plantillas al motor de plantillas de la aplicación.   
- `thymeleafChunkedAndDataDrivenViewResolver` → Encargado de crear el motor de plantillas junto con la configuración de las plantillas. En este caso, thymeleaf.    
- `configureViewResolvers` → Terminamos con el método que registra thymeleaf como motor de plantillas y le dice a webFlux que debe utilizarlo.   
   
Hasta aquí solo hemos configurado el motor de plantillas de la aplicación pero no hemos hablado de la internacionalización. Aunque lo anterior es importante tenerlo en cuenta para el correcto funcionamiento de la aplicación, ahora es que terminaremos la configuración del cambio de idioma. Primero partimos que dentro de la carpeta resources, debemos tener una serie de archivos con extensión properties llamados messages. Para diferenciar uno del otro, debemos agregar al nombre "\_us" por ejemplo; pero siempre debemos tener uno que se llame `messages.properties` ya que será el cargado por defecto. Veamos como está dentro de nuestra aplicación:   
![image.png](files\image.png)    
Ahora pasemos al código:   
```java
@Bean
    public MessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource(); 
        messageSource.setBasenames("classpath:lang/messages"); 
        messageSource.setUseCodeAsDefaultMessage(true); 
        messageSource.setDefaultEncoding("UTF-8"); 
        messageSource.setCacheSeconds(5); 
        return messageSource;
    }
```
La función `messageSource` se encargará de definir de donde obtendremos los messages para mostrar según el idioma seleccionado. Dentro estaremos definiendo la dirección donde se encontrarán nuestros mensajes, así como el nombre común de los mismos: `messageSource.setBasenames("classpath:lang/messages")`. Además de otros parámetros del mensaje, siendo el más importante el encode; que nos permitirá definir la forma de decodificar los caracteres y así, poder tener soporte para letras como la ñ: `messageSource.setDefaultEncoding("UTF-8")`   
Ahora si, después de todo esto solo resta reiniciar la aplicación y probar si los cambios que hicimos surtieron efecto. Pero…, cómo puedo manejar la internacionalización en thymeleaf?   
Es bastante sencillo, simplemente utilizamos el `th:text` de thymeleaf y agregarle la variable de mensaje. Pero en vez de utilizar el `$` para llamar a la variable usamos el `#`. Y el nombre que utilicemos debe ser el mismo del mensaje que queramos mostrar. A su vez, esa variable debe repetirse en los archivos `messages.properties`.   
```html
<span data-th-text="#{product}"> </span>
```
   
