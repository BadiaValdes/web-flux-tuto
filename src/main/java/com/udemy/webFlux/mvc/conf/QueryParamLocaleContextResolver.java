package com.udemy.webFlux.mvc.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;
import java.util.List;
import java.util.Locale;

public class QueryParamLocaleContextResolver implements LocaleContextResolver{

    private static final Logger log = LoggerFactory.getLogger(QueryParamLocaleContextResolver.class);

    @Override
    public LocaleContext resolveLocaleContext(ServerWebExchange exchange) {
        Locale target = Locale.getDefault();
        List<String> langs = exchange.getRequest().getQueryParams().get("lang");
        if(langs != null && !langs.isEmpty()){
            target = Locale.forLanguageTag(langs.get(0));
        }
        log.info("target " + target.getLanguage());
        return new SimpleLocaleContext(target);
    }

    @Override
    public void setLocaleContext(ServerWebExchange exchange, LocaleContext localeContext) {
        throw new UnsupportedOperationException("Not Supported");
    }
}
