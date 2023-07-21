package com.udemy.webFlux.mvc.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration;
import org.springframework.web.server.i18n.LocaleContextResolver;

/**
 * Esta clase de configuración nos permite definir el contexto de localidad a utilizar en el sistema
 * Para poder realizar esta operación debemos extender de la clase DelegatingWebFluxConfiguration y sobre escribir el método
 * createLocaleContextResolver.
 *
 * @author Emilio
 */
@Configuration
public class LocaleSupportConfig extends DelegatingWebFluxConfiguration {

    /**
     * Este méotod nos permite definir el contexto a utilizar dentro de nuestra aplicación. O mejor dicho,
     * de donde vamos a obtener el contexto.
     *
     * @return LocaleContext
     */
    @Override
    protected LocaleContextResolver createLocaleContextResolver() {
        return new QueryParamLocaleContextResolver();
    }
}
