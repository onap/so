package org.onap.so.adapters.sdnc;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.List;
import org.onap.so.adapters.sdncrest.SdncRestCallback;
import org.onap.so.adapters.sdncrest.SdncRestRequest;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SdncHttpJsonConfiguration implements WebMvcConfigurer {
    private final ObjectMapper mapper;

    public SdncHttpJsonConfiguration(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Bean
    public ServletRegistrationBean<DispatcherServlet> sdncHttpServletMapping(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean<DispatcherServlet> registration =
                new ServletRegistrationBean<>(dispatcherServlet, "/adapters/rest/v2/sdnc/requests");
        registration.setName("sdncHttpDispatcher");
        return registration;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper isolated = mapper.copy().disable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .disable(SerializationFeature.WRAP_ROOT_VALUE);
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter json = (MappingJackson2HttpMessageConverter) converter;
                json.registerObjectMappersForType(SdncRestRequest.class,
                        mappings -> mappings.put(MediaType.APPLICATION_JSON, isolated));
                json.registerObjectMappersForType(SdncRestCallback.class,
                        mappings -> mappings.put(MediaType.APPLICATION_JSON, isolated));
            }
        }
    }
}
