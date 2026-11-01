package org.onap.so.db.request.client;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.InstanceNfvoMapping;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class RequestDbOperationsClient {

    private final RestTemplate template;
    private final String baseUrl;
    private final String basicAuth;

    public RequestDbOperationsClient(RestTemplate template, String baseUrl, String basicAuth) {
        this.template = Objects.requireNonNull(template, "template");
        Assert.hasText(baseUrl, "baseUrl is required");
        Assert.hasText(basicAuth, "basicAuth is required");
        URI origin = URI.create(baseUrl);
        Assert.isTrue(
                ("http".equals(origin.getScheme()) || "https".equals(origin.getScheme())) && origin.getHost() != null
                        && origin.getUserInfo() == null && origin.getQuery() == null && origin.getFragment() == null
                        && (origin.getPath().isEmpty() || "/".equals(origin.getPath())),
                "baseUrl must be an HTTP server origin");
        this.baseUrl = UriComponentsBuilder.fromUri(origin).replacePath("/v1/request-db").build().toUriString();
        this.basicAuth = basicAuth;
    }

    public void updateInfraRequest(String requestId, RequestDbCommands.InfraRequestUpdate command) {
        write(HttpMethod.POST, uri("/infra-requests/{requestId}/updates", requestId), command);
    }

    public InfraActiveRequests getInfraRequest(String requestId) {
        return get(uri("/infra-requests/{requestId}", requestId), InfraActiveRequests.class);
    }

    public void setInstanceNfvoMappingRepository(String instanceId,
            RequestDbCommands.InstanceNfvoMappingUpdate command) {
        write(HttpMethod.PUT, uri("/instance-nfvo-mappings/{instanceId}", instanceId), command);
    }

    public InstanceNfvoMapping getInstanceNfvoMapping(String instanceId) {
        return getNullable(uri("/instance-nfvo-mappings/{instanceId}", instanceId), InstanceNfvoMapping.class);
    }

    public boolean getSiteStatus(String siteName) {
        return get(uri("/sites/{siteName}/status", siteName), RequestDbCommands.SiteState.class).isEnabled();
    }

    public OperationStatus getServiceOperationStatus(String serviceId, String operationId) {
        return getNullable(serviceOperationUri(serviceId, operationId), OperationStatus.class);
    }

    public void initServiceOperationStatus(RequestDbCommands.ServiceOperationWrite command) {
        write(HttpMethod.POST, uri("/service-operations"), command);
    }

    public void updateServiceOperationStatus(RequestDbCommands.ServiceOperationWrite command) {
        write(HttpMethod.PUT, serviceOperationUri(command.getServiceId(), command.getOperationId()), command);
    }

    public void initResourceOperationStatus(RequestDbCommands.ResourceOperationInit command) {
        write(HttpMethod.POST, uri("/resource-operations"), command);
    }

    public ResourceOperationStatus getResourceOperationStatus(String serviceId, String operationId,
            String resourceTemplateUUID) {
        return get(resourceOperationUri(serviceId, operationId, resourceTemplateUUID), ResourceOperationStatus.class);
    }

    public void updateResourceOperationStatus(String serviceId, String operationId, String resourceTemplateUUID,
            RequestDbCommands.ResourceOperationUpdate command) {
        write(HttpMethod.PUT, resourceOperationUri(serviceId, operationId, resourceTemplateUUID), command);
    }

    private URI serviceOperationUri(String serviceId, String operationId) {
        return uri("/service-operations?serviceId={serviceId}&operationId={operationId}", serviceId,
                operationId == null ? "" : operationId);
    }

    private URI resourceOperationUri(String serviceId, String operationId, String resourceTemplateUUID) {
        return uri("/resource-operations?serviceId={serviceId}&operationId={operationId}"
                + "&resourceTemplateUUID={resourceTemplateUUID}", serviceId, operationId, resourceTemplateUUID);
    }

    private URI uri(String path, Object... variables) {
        return UriComponentsBuilder.fromUriString(baseUrl + path).encode().buildAndExpand(variables).toUri();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, basicAuth);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private <T> T get(URI uri, Class<T> responseType) {
        T result = template.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers()), responseType).getBody();
        if (result == null) {
            throw new RestClientException("RequestDB returned an empty response");
        }
        return result;
    }

    private <T> T getNullable(URI uri, Class<T> responseType) {
        try {
            return get(uri, responseType);
        } catch (HttpClientErrorException.NotFound missing) {
            return null;
        }
    }

    private void write(HttpMethod method, URI uri, Object command) {
        HttpHeaders headers = headers();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Void> response = template.exchange(uri, method,
                new HttpEntity<>(Objects.requireNonNull(command, "command"), headers), Void.class);
        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RestClientException("Unexpected RequestDB write status: " + response.getStatusCode().value());
        }
    }
}
