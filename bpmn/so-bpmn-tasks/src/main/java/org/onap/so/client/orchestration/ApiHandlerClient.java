package org.onap.so.client.orchestration;

import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import static org.onap.so.client.orchestration.RestTemplateApiClientConfig.REST_TEMPLATE_API_HANDLER;

@Component
public class ApiHandlerClient {

    @Value("${mso.adapters.apihandler.serviceInstantiationEndpoint:/onap/so/infra/serviceInstantiation/v7/serviceInstances}")
    private String serviceInstantiationEndpoint;
    @Value("${mso.adapters.apihandler.endpoint:http://localhost:8080}")
    private String baseUri;
    @Value("${mso.adapters.apihandler.auth:Basic dGVzdDp0ZXN0Cg==}")
    private String auth;

    private RestTemplate restTemplate;

    public ApiHandlerClient(@Qualifier(REST_TEMPLATE_API_HANDLER) RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ServiceInstancesResponse createServiceInstance(ServiceInstancesRequest serviceInstancesRequest)
            throws ApiHandlerClientException {
        try {
            HttpEntity<ServiceInstancesRequest> request = createRequest(serviceInstancesRequest);
            return restTemplate.exchange(baseUri + serviceInstantiationEndpoint, HttpMethod.POST, request,
                    ServiceInstancesResponse.class).getBody();
        } catch (HttpStatusCodeException e) {
            throw new ApiHandlerClientException("Failed sending service createInstance request to api-handler."
                    + " Error: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ApiHandlerClientException(
                    "Failed sending service createInstance request to api-handler." + " Error: " + e.getMessage());
        }
    }

    public ServiceInstancesResponse deleteServiceInstance(ServiceInstancesRequest serviceInstancesRequest)
            throws ApiHandlerClientException {
        try {
            HttpEntity<ServiceInstancesRequest> request = createRequest(serviceInstancesRequest);
            return restTemplate.exchange(
                    baseUri + serviceInstantiationEndpoint
                            + String.format("/%s", serviceInstancesRequest.getServiceInstanceId()),
                    HttpMethod.DELETE, request, ServiceInstancesResponse.class).getBody();
        } catch (HttpStatusCodeException e) {
            throw new ApiHandlerClientException("Failed sending service deleteInstance request to api-handler."
                    + " Error: " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            throw new ApiHandlerClientException(
                    "Failed sending service deleteInstance request to api-handler." + " Error: " + e.getMessage());
        }
    }

    private HttpEntity<ServiceInstancesRequest> createRequest(ServiceInstancesRequest serviceInstancesRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, auth);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT, String.valueOf(MediaType.APPLICATION_JSON));

        return new HttpEntity<>(serviceInstancesRequest, headers);
    }
}
