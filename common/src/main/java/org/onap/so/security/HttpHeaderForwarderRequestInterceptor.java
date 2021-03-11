package org.onap.so.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * @author Sheel Bajpai (sheel.bajpai@orange.com)
 *
 */

public class HttpHeaderForwarderRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        
    	Map<String, List<String>> headerMap = HttpHeaderForwarderHandlerInterceptor.getHeaders();
    	if(headerMap!=null && !headerMap.isEmpty())
    		request.getHeaders().putAll(HttpHeaderForwarderHandlerInterceptor.getHeaders());
        return execution.execute(request, body);
    }

}
