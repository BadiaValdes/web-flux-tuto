package com.udemy.webFlux.mvc.conf;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;
import java.util.List;
import java.util.Locale;

/**
 * Esta clase me permitirá definir la forma en que obtenedremos el lenguaje a utilizar del sitio web.
 * Normalmente lo podríamos obtener del header de la petición, pero en este caso lo hacemos a partir de un
 * query pram.
 *
 * @author Emilio
 */
public class QueryParamLocaleContextResolver implements LocaleContextResolver{


    /**
     * Este método es el que se encarga de obtener los datos de las peticiones realizadas al servidor
     * @param exchange Posee la información de todas las trnasacciones que se realicen mediante el servidor.
     * @return LocalContext
     */
    @Override
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        Locale target = Locale.getDefault(); // Primero defininoms la localidad por defecto (la del servidor)
        List<String> langs = exchange.getRequest().getQueryParams().get("lang"); // Creamos una lista para obtener todos los parámetors que se pasen en el query param. En este caso estamos buscando el query param Lang
        if(langs != null && !langs.isEmpty()){ // Si no es nulo o vacio, vamos a asignarlo a nuestra variable objetivo
            target = Locale.forLanguageTag(langs.get(0)); // Este es el paso de asignación, como recibimos un String, debemos convertirlo a Locale.
        }
        return new SimpleLocaleContext(target); // Para devolver el valor, tenemos que crear un SimplexLocaleContext
    }

    /**
     * Seria parecido a lo anterior, pero para cambiarlo directamente en el código
     * @param exchange
     * @param localeContext
     */
    @Override
    public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {
        throw new UnsupportedOperationException("Not Supported");
    }
}
