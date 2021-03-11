package org.onap.so.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Sheel Bajpai (sheel.bajpai@orange.com)
 *
 */
@Configuration
@Profile("serviceMesh")
class HttpHeaderForwarderConfig implements WebMvcConfigurer{
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		
		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		if (CollectionUtils.isEmpty(interceptors)) 
			interceptors = new ArrayList<>();
		
		interceptors.add(new HttpHeaderForwarderRequestInterceptor());
		restTemplate.setInterceptors(interceptors);
		return restTemplate;
	}
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HttpHeaderForwarderHandlerInterceptor());
    }
}