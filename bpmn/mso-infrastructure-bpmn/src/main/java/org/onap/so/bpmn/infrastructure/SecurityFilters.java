package org.onap.so.bpmn.infrastructure;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

@Configuration
@Profile("!test")
public class SecurityFilters {

    @Bean
    public FilterRegistrationBean<SoCadiFilter> loginRegistrationBean() {
        FilterRegistrationBean<SoCadiFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new SoCadiFilter());
        filterRegistrationBean.setName("cadiFilter");
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
