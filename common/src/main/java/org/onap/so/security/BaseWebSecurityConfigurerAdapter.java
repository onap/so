package org.onap.so.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;


public abstract class BaseWebSecurityConfigurerAdapter /* implements WebSecurityConfigurer */ {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWebSecurityConfigurerAdapter.class);

    @Autowired
    protected UserDetailsService userDetailsService;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    abstract HttpSecurityConfigurer getHttpSecurityConfigurer();


    public void configure(final WebSecurity web) throws Exception {
        this.configure(web);
        final StrictHttpFirewall firewall = new MSOSpringFirewall();
        web.httpFirewall(firewall);
    }

    @Bean(name = "httpSecurityBeanOfBaseWebSecurityConfigurerAdapter")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        HttpSecurityConfigurer httpSecurityConfigurer = getHttpSecurityConfigurer();
        LOGGER.debug("Injecting {} configuration ...", httpSecurityConfigurer.getClass());

        return httpSecurityConfigurer.configure(http);
    }

    @Bean(name = "webSecurityBeanOfBaseWebSecurityConfigurerAdapter")
    public WebSecurity filterChain(WebSecurity web) throws Exception {
        this.configure(web);
        final StrictHttpFirewall firewall = new MSOSpringFirewall();
        return web.httpFirewall(firewall);

    }

    @Bean(name = "authenticationManagerBuilderBeanOfBaseWebSecurityConfigurerAdapter")
    public SecurityFilterChain filterChain(final AuthenticationManagerBuilder auth) throws Exception {
        return (SecurityFilterChain) auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }


}
