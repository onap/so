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

package org.onap.so.apihandlerinfra;

import org.apache.http.HttpStatus;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.data.controller.InstanceNameDuplicateCheckRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.ClientFactory;
import uk.co.blackpepper.bowman.Configuration;
import uk.co.blackpepper.bowman.RestTemplateConfigurer;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("RequestDbClient")
public class RequestsDbClient {

	private Client<InfraActiveRequests> infraActiveRequestClient;

	@Value("${mso.adapters.db.spring.endpoint:}")
	private String endpoint;
	
	@Value("${mso.db.auth:}")
	private String msoAdaptersAuth;

	private String getOrchestrationFilterURI = "/getOrchestrationFiltersFromInfraActive/";

	private String checkVnfIdStatus = "/infraActiveRequests/checkVnfIdStatus/";

	private String infraActiveRequestURI = "/infraActiveRequests/";

	private String checkInstanceNameDuplicate = "/infraActiveRequests/checkInstanceNameDuplicate";

	private String cloudOrchestrationFiltersFromInfraActive = "/infraActiveRequests/getCloudOrchestrationFiltersFromInfraActive";

	private HttpHeaders headers;

	@Autowired
	private RestTemplate restTemplate;

	@PostConstruct
	public void init() {
		getOrchestrationFilterURI = endpoint + getOrchestrationFilterURI;
		infraActiveRequestURI = endpoint + infraActiveRequestURI;
		checkVnfIdStatus = endpoint + checkVnfIdStatus;
		checkInstanceNameDuplicate = endpoint + checkInstanceNameDuplicate;
		cloudOrchestrationFiltersFromInfraActive = endpoint + cloudOrchestrationFiltersFromInfraActive;
		headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
	}

	public RequestsDbClient() {
		ClientFactory clientFactory = Configuration.builder().setRestTemplateConfigurer(new RestTemplateConfigurer() {

			public void configure(RestTemplate restTemplate) {

				restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {

					public ClientHttpResponse intercept(HttpRequest request, byte[] body,
							ClientHttpRequestExecution execution) throws IOException {

						request.getHeaders().add("Authorization", msoAdaptersAuth);
						return execution.execute(request, body);
					}
				});
			}
		}).build().buildClientFactory();
		infraActiveRequestClient = clientFactory.create(InfraActiveRequests.class);
       
	}
	public List<InfraActiveRequests> getCloudOrchestrationFiltersFromInfraActive(Map<String, String> orchestrationMap){
		URI uri = getUri(cloudOrchestrationFiltersFromInfraActive);
		HttpEntity<Map> entity = new HttpEntity<>(orchestrationMap, headers);
		try{
			return restTemplate.exchange(uri, HttpMethod.POST, entity, new ParameterizedTypeReference<List<InfraActiveRequests>>() {}).getBody();
		}catch(HttpClientErrorException e){
			if(HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()){
				return null;
			}
			throw e;
		}
	}
	public InfraActiveRequests getInfraActiveRequestbyRequestId(String requestId) {
		return this.getSingleInfraActiveRequests(this.getUri(endpoint + "/infraActiveRequests/" + requestId));
	}

	public List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive(Map<String, List<String>> orchestrationMap) {
		URI uri = getUri(getOrchestrationFilterURI);
		HttpEntity<Map<String, List<String>>> entity = new HttpEntity<>(orchestrationMap, headers);
		return restTemplate.exchange(uri, HttpMethod.POST, entity, new ParameterizedTypeReference<List<InfraActiveRequests>>() {}).getBody();
	}

	public InfraActiveRequests checkVnfIdStatus(String operationalEnvironmentId) {
		URI uri = getUri(checkVnfIdStatus + operationalEnvironmentId);
		return restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, InfraActiveRequests.class).getBody();
	}
	public InfraActiveRequests checkInstanceNameDuplicate(HashMap<String, String> instanceIdMap, String instanceName, String requestScope) {
		URI uri = getUri(checkInstanceNameDuplicate);
		HttpEntity<InstanceNameDuplicateCheckRequest> entity = new HttpEntity<>(new InstanceNameDuplicateCheckRequest(instanceIdMap, instanceName, requestScope), headers);
		try{
			return restTemplate.exchange(uri, HttpMethod.POST, entity, InfraActiveRequests.class).getBody();
		}catch(HttpClientErrorException e){
			if(HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()){
				return null;
			}
			throw e;
		}

	}

	public void save(InfraActiveRequests infraActiveRequests) {
		URI uri = getUri(infraActiveRequestURI);
		HttpEntity<InfraActiveRequests> entity = new HttpEntity<>(infraActiveRequests, headers);
		restTemplate.postForLocation(uri, entity);
	}

	protected InfraActiveRequests getSingleInfraActiveRequests(URI uri) {
		return infraActiveRequestClient.get(uri);
	}

	public void updateInfraActiveRequests(InfraActiveRequests request) {		
		infraActiveRequestClient.put(request);
	}

	protected URI getUri(String uri) {
		return URI.create(uri);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate( new HttpComponentsClientHttpRequestFactory());
	}
}
