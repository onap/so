package org.onap.so.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

public abstract class BaseWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseWebSecurityConfigurerAdapter.class);

    @Autowired
    protected UserDetailsService userDetailsService;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    abstract HttpSecurityConfigurer getHttpSecurityConfigurer();

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        HttpSecurityConfigurer httpSecurityConfigurer = getHttpSecurityConfigurer();
        LOGGER.debug("Injecting {} configuration ...", httpSecurityConfigurer.getClass());

        httpSecurityConfigurer.configure(http);
    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        super.configure(web);
        final StrictHttpFirewall firewall = new MSOSpringFirewall();
        web.httpFirewall(firewall);
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }
}
