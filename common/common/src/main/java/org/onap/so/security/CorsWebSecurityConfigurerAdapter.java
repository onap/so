package org.onap.so.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;

@EnableWebSecurity
@Configuration
@Order(1)
@Profile({ "cors" })
public class CorsWebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CorsWebSecurityConfigurerAdapter.class);

    @Autowired
    @Qualifier("cors")
    private HttpSecurityConfigurer httpSecurityConfigurer;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        LOGGER.debug("Injecting {} configuration ...", httpSecurityConfigurer.getClass());
        httpSecurityConfigurer.configure(http);
        return http.build();
    }

    @Bean
    public HttpFirewall httpFirewall() {
        return new MSOSpringFirewall();
    }
}
