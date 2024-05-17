package org.onap.so.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Component("cors")
@Profile({"cors"})
public class CorsBasicHttpSecurityConfigurer implements HttpSecurityConfigurer {

    @Override
    public SecurityFilterChain configure(final HttpSecurity http) throws Exception {
        // http.cors().and().csrf().disable().authorizeRequests().antMatchers("/manage/health",
        // "/manage/info").permitAll()
        // .antMatchers("/**").fullyAuthenticated().and().httpBasic();
        http.cors(cors -> cors.disable()).csrf(csrf -> {
            try {
                csrf.disable().authorizeRequests().requestMatchers("/manage/health", "/manage/info").permitAll()
                        .requestMatchers("/**").fullyAuthenticated().and().httpBasic(httpBasic -> httpBasic.disable());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return null;

    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("OPTIONS", "GET", "POST", "PATCH"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
