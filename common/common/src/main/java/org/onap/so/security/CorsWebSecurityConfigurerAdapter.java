package org.onap.so.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@Configuration
@Order(1)
@Profile({"cors"})
public class CorsWebSecurityConfigurerAdapter extends BaseWebSecurityConfigurerAdapter {
    @Autowired
    @Qualifier("cors")
    protected HttpSecurityConfigurer httpSecurityConfigurer;

    @Override
    HttpSecurityConfigurer getHttpSecurityConfigurer() {
        return httpSecurityConfigurer;
    }
}
