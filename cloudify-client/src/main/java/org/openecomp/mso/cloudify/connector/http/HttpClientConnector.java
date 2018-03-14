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

package org.openecomp.mso.cloudify.connector.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openecomp.mso.cloudify.base.client.CloudifyClientConnector;
import org.openecomp.mso.cloudify.base.client.CloudifyConnectException;
import org.openecomp.mso.cloudify.base.client.CloudifyRequest;
import org.openecomp.mso.cloudify.base.client.CloudifyResponse;
import org.openecomp.mso.cloudify.base.client.CloudifyResponseException;
import org.openecomp.mso.logger.MsoLogger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class HttpClientConnector implements CloudifyClientConnector {

	public static ObjectMapper DEFAULT_MAPPER;
	public static ObjectMapper WRAPPED_MAPPER;
	
    private static MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

	static {
		DEFAULT_MAPPER = new ObjectMapper();

		DEFAULT_MAPPER.setSerializationInclusion(Include.NON_NULL);
		DEFAULT_MAPPER.disable(SerializationFeature.INDENT_OUTPUT);
		DEFAULT_MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		DEFAULT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		WRAPPED_MAPPER = new ObjectMapper();

		WRAPPED_MAPPER.setSerializationInclusion(Include.NON_NULL);
		WRAPPED_MAPPER.disable(SerializationFeature.INDENT_OUTPUT);
		WRAPPED_MAPPER.enable(SerializationFeature.WRAP_ROOT_VALUE);
		WRAPPED_MAPPER.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
		WRAPPED_MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		WRAPPED_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	protected static <T> ObjectMapper getObjectMapper (Class<T> type) {
		return type.getAnnotation(JsonRootName.class) == null ? DEFAULT_MAPPER : WRAPPED_MAPPER;
	}

	public <T> CloudifyResponse request(CloudifyRequest<T> request) {

		CloseableHttpClient httpClient = null; //HttpClients.createDefault();

		if (request.isBasicAuth()) {
			// Use Basic Auth for this request.
			CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials (request.getUser(), request.getPassword()));

			httpClient = HttpClients.custom().setRedirectStrategy(new HttpClientRedirectStrategy()).setDefaultCredentialsProvider(credentialsProvider).build();
		}
		else {
			// Don't use basic authentication.  The Client will attempt Token-based authentication
			httpClient = HttpClients.custom().setRedirectStrategy(new HttpClientRedirectStrategy()).build();
		}
		
		URI uri = null;
		
		// Build the URI with query params
		try {
			URIBuilder uriBuilder = new URIBuilder(request.endpoint() + request.path());

			for(Map.Entry<String, List<Object> > entry : request.queryParams().entrySet()) {
				for (Object o : entry.getValue()) {
					uriBuilder.setParameter(entry.getKey(), String.valueOf(o));
				}
			}
			
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new HttpClientException (e);
		}

		HttpEntity entity = null;
		if (request.entity() != null) {
			// Special handling for streaming input
			if (request.entity().getEntity() instanceof InputStream) {
				// Entity is an InputStream
				entity = new InputStreamEntity ((InputStream) request.entity().getEntity());
			}
			else {
				// Assume to be JSON.  Flatten the entity to a Json string					
				try {
			    	// Get appropriate mapper, based on existence of a root element in Entity class
					ObjectMapper mapper = getObjectMapper (request.entity().getEntity().getClass());
	
					String entityJson = mapper.writeValueAsString (request.entity().getEntity());
					entity = new StringEntity(entityJson, ContentType.create(request.entity().getContentType()));
					
					LOGGER.debug ("Request JSON Body: " + entityJson.replaceAll("\"password\":\"[^\"]*\"", "\"password\":\"***\""));
	
				} catch (JsonProcessingException e) {
					throw new HttpClientException ("Json processing error on request entity", e);
				} catch (IOException e) {
					throw new HttpClientException ("Json IO error on request entity", e);
				}
			}
		}
		
		// Determine the HttpRequest class based on the method
		HttpUriRequest httpRequest;
		
		switch (request.method()) {
		case POST:
			HttpPost post = new HttpPost(uri);
			post.setEntity (entity);
			httpRequest = post;
			break;
			
		case GET:
			httpRequest = new HttpGet(uri);
			break;

		case PUT:
			HttpPut put = new HttpPut(uri);
			put.setEntity (entity);
			httpRequest = put;
			break;
			
		case DELETE:
			httpRequest = new HttpDelete(uri);
			break;
			
		default:
			throw new HttpClientException ("Unrecognized HTTP Method: " + request.method());
		}
		
		for (Entry<String, List<Object>> h : request.headers().entrySet()) {
			StringBuilder sb = new StringBuilder();
			for (Object v : h.getValue()) {
				sb.append(String.valueOf(v));
			}
			httpRequest.addHeader(h.getKey(), sb.toString());
		}

		// Get the Response.  But don't get the body entity yet, as this response
		// will be wrapped in an HttpClientResponse.  The HttpClientResponse
		// buffers the body in constructor, so can close the response here.
		HttpClientResponse httpClientResponse = null;
		CloseableHttpResponse httpResponse = null;
		
		// Catch known HttpClient exceptions, and wrap them in OpenStack Client Exceptions
		// so calling functions can distinguish.  Only RuntimeExceptions are allowed.
		try {
			httpResponse = httpClient.execute(httpRequest);

			LOGGER.debug ("Response status: " + httpResponse.getStatusLine().getStatusCode());
			
			httpClientResponse = new HttpClientResponse (httpResponse);

			int status = httpResponse.getStatusLine().getStatusCode();
			if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED ||
				status == HttpStatus.SC_NO_CONTENT || status == HttpStatus.SC_ACCEPTED)
			{
				return httpClientResponse;
			}
		}
		catch (HttpResponseException e) {
			// What exactly does this mean?  It does not appear to get thrown for
			// non-2XX responses as documented.
			throw new CloudifyResponseException(e.getMessage(), e.getStatusCode());
		}
		catch (UnknownHostException e) {
			throw new CloudifyConnectException("Unknown Host: " + e.getMessage());
		}
		catch (IOException e) {
			// Catch all other IOExceptions and throw as OpenStackConnectException
			throw new CloudifyConnectException(e.getMessage());
		}
		catch (Exception e) {
			// Catchall for anything else, must throw as a RuntimeException
			e.printStackTrace();
			throw new RuntimeException("Unexpected client exception", e);
		}
		finally {
			// Have the body.  Close the stream
			if (httpResponse != null)
				try {
					httpResponse.close();
				} catch (IOException e) {
					LOGGER.debug("Unable to close HTTP Response: " + e);
				}
		}
		
		// Get here on an error response (4XX-5XX)
		throw new CloudifyResponseException(httpResponse.getStatusLine().getReasonPhrase(),
											httpResponse.getStatusLine().getStatusCode(),
											httpClientResponse);
	}

}
