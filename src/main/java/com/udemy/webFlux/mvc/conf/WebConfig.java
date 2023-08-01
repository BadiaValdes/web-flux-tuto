package com.udemy.webFlux.mvc.conf;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.CacheControl;
import org.springframework.web.reactive.config.*;
import org.thymeleaf.spring6.ISpringWebFluxTemplateEngine;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Este es una clase de configuración. Dentro pondremos todos los métodos que nos permitirán configurar nuesta aplicación de webFlux.
 *
 * Esta clase implementa las interfaces ApplicationContextAware y WebFluxConfigurer. Siendo la primera encargada de capturar el cotnexto de la aplicación y la segunda
 * de poseer los métodos de configuración de spring boot.
 *
 * @author Emilio
 */
@Configuration
@EnableWebFlux // Este decorador nos permite utilizar esta clase como parte de la configuración de webFlux.
public class WebConfig implements ApplicationContextAware, WebFluxConfigurer  {

    private ApplicationContext ctx;

    /**
     * Este método obtendrá el contexto de la aplicación y nos permitrá utilizarlo dentro de esta clase.
     * @param context
     */
    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.ctx = context;
    }

    /**
     * Un problema que trae la sobre-escritura de la configración de webFlux es la pérdida de la referencia al template y los statics.
     * Este método se encarga de definir la dirección donde encontraremos los archivos css y js.
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**") // Mediante el registry definimos como manejaremos los static (patrón de acceso, en este caso es cualquier carpeta interna)
                .addResourceLocations("/public", "classpath:/static/") // Aquí definimos la carpeta global donde estarán guardados los archivos statics
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS));
    }

    /**
     * A diferencia de la anterior, este se encarga de definir la ruta del template y que extensión tendrán los mismos.
     * @return
     */
    @Bean
    public SpringResourceTemplateResolver thymeleafTemplateResolver() {
        final SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver(); // Comenzamos creando un resolver para los templates
        resolver.setApplicationContext(this.ctx); // Le pasamos el contexto de la aplicación
        resolver.setPrefix("classpath:/templates/"); // Definimos la carpeta donde se encontrarán las vistas
        resolver.setSuffix(".html"); // Esto hace referencia a la extensión
        resolver.setTemplateMode(TemplateMode.HTML); // El decodificador a utilizar
        resolver.setCacheable(false);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCheckExistence(false);
        return resolver;
    }

    /**
     * Definiremos mediante este método el motor de plantillas a utilizar
     * @return
     */
    @Bean // Como es una configuración que debe ser administrada completamente por el sistema, debemos ponerle este decorador.
    public ISpringWebFluxTemplateEngine thymeleafTemplateEngine() {
        final SpringWebFluxTemplateEngine templateEngine = new SpringWebFluxTemplateEngine(); // Creamos una nueva instancia del Template Engine de WebFlux
        templateEngine.setTemplateResolver(thymeleafTemplateResolver()); // Le asignamos un resolver, en este caso, el creado en el método thymeleafTemplateResolver
        return templateEngine;
    }

    /**
     * Después de declarar el motor de plantillas a utilizar, debemos hacer que este forme parte de nuestra aplicación.
     * Este método se encarga de realizar ese proceso.
     *
     * @return
     */
    @Bean
    public ThymeleafReactiveViewResolver thymeleafChunkedAndDataDrivenViewResolver() {
        final ThymeleafReactiveViewResolver viewResolver = new ThymeleafReactiveViewResolver(); // Como vamos a utilizar thymeleaf, debemos crear un resolver propio.
        viewResolver.setTemplateEngine(thymeleafTemplateEngine()); // Le pasamos el engine creado en el método thymeleafTemplateEngine
        viewResolver.setResponseMaxChunkSizeBytes(8192); // OUTPUT BUFFER size limit
        return viewResolver;
    }

    /**
     * Para terminar, sobrescribimos el viewResolver para que utilice thymeleaf como motor de plantillas.
     * @param registry
     */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.viewResolver(thymeleafChunkedAndDataDrivenViewResolver());
    }

    /**
     * Si queremos utilizar el i18n en nuestro sistema debemos definir un manejador de mansajes.
     * @return MessageSource
     */
    @Bean
    public MessageSource messageSource() {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource(); // Definimos el objeto para almacenar la configuración de los mensajes
        messageSource.setBasenames("classpath:lang/messages"); // Añadimos la ubicación de los messages.properties. Todos estos archivos deben comenzar con message en el nombre
        messageSource.setUseCodeAsDefaultMessage(true); // Lo define como manejador de mensajes por defecto
        messageSource.setDefaultEncoding("UTF-8"); // El codificador por defecto debe ser UTF-8 para permitir tildes y ñ
        messageSource.setCacheSeconds(5); // Definimos cuanto tiempo los mensajes se mantendrán en cache
        return messageSource;
    }

    /***
     * Circuit breaker configuration
     *
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerConfigCustomizer(){
        CircuitBreakerConfig externalServiceFooConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .waitDurationInOpenState(Duration.ofSeconds(1))
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50.0f)
                .build();
        return CircuitBreakerRegistry.of(
                Map.of("test", externalServiceFooConfig)
        );
    }


}
