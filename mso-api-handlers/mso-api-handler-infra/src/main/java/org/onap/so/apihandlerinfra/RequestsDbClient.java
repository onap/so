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
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.data.controller.InstanceNameDuplicateCheckRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.ClientFactory;
import uk.co.blackpepper.bowman.Configuration;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("RequestDbClient")
public class RequestsDbClient {

	private static final String SERVICE_ID = "SERVICE_ID";
	private static final String OPERATION_ID = "OPERATION_ID";
	
	private Client<InfraActiveRequests> infraActiveRequestClient;
	private Client<OperationStatus> operationStatusClient;

	@Value("${mso.adapters.requestDb.endpoint}")
	private String endpoint;

	@Value("${mso.adapters.requestDb.auth}")
	private String msoAdaptersAuth;

	private String getOrchestrationFilterURI = "/infraActiveRequests/getOrchestrationFiltersFromInfraActive/";
	private static final String OPERATION_STATUS_REPOSITORY_SEARCH = "/operationStatusRepository/search";

	private String checkVnfIdStatus = "/infraActiveRequests/checkVnfIdStatus/";

	private String infraActiveRequestURI = "/infraActiveRequests/";

	private String checkInstanceNameDuplicate = "/infraActiveRequests/checkInstanceNameDuplicate";
	
	private String findOneByServiceIdAndOperationIdURI = "/findOneByServiceIdAndOperationId";

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
		findOneByServiceIdAndOperationIdURI = endpoint + OPERATION_STATUS_REPOSITORY_SEARCH + findOneByServiceIdAndOperationIdURI;
		headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
	}

	public RequestsDbClient() {
		ClientFactory clientFactory = Configuration.builder().setRestTemplateConfigurer(restTemplate -> restTemplate.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().add("Authorization", msoAdaptersAuth);
			return execution.execute(request, body);
		})).build().buildClientFactory();
		infraActiveRequestClient = clientFactory.create(InfraActiveRequests.class);
		operationStatusClient = clientFactory.create(OperationStatus.class);
       
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
	
	public  OperationStatus getOneByServiceIdAndOperationId(String serviceId, String operationId){
		return this.getSingleOperationStatus(UriBuilder.fromUri(findOneByServiceIdAndOperationIdURI)
				.queryParam(SERVICE_ID,serviceId)
				.queryParam(OPERATION_ID,operationId)
				.build());
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
	
	public OperationStatus getSingleOperationStatus(URI uri){
		return operationStatusClient.get(uri);
	}

	protected URI getUri(String uri) {
		return URI.create(uri);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate( new HttpComponentsClientHttpRequestFactory());
	}
}
