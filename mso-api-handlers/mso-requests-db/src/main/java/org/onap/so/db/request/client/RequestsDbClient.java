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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.http.HttpStatus;
import org.onap.so.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.db.request.beans.ArchivedInfraRequests;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.OperationalEnvDistributionStatus;
import org.onap.so.db.request.beans.OperationalEnvServiceModelStatus;
import org.onap.so.db.request.beans.OrchestrationTask;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.beans.SiteStatus;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.beans.WatchdogDistributionStatus;
import org.onap.so.db.request.beans.WatchdogServiceModVerIdLookup;
import org.onap.so.db.request.data.controller.InstanceNameDuplicateCheckRequest;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component("RequestsDbClient")
@Primary
public class RequestsDbClient {

    private static final String SERVICE_ID = "SERVICE_ID";
    private static final String OPERATION_ID = "OPERATION_ID";
    private static final String SO_REQUEST_ID = "SO_REQUEST_ID";
    private static final String IS_INTERNAL_DATA = "IS_INTERNAL_DATA";
    private static final String NAME = "NAME";
    private static final String GROUPING_ID = "GROUPING_ID";
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String OPERATIONAL_ENVIRONMENT_ID = "OPERATIONAL_ENV_ID";
    private static final String SERVICE_MODEL_VERSION_ID = "SERVICE_MODEL_VERSION_ID";
    private static final String TAG = "TAG";
    private static final String FLOW_EXECUTION_PATH = "flowExecutionPath";
    private static final String BPMN_EXECUTION_DATA_TAG = "BPMNExecutionData";

    @Value("${mso.adapters.requestDb.endpoint:#{null}}")
    protected String endpoint;

    @Value("${mso.adapters.requestDb.auth:#{null}}")
    protected String msoAdaptersAuth;

    private String getOrchestrationFilterURI = "/infraActiveRequests/getOrchestrationFiltersFromInfraActive/";
    private static final String OPERATION_STATUS_SEARCH = "/operationStatus/search";
    private static final String OPERATIONAL_ENV_SERVICE_MODEL_STATUS_SEARCH =
            "/operationalEnvServiceModelStatus/search";


    private String checkVnfIdStatus = "/infraActiveRequests/checkVnfIdStatus/";

    private String infraActiveRequestURI = "/infraActiveRequests/";

    private String checkInstanceNameDuplicate = "/infraActiveRequests/checkInstanceNameDuplicate";

    private String operationalEnvDistributionStatusURI = "/operationalEnvDistributionStatus/";

    private String findOneByServiceIdAndOperationIdURI = "/findOneByServiceIdAndOperationId";

    private String findOneByRequestId = "/infraActiveRequests/search/findOneByRequestId";

    private String findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestIdURI =
            "/findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId";

    private String findAllByOperationalEnvIdAndRequestIdURI = "/findAllByOperationalEnvIdAndRequestId";

    private String cloudOrchestrationFiltersFromInfraActive =
            "/infraActiveRequests/getCloudOrchestrationFiltersFromInfraActive";

    private String requestProcessingDataURI = "/requestProcessingData";

    private String getInfraActiveRequests = "/infraActiveRequests/v1/getInfraActiveRequests";

    private String getInProgressVolumeGroupsAndVfModules = "/infraActiveRequests/getInProgressVolumeGroupsAndVfModules";

    private String orchestrationTasksURI = "/orchestrationTask";

    private static final String findBySoRequestIdAndGroupIdAndName =
            "/requestProcessingData/search/findOneBySoRequestIdAndGroupingIdAndName";

    private static final String findBySoRequestIdAndName = "/requestProcessingData/search/findOneBySoRequestIdAndName";

    private static final String findBySoRequestIdOrderByGroupingIdDesc =
            "/requestProcessingData/search/findBySoRequestIdOrderByGroupingIdDesc";

    private static final String findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc =
            "/requestProcessingData/search/findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc";

    private static final String findByGroupingIdAndNameAndTag =
            "/requestProcessingData/search/findByGroupingIdAndNameAndTag";

    private static final String findBySoRequestIdAndNameAndTagOrderByCreateTimeDesc =
            "/requestProcessingData/search/findBySoRequestIdAndNameAndTagOrderByCreatedDesc";

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
        getInfraActiveRequests = endpoint + getInfraActiveRequests;
        operationalEnvDistributionStatusURI = endpoint + operationalEnvDistributionStatusURI;
        findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestIdURI =
                endpoint + OPERATIONAL_ENV_SERVICE_MODEL_STATUS_SEARCH
                        + findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestIdURI;
        findAllByOperationalEnvIdAndRequestIdURI =
                endpoint + OPERATIONAL_ENV_SERVICE_MODEL_STATUS_SEARCH + findAllByOperationalEnvIdAndRequestIdURI;
        findOneByRequestId = endpoint + findOneByRequestId;
        orchestrationTasksURI = endpoint + orchestrationTasksURI;
    }

    protected String getEndpoint() {
        return endpoint;
    }

    public List<InfraActiveRequests> getCloudOrchestrationFiltersFromInfraActive(Map<String, String> orchestrationMap) {
        URI uri = getUri(cloudOrchestrationFiltersFromInfraActive);
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(orchestrationMap, headers);
        try {
            return restTemplate.exchange(uri, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<List<InfraActiveRequests>>() {}).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                return null;
            }
            throw e;
        }
    }

    public List<InfraActiveRequests> getInProgressVolumeGroupsAndVfModules() {
        URI uri = getUri(endpoint + getInProgressVolumeGroupsAndVfModules);

        return getMultipleResponse(uri, new ParameterizedTypeReference<List<InfraActiveRequests>>() {});

    }

    public InfraActiveRequests getInfraActiveRequestbyRequestId(String requestId) {


        InfraActiveRequests infraActiveRequests =
                getSingleResponse(getUri(endpoint + "/infraActiveRequests/" + requestId), InfraActiveRequests.class);
        if (infraActiveRequests != null) {
            infraActiveRequests.setRequestId(requestId);
        }
        return infraActiveRequests;
    }

    public List<InfraActiveRequests> getOrchestrationFiltersFromInfraActive(
            Map<String, List<String>> orchestrationMap) {
        HttpHeaders headers = getHttpHeaders();
        URI uri = getUri(getOrchestrationFilterURI);
        HttpEntity<Map<String, List<String>>> entity = new HttpEntity<>(orchestrationMap, headers);
        return restTemplate
                .exchange(uri, HttpMethod.POST, entity, new ParameterizedTypeReference<List<InfraActiveRequests>>() {})
                .getBody();
    }

    public InfraActiveRequests checkVnfIdStatus(String operationalEnvironmentId) {
        URI uri = getUri(checkVnfIdStatus + operationalEnvironmentId);
        return getSingleResponse(uri, InfraActiveRequests.class);
    }

    public InfraActiveRequests checkInstanceNameDuplicate(Map<String, String> instanceIdMap, String instanceName,
            String requestScope) {
        HttpHeaders headers = getHttpHeaders();
        URI uri = getUri(checkInstanceNameDuplicate);
        HttpEntity<InstanceNameDuplicateCheckRequest> entity = new HttpEntity<>(
                new InstanceNameDuplicateCheckRequest(instanceIdMap, instanceName, requestScope), headers);

        return postSingleResponse(uri, entity, InfraActiveRequests.class);

    }

    public OperationStatus getOneByServiceIdAndOperationId(String serviceId, String operationId) {
        OperationStatus operationStatus = getSingleResponse(
                getUri(UriBuilder.fromUri(getUri(findOneByServiceIdAndOperationIdURI)).queryParam(SERVICE_ID, serviceId)
                        .queryParam(OPERATION_ID, operationId).build().toString()),
                OperationStatus.class);
        if (operationStatus != null) {
            operationStatus.setServiceId(serviceId);
            operationStatus.setOperationId(operationId);
        }
        return operationStatus;
    }

    public OperationalEnvServiceModelStatus findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestId(
            String operationalEnvironmentId, String serviceModelVersionId, String requestId) {

        OperationalEnvServiceModelStatus modelStatus =
                getSingleResponse(
                        getUri(UriBuilder.fromUri(findOneByOperationalEnvIdAndServiceModelVersionIdAndRequestIdURI)
                                .queryParam(OPERATIONAL_ENVIRONMENT_ID, operationalEnvironmentId)
                                .queryParam(SERVICE_MODEL_VERSION_ID, serviceModelVersionId)
                                .queryParam(REQUEST_ID, requestId).build().toString()),
                        OperationalEnvServiceModelStatus.class);
        if (null != modelStatus) {
            modelStatus.setOperationalEnvId(operationalEnvironmentId);
            modelStatus.setServiceModelVersionId(serviceModelVersionId);
            modelStatus.setRequestId(requestId);
        }
        return modelStatus;

    }

    public List<OperationalEnvServiceModelStatus> getAllByOperationalEnvIdAndRequestId(String operationalEnvironmentId,
            String requestId) {
        return this.getMultipleOperationalEnvServiceModelStatus(
                getUri(UriBuilder.fromUri(findAllByOperationalEnvIdAndRequestIdURI)
                        .queryParam(OPERATIONAL_ENVIRONMENT_ID, operationalEnvironmentId)
                        .queryParam(REQUEST_ID, requestId).build().toString()));
    }

    public OperationalEnvDistributionStatus getDistributionStatusById(String distributionId) {

        OperationalEnvDistributionStatus distributionStatus = getSingleResponse(
                getUri(operationalEnvDistributionStatusURI + distributionId), OperationalEnvDistributionStatus.class);
        if (null != distributionStatus) {
            distributionStatus.setDistributionId(distributionId);
        }
        return distributionStatus;

    }

    private List<OperationalEnvServiceModelStatus> getMultipleOperationalEnvServiceModelStatus(URI uri) {
        return getMultipleResponse(uri, new ParameterizedTypeReference<List<OperationalEnvServiceModelStatus>>() {});
    }

    public void save(InfraActiveRequests infraActiveRequests) {
        HttpHeaders headers = getHttpHeaders();
        URI uri = getUri(infraActiveRequestURI);
        HttpEntity<InfraActiveRequests> entity = new HttpEntity<>(infraActiveRequests, headers);
        restTemplate.postForLocation(uri, entity);
    }

    public <T> void save(T object) {
        HttpHeaders headers = getHttpHeaders();
        URI uri = getUri(endpoint + classURLMapper.getURI(object.getClass()));
        HttpEntity<T> entity = new HttpEntity<>(object, headers);
        restTemplate.postForLocation(uri, entity);
    }

    // Method to update InfraActiveRequests
    public void updateInfraActiveRequests(InfraActiveRequests request) {
        HttpHeaders headers = getHttpHeaders();
        URI uri = getUri(infraActiveRequestURI + request.getRequestId());
        HttpEntity<InfraActiveRequests> entity = new HttpEntity<>(request, headers);
        restTemplate.put(uri, entity);
    }

    public void patchInfraActiveRequests(InfraActiveRequests request) {
        HttpHeaders headers = getHttpHeaders();
        URI uri = getUri(infraActiveRequestURI + request.getRequestId());
        HttpEntity<InfraActiveRequests> entity = new HttpEntity<>(request, headers);
        restTemplate.exchange(uri, HttpMethod.PATCH, entity, String.class);
    }

    /**
     * Required for groovy usage. Cannot use Spring Autowired variables
     *
     * @param requestId
     * @param basicAuth
     * @param host
     * @return
     */
    public InfraActiveRequests getInfraActiveRequests(String requestId, String basicAuth, String host) {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, basicAuth);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        URI uri = getUri(host + "/infraActiveRequests/" + requestId);
        try {
            InfraActiveRequests infraActiveRequests = template
                    .exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), InfraActiveRequests.class).getBody();
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

    /**
     * Required for groovy usage. Cannot use Spring Autowired variables
     *
     * @param request
     * @param basicAuth
     * @param host
     */
    public void updateInfraActiveRequests(InfraActiveRequests request, String basicAuth, String host) {
        RestTemplate template = new RestTemplate();
        template.getInterceptors().add(new SOSpringClientFilter());
        template.getInterceptors().add(new SpringClientPayloadFilter());
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, basicAuth);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        URI uri = getUri(host + "/infraActiveRequests/" + request.getRequestId());
        HttpEntity<InfraActiveRequests> entity = new HttpEntity<>(request, headers);
        template.put(uri, entity);
    }

    protected URI getUri(String uri) {
        return URI.create(uri);
    }

    public void saveRequestProcessingData(RequestProcessingData requestProcessingData) {
        HttpHeaders headers = getHttpHeaders();
        URI uri = getUri(requestProcessingDataURI);
        HttpEntity<RequestProcessingData> entity = new HttpEntity<>(requestProcessingData, headers);
        restTemplate.postForLocation(uri, entity);
    }

    public void updateRequestProcessingData(RequestProcessingData requestProcessingData) {
        HttpHeaders headers = getHttpHeaders();
        URI uri = getUri(requestProcessingDataURI + "/" + requestProcessingData.getId());
        HttpEntity<RequestProcessingData> entity = new HttpEntity<>(requestProcessingData, headers);
        restTemplate.put(uri, entity);
    }

    public List<RequestProcessingData> getRequestProcessingDataBySoRequestId(String soRequestId) {
        return this
                .getRequestProcessingData(getUri(UriBuilder.fromUri(endpoint + findBySoRequestIdOrderByGroupingIdDesc)
                        .queryParam(SO_REQUEST_ID, soRequestId).build().toString()));
    }

    public List<RequestProcessingData> getExternalRequestProcessingDataBySoRequestId(String soRequestId) {
        URI uri = getUri(UriBuilder.fromUri(getEndpoint() + findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc)
                .queryParam(SO_REQUEST_ID, soRequestId).queryParam(IS_INTERNAL_DATA, false).build().toString());
        return getMultipleResponse(uri, new ParameterizedTypeReference<List<RequestProcessingData>>() {});
    }

    public RequestProcessingData getRequestProcessingDataBySoRequestIdAndNameAndGrouping(String soRequestId,
            String name, String groupingId) {
        return getSingleResponse(getUri(
                UriBuilder.fromUri(endpoint + findBySoRequestIdAndGroupIdAndName).queryParam(SO_REQUEST_ID, soRequestId)
                        .queryParam(NAME, name).queryParam(GROUPING_ID, groupingId).build().toString()),
                RequestProcessingData.class);
    }

    public List<RequestProcessingData> getRequestProcessingDataByGroupingIdAndNameAndTag(String groupingId, String name,
            String tag) {

        return getMultipleResponse(
                getUri(UriBuilder.fromUri(endpoint + findByGroupingIdAndNameAndTag).queryParam(GROUPING_ID, groupingId)
                        .queryParam(NAME, name).queryParam(TAG, tag).build().toString()),
                new ParameterizedTypeReference<List<RequestProcessingData>>() {});
    }

    public RequestProcessingData getRequestProcessingDataBySoRequestIdAndName(String soRequestId, String name) {
        return getSingleResponse(getUri(UriBuilder.fromUri(endpoint + findBySoRequestIdAndName)
                .queryParam(SO_REQUEST_ID, soRequestId).queryParam(NAME, name).build().toString()),
                RequestProcessingData.class);
    }

    public List<RequestProcessingData> getRequestProcessingDataBySoRequestIdAndNameAndTagOrderByCreateTimeDesc(
            String soRequestId, String name, String tag) {

        return getMultipleResponse(getUri(UriBuilder
                .fromUri(endpoint + findBySoRequestIdAndNameAndTagOrderByCreateTimeDesc)
                .queryParam(SO_REQUEST_ID, soRequestId).queryParam(NAME, name).queryParam(TAG, tag).build().toString()),
                new ParameterizedTypeReference<List<RequestProcessingData>>() {});
    }


    public void persistProcessingData(String flowExecutionPath, String requestId) {

        HttpHeaders headers = getHttpHeaders();
        URI uri = getUri(requestProcessingDataURI);
        RequestProcessingData rpd = new RequestProcessingData();
        rpd.setName(FLOW_EXECUTION_PATH);
        rpd.setSoRequestId(requestId);
        rpd.setValue(flowExecutionPath);
        rpd.setTag(BPMN_EXECUTION_DATA_TAG);
        rpd.setIsDataInternal(true);

        HttpEntity<RequestProcessingData> entity = new HttpEntity<>(rpd, headers);
        restTemplate.postForLocation(uri, entity);
    }

    private List<RequestProcessingData> getRequestProcessingData(URI uri) {
        return getMultipleResponse(uri, new ParameterizedTypeReference<List<RequestProcessingData>>() {});
    }

    public InfraActiveRequests findOneByRequestId(String requestId) {
        return getSingleResponse(
                getUri(UriBuilder.fromUri(findOneByRequestId).queryParam(REQUEST_ID, requestId).build().toString()),
                InfraActiveRequests.class);

    }

    // From and To are defaulted to ignore start/endtime on query to database
    public List<InfraActiveRequests> getRequest(final Map<String, String[]> filters) {
        String url = UriBuilder.fromUri(getUri(getInfraActiveRequests)).queryParam("from", "0")
                .queryParam("to", "10000000000000").build().toString();
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<Map<String, String[]>> entity = new HttpEntity<>(filters, headers);
        return restTemplate
                .exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<List<InfraActiveRequests>>() {})
                .getBody();
    }

    public List<OrchestrationTask> getAllOrchestrationTasks() {
        return getMultipleResponse(getUri(orchestrationTasksURI),
                new ParameterizedTypeReference<List<OrchestrationTask>>() {});
    }

    public OrchestrationTask getOrchestrationTask(String taskId) {
        String url = UriBuilder.fromUri(getUri(orchestrationTasksURI + "/" + taskId)).build().toString();
        HttpEntity<?> entity = getHttpEntity();
        return restTemplate.exchange(url, HttpMethod.GET, entity, OrchestrationTask.class).getBody();
    }

    public OrchestrationTask createOrchestrationTask(OrchestrationTask orchestrationTask) {
        String url = UriBuilder.fromUri(getUri(orchestrationTasksURI + "/")).build().toString();
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<OrchestrationTask> entity = new HttpEntity<>(orchestrationTask, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, OrchestrationTask.class).getBody();
    }

    public OrchestrationTask updateOrchestrationTask(String taskId, OrchestrationTask orchestrationTask) {
        String url = getUri(orchestrationTasksURI + "/" + taskId).toString();
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<OrchestrationTask> entity = new HttpEntity<>(orchestrationTask, headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, OrchestrationTask.class).getBody();
    }

    public void deleteOrchestrationTask(String taskId) {
        String url = getUri(orchestrationTasksURI + "/" + taskId).toString();
        HttpEntity<?> entity = getHttpEntity();
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class).getBody();
    }

    @Component
    static class ClassURLMapper {
        private static final Map<Class<?>, String> classURLMap = new HashMap<>();

        ClassURLMapper() {
            classURLMap.put(ArchivedInfraRequests.class, "/archivedInfraRequests/");
            classURLMap.put(InfraActiveRequests.class, "/infraActiveRequests/");
            classURLMap.put(OperationalEnvDistributionStatus.class, "/operationalEnvDistributionStatus/");
            classURLMap.put(OperationalEnvServiceModelStatus.class, "/operationalEnvServiceModelStatus/");
            classURLMap.put(OperationStatus.class, "/operationStatus/");
            classURLMap.put(ResourceOperationStatus.class, "/resourceOperationStatus/");
            classURLMap.put(SiteStatus.class, "/siteStatus/");
            classURLMap.put(WatchdogComponentDistributionStatus.class, "/watchdogComponentDistributionStatus/");
            classURLMap.put(WatchdogDistributionStatus.class, "/watchdogDistributionStatus/");
            classURLMap.put(WatchdogServiceModVerIdLookup.class, "/watchdogServiceModVerIdLookup/");
        }

        <T> String getURI(Class<T> className) {
            Class<?> actualClass =
                    classURLMap.keySet().stream().filter(requestdbClass -> requestdbClass.isAssignableFrom(className))
                            .<Class<?>>map(Class.class::cast).findFirst().get();
            return classURLMap.get(actualClass);
        }
    }

    // USED FOR TEST ONLY
    public void setPortToEndpoint(String port) {
        endpoint = endpoint + port;
    }

    // USED FOR TEST ONLY
    public void removePortFromEndpoint() {
        endpoint = endpoint.substring(0, endpoint.lastIndexOf(':') + 1);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, msoAdaptersAuth);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        return headers;
    }

    private HttpEntity<?> getHttpEntity() {
        HttpHeaders headers = getHttpHeaders();
        return new HttpEntity<>(headers);
    }

    private <T> T getSingleResponse(URI uri, Class<T> clazz) {
        try {
            HttpEntity<?> entity = getHttpEntity();
            return restTemplate.exchange(uri, HttpMethod.GET, entity, clazz).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                return null;
            }
            throw e;
        }
    }

    private <T> List<T> getMultipleResponse(URI uri, ParameterizedTypeReference<List<T>> type) {
        try {
            HttpEntity<?> entity = getHttpEntity();
            return restTemplate.exchange(uri, HttpMethod.GET, entity, type).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                return new ArrayList<T>();
            }
            throw e;
        }
    }

    private <T> T postSingleResponse(URI uri, HttpEntity<?> payload, Class<T> clazz) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(payload.getBody(), getHttpHeaders());
            return restTemplate.exchange(uri, HttpMethod.POST, entity, clazz).getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                return null;
            }
            throw e;
        }
    }

    private <T> List<T> postMultipleResponse(URI uri, HttpEntity<?> payload, ParameterizedTypeReference<List<T>> type) {
        try {
            HttpEntity<?> entity = new HttpEntity<>(payload.getBody(), getHttpHeaders());
            ResponseEntity<List<T>> result = restTemplate.exchange(uri, HttpMethod.POST, entity, type);

            return result.getBody();
        } catch (HttpClientErrorException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode().value()) {
                return new ArrayList<T>();
            }
            throw e;
        }
    }

}
