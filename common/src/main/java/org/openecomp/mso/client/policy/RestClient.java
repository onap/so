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

package org.openecomp.mso.client.policy;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.openecomp.mso.client.RestProperties;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.utils.CryptoUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public abstract class RestClient {
	protected static final String ECOMP_COMPONENT_NAME = "MSO";
	
	private static final int MAX_PAYLOAD_SIZE = 1024 * 1024;
	private WebTarget webTarget;

	protected final Map<String, String> headerMap;
	protected final MsoLogger msoLogger;
	protected URL host;
	protected Optional<URI> path;
	protected Logger logger;
	protected String accept;
	protected String contentType;
	protected UUID requestId;

	protected RestClient(RestProperties props, UUID requestId, Optional<URI> path) {
		logger = Logger.getLogger(getClass().getName());
		msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);
		this.requestId = requestId;
		headerMap = new HashMap<>();
		try {
			host = props.getEndpoint();
		} catch (MalformedURLException e) {
			logger.error("url not valid", e);
			throw new RuntimeException(e);
		}
		
		this.path = path;
		initializeClient(getClient());
	}

	protected RestClient(RestProperties props, UUID requestId, Optional<URI> path, String accept, String contentType) {
		this(props, requestId, path);
		this.accept = accept;
		this.contentType = contentType;
	}

	protected RestClient(URL host, UUID requestId, String contentType) {
		headerMap = new HashMap<>();
		logger = Logger.getLogger(getClass().getName());
		msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);
		this.path = Optional.empty();
		this.host = host;
		this.contentType = contentType;
		this.requestId = requestId;
		initializeClient(getClient());
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

		Builder builder = webTarget.request();
		initializeHeaderMap(headerMap);

		for (Entry<String, String> entry : headerMap.entrySet()) {
			builder.header(entry.getKey(), entry.getValue());
		}
		return builder;
	}

	protected abstract void initializeHeaderMap(Map<String, String> headerMap);

	protected abstract Optional<ClientResponseFilter> addResponseFilter();

	public abstract RestClient addRequestId(UUID requestId);
	
	/**
	 * Adds a basic authentication header to the request.
	 * @param auth the encrypted credentials
	 * @param key the key for decrypting the credentials
	 */
	protected void addBasicAuthHeader(String auth, String key) {
		try {
			byte[] decryptedAuth = CryptoUtils.decrypt(auth, key).getBytes();
			String authHeaderValue = "Basic " + new String(Base64.encodeBase64(decryptedAuth));
			headerMap.put("Authorization", authHeaderValue);
		} catch (GeneralSecurityException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	protected ContextResolver<ObjectMapper> getMapper() {
		return new CommonObjectMapperProvider();
	}

	protected String getAccept() {
		return accept;
	}

	protected String getContentType() {
		return contentType;
	}

	protected String getMergeContentType() {
		return "application/merge-patch+json";
	}

	protected Client getClient() {
		return ClientBuilder.newBuilder().build();
	}

	protected UUID getRequestId() {
		return this.requestId;
	}
	protected void initializeClient(Client client) {
		if (this.enableLogging()) {
			client.register(logger).register(new LoggingFilter(this.getMaxPayloadSize()));
		}
		client.register(this.getMapper());
		Optional<ClientResponseFilter> responseFilter = this.addResponseFilter();
		responseFilter.ifPresent(clientResponseFilter -> client.register(clientResponseFilter));
		webTarget = path.<WebTarget>map(uri -> client.target(UriBuilder.fromUri(host + uri.toString())))
			.orElseGet(() -> client.target(host.toString()));
		this.accept = MediaType.APPLICATION_JSON;
		this.contentType = MediaType.APPLICATION_JSON;
	}

	public Response get() {
		return this.getBuilder().accept(this.getAccept()).get();
	}

	public Response post(Object obj) {
		return this.getBuilder().accept(this.getAccept()).post(Entity.entity(obj, this.getContentType()));
	}

	public Response patch(Object obj) {
		return this.getBuilder().header("X-HTTP-Method-Override", "PATCH").accept(this.getAccept())
				.post(Entity.entity(obj, this.getMergeContentType()));
	}

	public Response put(Object obj) {
		return this.getBuilder().accept(this.getAccept()).put(Entity.entity(obj, this.getContentType()));
	}

	public Response delete() {
		return this.getBuilder().accept(this.getAccept()).delete();
	}

	public Response delete(Object obj) {
		return this.getBuilder().header("X-HTTP-Method-Override", "DELETE").accept(this.getAccept())
				.put(Entity.entity(obj, this.getContentType()));
	}

	public <T> T get(Class<T> resultClass) {
		return this.get().readEntity(resultClass);
	}

	public <T> T get(GenericType<T> resultClass) {
		return this.get().readEntity(resultClass);
	}

	public <T> T post(Object obj, Class<T> resultClass) {
		return this.post(obj).readEntity(resultClass);
	}

	public <T> T patch(Object obj, Class<T> resultClass) {
		return this.patch(obj).readEntity(resultClass);
	}

	public <T> T put(Object obj, Class<T> resultClass) {
		return this.put(obj).readEntity(resultClass);
	}

	public <T> T delete(Class<T> resultClass) {
		return this.delete().readEntity(resultClass);
	}
	
	public <T> T delete(Object obj, Class<T> resultClass) {
		return this.delete(obj).readEntity(resultClass);
	}
}
