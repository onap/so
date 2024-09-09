package org.onap.so.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
public abstract class BaseWebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWebSecurityConfigurerAdapter.class);

    @Autowired
    protected UserDetailsService userDetailsService;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    abstract HttpSecurityConfigurer getHttpSecurityConfigurer();

    // Configure the HTTP security
    @Bean(name = "httpSecurityBeanOfBaseWebSecurityConfigurerAdapter")
    public SecurityFilterChain httpSecurityFilterChain(HttpSecurity http) throws Exception {
        HttpSecurityConfigurer httpSecurityConfigurer = getHttpSecurityConfigurer();
        LOGGER.debug("Injecting {} configuration ...", httpSecurityConfigurer.getClass());
        return httpSecurityConfigurer.configure(http);
    }

    // Configure web security settings including the HTTP firewall
    public void configureWebSecurity(WebSecurity web) throws Exception {
        final StrictHttpFirewall firewall = new MSOSpringFirewall();
        web.httpFirewall(firewall);
    }

    // Use AuthenticationConfiguration to create the AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


}
