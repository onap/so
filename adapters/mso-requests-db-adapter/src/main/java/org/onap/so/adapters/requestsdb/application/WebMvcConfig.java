package org.onap.so.adapters.requestsdb.application;


import org.onap.so.logging.spring.interceptor.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Autowired
    LoggingInterceptor loggingInterceptor;

    @Bean
    public MappedInterceptor mappedLoggingInterceptor() {
        return new MappedInterceptor(new String[]{"/**"}, loggingInterceptor);
    }

}