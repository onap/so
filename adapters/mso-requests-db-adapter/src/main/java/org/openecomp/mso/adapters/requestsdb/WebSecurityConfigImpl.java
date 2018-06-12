package org.openecomp.mso.adapters.requestsdb;

import org.openecomp.mso.security.MSOSpringFirewall;
import org.openecomp.mso.security.WebSecurityConfig;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.util.StringUtils;

@EnableWebSecurity
public class WebSecurityConfigImpl extends WebSecurityConfig {
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
		.authorizeRequests()
		.antMatchers("/manage/health","/manage/info").permitAll()
		.antMatchers("/**").hasAnyRole(StringUtils.collectionToDelimitedString(getRoles(),",").toString())
		.and()
		.httpBasic();
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		super.configure(web);
		StrictHttpFirewall firewall = new MSOSpringFirewall();
		web.httpFirewall(firewall);
	}

}
