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

package org.onap.so.db.request.client;

import org.apache.http.HttpStatus;
import org.onap.so.db.request.beans.ArchivedInfraRequests;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.OperationalEnvDistributionStatus;
import org.onap.so.db.request.beans.OperationalEnvServiceModelStatus;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.beans.SiteStatus;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.beans.WatchdogServiceModVerIdLookup;
import org.onap.so.db.request.data.controller.InstanceNameDuplicateCheckRequest;
import org.onap.so.logging.jaxrs.filter.SpringClientFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
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
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component("RequestsDbClient")
@Primary
public class RequestsDbClient {

	private static final String SERVICE_ID = "SERVICE_ID";
	private static final String OPERATION_ID = "OPERATION_ID";
	private static final String SO_REQUEST_ID = "SO_REQUEST_ID";
	private static final String GROUPING_ID = "GROUPING_ID";
	private static final String REQUEST_ID = "REQUEST_ID";
	private static final String OPERATIONAL_ENVIRONMENT_ID = "OPERATIONAL_ENVIRONMENT_ID";
	private static final String SERVICE_MODEL_VERSION_ID = "SERVICE_MODEL_VERSION_ID";
	private static final String NAME = "NAME";
	private static final String VALUE = "VALUE";
	private static final String TAG = "TAG";
	

	@Value("${mso.adapters.requestDb.endpoint}")
	protected String endpoint;

	@Value("${mso.adapters.requestDb.auth}")
	private String msoAdaptersAuth;

	private String getOrchestrationFilterURI = "/infraActiveRequests/getOrchestrationFiltersFromInfraActive/";
	private static final String OPERATION_STATUS_SEARCH = "/operationStatus/search";
	private static final String OPERATIONAL_ENV_SERVICE_MODEL_STATUS_SEARCH = "/operationalEnvServiceModelStatus/search";


	private String checkVnfIdStatus = "/infraActiveRequests/checkVnfIdStatus/";

	private String infraActiveRequestURI = "/infraActiveRequests/";

	private String checkInstanceNameDuplicate = "/infraActiveRequests/checkInstanceNameDuplicate";
	
	private String operationalEnvDistributionStatusURI = "/operationalEnvDistributionStatus/";
	
	private String findOneByServiceIdAndOperationIdURI = "/findOneByServiceIdAndOperationId";
	
	private String findOneByOperationalEnvIdAndServiceModelVersionIdURI = "/findOneByOperationalEnvIdAndServiceModelVersionId";
	
	private String findAllByOperationalEnvIdAndRequestIdURI = "/findAllByOperationalEnvIdAndRequestId";

	private String cloudOrchestrationFiltersFromInfraActive = "/infraActiveRequests/getCloudOrchestrationFiltersFromInfraActive";
	
	private String requestProcessingDataURI = "/requestProcessingData";
	
	private String findOneBySoRequestIdAndGroupingIdAndNameAndTagURI = "/requestProcessingData/search/findOneBySoRequestIdAndGroupingIdAndNameAndTag/";

	private String findBySoRequestIdOrderByGroupingIdDesc = "/requestProcessingData/search/findBySoRequestIdOrderByGroupingIdDesc/";


	@Autowired
	protected RestTemplate restTemplate;
	
	@Autowired
	ClassURLMapper classURLMapper;

	@PostConstruct
	public void init() {
		getOrchestrationFilterURI = endpoint + getOrchestrationFilterURI;
		infraActiveRequestURI = endpoint + infraActiveRequestURI;
		checkVnfIdStatus = endpoint + checkVnfIdStatus;
		checkInstanceNameDuplicate = endpoint + checkInstanceNameDuplicate;
		cloudOrchestrationFiltersFromInfraActive = endpoint + cloudOrchestrationFiltersFromInfraActive;
		findOneByServiceIdAndOperationIdURI = endpoint + OPERATION_STATUS_SEARCH + findOneByServiceIdAndOperationIdURI;
		requestProcessingDataURI = endpoint + requestProcessingDataURI;
		operationalEnvDistributionStatusURI = endpoint + operationalEnvDistributionStatusURI;
		findOneByOperationalEnvIdAndServiceModelVersionIdURI = endpoint + OPERATIONAL_ENV_SERVICE_MODEL_STATUS_SEARCH + findOneByOperationalEnvIdAndServiceModelVersionIdURI;
		findAllByOperationalEnvIdAndRequestIdURI = endpoint + OPERATIONAL_ENV_SERVICE_MODEL_STATUS_SEARCH + findAllByOperationalEnvIdAndRequestIdURI;
	}
	
	public ClientFactory getClientFactory(){
		URI baseUri = UriBuilder.fromUri(endpoint).build();		
		ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory());

		return Configuration.builder().setBaseUri(baseUri).setClientHttpRequestFactory(factory).setRestTemplateConfigurer(restTemplate -> {
			restTemplate.getInterceptors().add((new SpringClientFilter()));

			restTemplate.getInterceptors().add((request, body, execution) -> {

				request.getHeaders().add(HttpHeaders.AUTHORIZATION, msoAdaptersAuth);
				return execution.execute(request, body);
			});
		}).build().buildClientFactory();
	}

	
	public List<InfraActiveRequests> getCloudOrchestrationFiltersFromInfraActive(Map<String, String> orchestrationMap){
		URI uri = getUri(cloudOrchestrationFiltersFromInfraActive);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
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
        try {
        	HttpHeaders headers = new HttpHeaders();
    		headers.set("Authorization", msoAdaptersAuth);
    		HttpEntity<?> entity = new HttpEntity<>(headers);
            InfraActiveRequests infraActiveRequests = restTemplate.exchange(getUri(endpoint + "/infraActiveRequests/" + requestId), HttpMethod.GET, entity, InfraActiveRequests.class).getBody();
            if (infraActiveRequests != null) {
                infraActiveRequests.setRequestId(requestId);
            }
            return infraActiveRequests;
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                return null;
            }
            throw e;
        }
    }

	public List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive(Map<String, List<String>> orchestrationMap) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
		URI uri = getUri(getOrchestrationFilterURI);
		HttpEntity<Map<String, List<String>>> entity = new HttpEntity<>(orchestrationMap, headers);
		return restTemplate.exchange(uri, HttpMethod.POST, entity, new ParameterizedTypeReference<List<InfraActiveRequests>>() {}).getBody();
	}

	public InfraActiveRequests checkVnfIdStatus(String operationalEnvironmentId) {
    	HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		URI uri = getUri(checkVnfIdStatus + operationalEnvironmentId);
		return restTemplate.exchange(uri, HttpMethod.GET, entity, InfraActiveRequests.class).getBody();
	}
	
	public InfraActiveRequests checkInstanceNameDuplicate(HashMap<String, String> instanceIdMap, String instanceName, String requestScope) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
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

	public OperationStatus getOneByServiceIdAndOperationId(String serviceId, String operationId) {
		try {
	    	HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", msoAdaptersAuth);
			HttpEntity<?> entity = new HttpEntity<>(headers);
			OperationStatus operationStatus = restTemplate.exchange(UriBuilder.fromUri(getUri(findOneByServiceIdAndOperationIdURI))
					.queryParam(SERVICE_ID, serviceId)
					.queryParam(OPERATION_ID, operationId)
					.build(), HttpMethod.GET, entity, OperationStatus.class).getBody();
			if (operationStatus != null) {
				operationStatus.setServiceId(serviceId);
				operationStatus.setOperationId(operationId);
			}

			return operationStatus;
		} catch (HttpClientErrorException e) {
			if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
				return null;
			}
			throw e;
		}
	}
	
	public OperationalEnvServiceModelStatus findOneByOperationalEnvIdAndServiceModelVersionId(String operationalEnvironmentId, String serviceModelVersionId) {
		return this.getSingleOperationalEnvServiceModelStatus(UriBuilder.fromUri(findOneByOperationalEnvIdAndServiceModelVersionIdURI)
				.queryParam(OPERATIONAL_ENVIRONMENT_ID,operationalEnvironmentId)
				.queryParam(SERVICE_MODEL_VERSION_ID,serviceModelVersionId)
				.build());
	}

	public List<OperationalEnvServiceModelStatus> getAllByOperationalEnvIdAndRequestId(String operationalEnvironmentId, String requestId){
		return this.getMultipleOperationalEnvServiceModelStatus(UriBuilder.fromUri(findAllByOperationalEnvIdAndRequestIdURI)
				.queryParam(OPERATIONAL_ENVIRONMENT_ID,operationalEnvironmentId)
				.queryParam(REQUEST_ID,requestId)
				.build());
	}
	
	public OperationalEnvDistributionStatus getDistributionStatusById(String distributionId){
		return this.getSingleOperationalEnvDistributionStatus(UriBuilder.fromUri(operationalEnvDistributionStatusURI+distributionId).build());
	}
	
	private OperationalEnvServiceModelStatus getSingleOperationalEnvServiceModelStatus(URI uri){
		return getClientFactory().create(OperationalEnvServiceModelStatus.class).get(uri);
	}

	private List<OperationalEnvServiceModelStatus> getMultipleOperationalEnvServiceModelStatus(URI uri){
		Iterable <OperationalEnvServiceModelStatus> iterable = getClientFactory().create(OperationalEnvServiceModelStatus.class).getAll(uri);
		List<OperationalEnvServiceModelStatus> serviceModelStatuses = new ArrayList<>();
		Iterator<OperationalEnvServiceModelStatus> statusIterator = iterable.iterator();
		statusIterator.forEachRemaining(serviceModelStatuses::add);
		return serviceModelStatuses;
	}

	public void save(InfraActiveRequests infraActiveRequests) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
		URI uri = getUri(infraActiveRequestURI);
		HttpEntity<InfraActiveRequests> entity = new HttpEntity<>(infraActiveRequests, headers);
		restTemplate.postForLocation(uri, entity);
	}

	public <T> void save(T object){
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
		URI uri = getUri(endpoint+classURLMapper.getURI(object.getClass()));
		HttpEntity<T> entity = new HttpEntity<>(object, headers);
		restTemplate.postForLocation(uri, entity);
	}
	
	private OperationalEnvDistributionStatus getSingleOperationalEnvDistributionStatus(URI uri){
		return getClientFactory().create(OperationalEnvDistributionStatus.class).get(uri);
	}

	public void updateInfraActiveRequests(InfraActiveRequests request) {				
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
		headers.set(HttpHeaders.CONTENT_TYPE,"application/json");
		headers.set(HttpHeaders.ACCEPT,  "application/json");
		URI uri = getUri(infraActiveRequestURI+request.getRequestId());
		HttpEntity<InfraActiveRequests> entity = new HttpEntity<>(request, headers);
		restTemplate.put(uri, entity);
	}
	
	protected URI getUri(String uri) {
		return URI.create(uri);
	}

	public void saveRequestProcessingData(RequestProcessingData requestProcessingData) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", msoAdaptersAuth);
		URI uri = getUri(endpoint + requestProcessingDataURI);
		HttpEntity<RequestProcessingData> entity = new HttpEntity<>(requestProcessingData, headers);
		restTemplate.postForLocation(uri, entity);
	}
	
	public RequestProcessingData getRequestProcessingDataBySoRequestIdAndGroupingIdAndNameAndTag(String soRequestId,
			String groupingId, String name, String tag) {
		return this.getSingleRequestProcessingData(UriBuilder.fromUri(endpoint + findOneBySoRequestIdAndGroupingIdAndNameAndTagURI)
				.queryParam(SO_REQUEST_ID,soRequestId)
				.queryParam(GROUPING_ID,groupingId)
				.queryParam(NAME,name)
				.queryParam(TAG,tag)
				.build());
	}
	public List<RequestProcessingData> getRequestProcessingDataBySoRequestId(String soRequestId) {
		return this.getRequestProcessingData(UriBuilder.fromUri(endpoint + findBySoRequestIdOrderByGroupingIdDesc)
				.queryParam(SO_REQUEST_ID,soRequestId)
				.build());
	}
	
	public RequestProcessingData getSingleRequestProcessingData(URI uri){
		return getClientFactory().create(RequestProcessingData.class).get(uri);
	}
	
	private List<RequestProcessingData> getRequestProcessingData(URI uri) {
		Iterable<RequestProcessingData> requestProcessingDataIterator = getClientFactory().create(RequestProcessingData.class).getAll(uri);
		List<RequestProcessingData> requestProcessingDataList = new ArrayList<>();
		Iterator<RequestProcessingData> it = requestProcessingDataIterator.iterator();
		it.forEachRemaining(requestProcessingDataList::add);
		return requestProcessingDataList;
	}
	
	public List<RequestProcessingData> getAllRequestProcessingData() {
		
		return (List<RequestProcessingData>) this.getAllRequestProcessingData(UriBuilder.fromUri(endpoint + "/requestProcessingData").build());
	}
	
	private Iterable<RequestProcessingData> getAllRequestProcessingData(URI uri) {		
		return getClientFactory().create(RequestProcessingData.class).getAll(uri);
	}

	@Component
	static class ClassURLMapper {
		private static final Map <Class,String> classURLMap = new HashMap<>();

		ClassURLMapper() {
			classURLMap.put(ArchivedInfraRequests.class,"/archivedInfraRequests/");
			classURLMap.put(InfraActiveRequests.class,"/infraActiveRequests/");
			classURLMap.put(OperationalEnvDistributionStatus.class,"/operationalEnvDistributionStatus/");
			classURLMap.put(OperationalEnvServiceModelStatus.class,"/operationalEnvServiceModelStatus/");
			classURLMap.put(OperationStatus.class,"/operationStatus/");
			classURLMap.put(ResourceOperationStatus.class,"/resourceOperationStatus/");
			classURLMap.put(SiteStatus.class,"/siteStatus/");
			classURLMap.put(WatchdogComponentDistributionStatus.class,"/watchdogComponentDistributionStatus/");
			classURLMap.put(WatchdogDistributionStatus.class,"/watchdogDistributionStatus/");
			classURLMap.put(WatchdogServiceModVerIdLookup.class,"/watchdogServiceModVerIdLookup/");
		}

		  <T> String getURI(Class<T> className) {
			  Class actualClass = classURLMap.keySet()
					  .stream()
					  .filter(requestdbClass -> requestdbClass.isAssignableFrom(className))
					  .findFirst()
					  .get();
			  return classURLMap.get(actualClass);
		}
	}
	
	//USED FOR TEST ONLY
	public void setPortToEndpoint(String port) {
		endpoint = endpoint + port;
	}
	
	//USED FOR TEST ONLY
	public void removePortFromEndpoint() {
		endpoint = endpoint.substring(0, endpoint.lastIndexOf(':') + 1);
	}
}
