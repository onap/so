/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.onap.so.client.policy.CommonObjectMapperProvider;
import org.onap.so.logging.jaxrs.filter.JaxRsClientLogging;
import org.onap.so.logging.jaxrs.filter.PayloadLoggingFilter;
import org.onap.so.utils.CryptoUtils;
import org.onap.so.utils.TargetEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;


public abstract class RestClient {
	private static final String APPLICATION_MERGE_PATCH_JSON = "application/merge-patch+json";

    public static final String ECOMP_COMPONENT_NAME = "MSO";
	
	private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;
	private WebTarget webTarget;

	protected final Map<String, String> headerMap;
	protected final Logger logger = LoggerFactory.getLogger(RestClient.class);
	protected URL host;
	protected Optional<URI> path;
	protected String accept;
	protected String contentType;
	protected String requestId;
    protected JaxRsClientLogging jaxRsClientLogging;
    protected RestProperties props;

    protected RestClient(RestProperties props, Optional<URI> path) {
		
		headerMap = new HashMap<>();
		try {
			host = props.getEndpoint();
		} catch (MalformedURLException e) {
			
			throw new RuntimeException(e);
		}
		this.props = props;
		this.path = path;
	}

	protected RestClient(RestProperties props, Optional<URI> path, String accept, String contentType) {
		this(props, path);
		this.accept = accept;
		this.contentType = contentType;
		this.props = props;
	}

	protected RestClient(URL host, String contentType) {
		headerMap = new HashMap<>();
		this.path = Optional.empty();
		this.host = host;
		this.contentType = contentType;
		this.props = new DefaultProperties(host);
	}

	/**
	 * Override method to return false to disable logging.
	 * 
	 * @return true - to enable logging, false otherwise
	 */
	protected boolean enableLogging() {
		return true;
	}
	
	/**
	 * Override method to return custom value for max payload size.
	 * 
	 * @return Default value for MAX_PAYLOAD_SIZE = 1024 * 1024
	 */
	protected int getMaxPayloadSize()
	{
		return MAX_PAYLOAD_SIZE;
	}
	
	protected Builder getBuilder() {

	    if (webTarget == null) {
	        initializeClient(getClient());
	    }
	    Builder builder = webTarget.request();
	    initializeHeaderMap(headerMap);
	    for (Entry<String, String> entry : headerMap.entrySet()) {
	        builder.header(entry.getKey(), entry.getValue());
	    }
	    return builder;
	}
	
	protected WebTarget getWebTarget() {
		return this.webTarget;
	}

	protected abstract void initializeHeaderMap(Map<String, String> headerMap);

	protected Optional<ResponseExceptionMapper> addResponseExceptionMapper() {
		return Optional.of(new ResponseExceptionMapperImpl());
	}

	protected CommonObjectMapperProvider getCommonObjectMapperProvider() {
		return new CommonObjectMapperProvider();
	}
	
	/**
	 * Adds a basic authentication header to the request.
	 * @param auth the encrypted credentials
	 * @param key the key for decrypting the credentials
	 */
	protected void addBasicAuthHeader(String auth, String key) {
		try {
			byte[] decryptedAuth = CryptoUtils.decrypt(auth, key).getBytes();
			String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(decryptedAuth);
			headerMap.put("Authorization", authHeaderValue);
		} catch (GeneralSecurityException e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected String getAccept() {
		return accept;
	}

	protected String getContentType() {
		return contentType;
	}

	protected String getMergeContentType() {
		return APPLICATION_MERGE_PATCH_JSON;
	}

	protected Client getClient() {
		return ClientBuilder.newBuilder().build();
	}

	protected abstract TargetEntity getTargetEntity();

	protected void initializeClient(Client client) {
		if (this.enableLogging()) {
			client.register(new PayloadLoggingFilter(this.getMaxPayloadSize()));
		}
		CommonObjectMapperProvider provider = this.getCommonObjectMapperProvider();
		client.register(new JacksonJsonProvider(provider.getMapper()));
		
        jaxRsClientLogging = new JaxRsClientLogging();
        jaxRsClientLogging.setTargetService(getTargetEntity());
        client.register(jaxRsClientLogging);

        if (!path.isPresent()) {
			webTarget = client.target(host.toString());
		} else {
			webTarget = client.target(UriBuilder.fromUri(host + path.get().toString()));
		}
		if (getAccept() == null || getAccept().isEmpty()) {
			this.accept = MediaType.APPLICATION_JSON;
		}
		if (getContentType() == null || getContentType().isEmpty()) {
			this.contentType = MediaType.APPLICATION_JSON;
		}
	}
	
	protected List<Predicate<Throwable>> retryOn() {
		
		List<Predicate<Throwable>> result = new ArrayList<>();
		
		result.add(e -> {
					return e.getCause() instanceof SocketTimeoutException;
				});
		result.add(e -> {
			return e.getCause() instanceof ConnectException;
		});
		return result;
	}

	public Response get() {
		return method("GET", null);
	}

	public Response post(Object obj) {
		return method("POST", obj);
	}

	public Response patch(Object obj) {
		return method("PATCH", obj);
	}

	public Response put(Object obj) {
		return method("PUT", obj);
	}

	public Response delete() {
		return method("DELETE", null);
	}

	public Response delete(Object obj) {
		return method("DELETE", obj);
	}

	public <T> Optional<T> get(Class<T> resultClass) {
		return format(method("GET", null), resultClass);
	}

	public <T> Optional<T> get(GenericType<T> resultClass) {
		return format(method("GET", null), resultClass);
	}

	public <T> T post(Object obj, Class<T> resultClass) {
		return format(method("POST", obj), resultClass).orElse(null);
	}

	public <T> T patch(Object obj, Class<T> resultClass) {
		return format(method("PATCH", obj), resultClass).orElse(null);
	}

	public <T> T put(Object obj, Class<T> resultClass) {
		return format(method("PUT", obj), resultClass).orElse(null);
	}

	public <T> T delete(Class<T> resultClass) {
		return format(method("DELETE", null), resultClass).orElse(null);
	}
	
	public <T> T delete(Object obj, Class<T> resultClass) {
		return format(method("DELETE", obj), resultClass).orElse(null);
	}
	
	private Response method(String method, Object entity) {
		RetryPolicy policy = new RetryPolicy();
		
		List<Predicate<Throwable>> items = retryOn();
		
		Predicate<Throwable> pred = items.stream().reduce(Predicate::or).orElse(x -> false);

		policy.retryOn(error -> pred.test(error));
			
		policy.withDelay(this.props.getDelayBetweenRetries(), TimeUnit.MILLISECONDS)
				.withMaxRetries(this.props.getRetries());
		
		return Failsafe.with(policy).get(buildRequest(method, entity));
	}
	
	protected RestRequest buildRequest(String method, Object entity) {
		return new RestRequest(this, method, entity);
	}
	private <T> Optional<T> format(Response response, Class<T> resultClass) {
		if (this.props.mapNotFoundToEmpty() && response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			return Optional.empty();
		}
		return Optional.of(response.readEntity(resultClass));
	}
	
	private <T> Optional<T> format(Response response, GenericType<T> resultClass) {
		if (this.props.mapNotFoundToEmpty() && response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			return Optional.empty();
		}
		return Optional.of(response.readEntity(resultClass));
	}
}
