package org.onap.so.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.context.annotation.Configuration;
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

    public void configureWebSecurity(final WebSecurity web) throws Exception {
        final StrictHttpFirewall firewall = new MSOSpringFirewall();
        web.httpFirewall(firewall);
    }

    @Bean(name = "httpSecurityBeanOfBaseWebSecurityConfigurerAdapter")
    public SecurityFilterChain httpSecurityFilterChain(HttpSecurity http) throws Exception {
        LOGGER.debug("HttpSecurity configuration ...");
        HttpSecurityConfigurer httpSecurityConfigurer = getHttpSecurityConfigurer();
        LOGGER.debug("Injecting {} configuration ...", httpSecurityConfigurer.getClass());
        return httpSecurityConfigurer.configure(http);
    }


    @Bean(name = "webSecurityBeanOfBaseWebSecurityConfigurerAdapter")
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            try {
                configureWebSecurity(web);
            } catch (Exception e) {
                LOGGER.error("Error configuring web security", e);
                throw new RuntimeException(e);
            }
        };
    }

    // Configure the configureAuthenticationManager
    @Autowired
    public void configureAuthenticationManager(AuthenticationManagerBuilder auth) throws Exception {
        LOGGER.debug("Injecting UserDetailsService and PasswordEncoder ...");
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        LOGGER.debug("Out of UserDetailsService and PasswordEncoder ...");
    }

    // Configure the AuthenticationManagerBuilder
    @Bean(name = "authenticationManagerBeanOfBaseWebSecurityConfigurerAdapter")
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        LOGGER.debug("Injecting AuthenticationManager ...");
        return authenticationConfiguration.getAuthenticationManager();
    }


}
