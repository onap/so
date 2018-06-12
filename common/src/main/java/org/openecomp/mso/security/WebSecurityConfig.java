package org.openecomp.mso.security;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@ConfigurationProperties(prefix = "spring.security")
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private List<UserCredentials> credentials;
	private List<String> roles = new ArrayList<>();

	public List<String> getRoles() {
		return roles;
	}

	@PostConstruct
	private void addRoles() {
		for(int i=0; i <credentials.size(); i++) {
			roles.add(credentials.get(i).getRole());
		}
	}
	
	public List<UserCredentials> getUsercredentials() {
		return credentials;
	}

	public void setUsercredentials(List<UserCredentials> usercredentials) {
		this.credentials = usercredentials;
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
	}

}
