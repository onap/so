package org.onap.so.adapters.requestsdb.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Map;
import java.util.UUID;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.requestsdb.RequestsAdapterBase;
import org.onap.so.adapters.requestsdb.RequestDbOperationsController;
import org.onap.so.adapters.requestsdb.RequestDbOperationsService;
import org.onap.so.adapters.requestsdb.MsoRequestsDbAdapter;
import org.onap.so.adapters.requestsdb.RequestStatusType;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.OperationStatus;
import org.onap.so.db.request.beans.InstanceNfvoMapping;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.requestsdb.RequestsDbConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class RequestDbOperationsRestTest extends RequestsAdapterBase {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private Environment environment;

    @Autowired
    private InfraActiveRequestsRepository infraRequests;

    private TestRestTemplate authenticated;
    private String identifier;

    @LocalServerPort
    private int port;

    private MsoRequestsDbAdapter soap() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(MsoRequestsDbAdapter.class);
        factory.setAddress("http://localhost:" + port + "/services/RequestsDbAdapter");
        factory.setUsername(environment.getRequiredProperty("so.auth.username"));
        factory.setPassword(environment.getRequiredProperty("so.auth.password"));
        return (MsoRequestsDbAdapter) factory.create();
    }

    @Before
    public void configureClient() {
        authenticated = template.withBasicAuth(environment.getRequiredProperty("so.auth.username"),
                environment.getRequiredProperty("so.auth.password"));
        identifier = UUID.randomUUID().toString();
    }

    @Test
    public void absentSiteIsEnabled() {
        ResponseEntity<Map> response =
                authenticated.getForEntity("/v1/request-db/sites/" + identifier + "/status", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("enabled", true);
    }

    @Test
    public void infraUpdatePreservesOmittedFieldsAndSetsTerminalTime() {
        InfraActiveRequests original = new InfraActiveRequests();
        original.setRequestId(identifier);
        original.setRequestStatus("IN_PROGRESS");
        original.setResponseBody("retained");
        original.setProgress(12L);
        infraRequests.save(original);

        ResponseEntity<Void> update = authenticated.postForEntity(
                "/v1/request-db/infra-requests/" + identifier + "/updates",
                Map.of("lastModifiedBy", "REST", "requestStatus", "COMPLETE", "progress", "invalid"), Void.class);
        ResponseEntity<InfraActiveRequests> response =
                authenticated.getForEntity("/v1/request-db/infra-requests/" + identifier, InfraActiveRequests.class);

        assertThat(update.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getResponseBody()).isEqualTo("retained");
        assertThat(response.getBody().getProgress()).isEqualTo(12L);
        assertThat(response.getBody().getEndTime()).isNotNull();
        assertThat(response.getBody().getRequestStatus()).isEqualTo("COMPLETE");
    }

    @Test
    public void missingInfraRequestReturnsSanitizedNotFound() {
        ResponseEntity<Map> response =
                authenticated.getForEntity("/v1/request-db/infra-requests/" + identifier, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKeys("code", "message", "requestId");
        assertThat(response.getBody().toString()).doesNotContain("Exception", "SQL", "stackTrace");
    }

    @Test
    public void missingInfraUpdateReturnsNotFound() {
        ResponseEntity<Void> response =
                authenticated.postForEntity("/v1/request-db/infra-requests/" + identifier + "/updates",
                        Map.of("lastModifiedBy", "REST"), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void invalidStatusReturnsBadRequest() {
        ResponseEntity<Void> response =
                authenticated.postForEntity("/v1/request-db/infra-requests/" + identifier + "/updates",
                        Map.of("lastModifiedBy", "REST", "requestStatus", "INVALID"), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void nfvoMappingCanBeWrittenAndRead() {
        String path = "/v1/request-db/instance-nfvo-mappings/" + identifier;
        ResponseEntity<Void> update = authenticated.exchange(path, HttpMethod.PUT,
                new HttpEntity<>(Map.of("nfvoName", "nfvo", "endpoint", "https://nfvo.example", "apiRoot", "v1")),
                Void.class);
        ResponseEntity<Map> response = authenticated.getForEntity(path, Map.class);

        assertThat(update.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("nfvoName", "nfvo").containsEntry("apiRoot", "v1");
        assertThat(authenticated.getForEntity(path + "-absent", Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void serviceOperationSupportsInitializationLookupAndUpsert() {
        Map<String, String> command =
                Map.of("serviceId", identifier, "operationId", "operation", "operationType", "CREATE", "userId", "user",
                        "result", "processing", "operationContent", "content", "progress", "10", "reason", "");
        assertThat(
                authenticated.postForEntity("/v1/request-db/service-operations", command, Void.class).getStatusCode())
                        .isEqualTo(HttpStatus.NO_CONTENT);

        OperationStatus found = authenticated.getForObject("/v1/request-db/service-operations?serviceId=" + identifier,
                OperationStatus.class);
        assertThat(found.getOperationId()).isEqualTo("operation");
        assertThat(found.getProgress()).isEqualTo("10");

        String path = "/v1/request-db/service-operations?serviceId=" + identifier + "&operationId=upsert";
        assertThat(authenticated.exchange(path, HttpMethod.PUT, new HttpEntity<>(command), Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(authenticated.getForObject(path, OperationStatus.class).getOperationId()).isEqualTo("upsert");
        assertThat(authenticated.getForEntity(path + "-absent", Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void resourceOperationsPreserveAggregateUpdate() {
        assertThat(authenticated.postForEntity("/v1/request-db/resource-operations",
                Map.of("serviceId", identifier, "operationId", "operation", "operationType", "CREATE",
                        "resourceTemplateUUIDs", "resource:"),
                Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        String path = "/v1/request-db/resource-operations?serviceId=" + identifier
                + "&operationId=operation&resourceTemplateUUID=resource";
        ResourceOperationStatus initial = authenticated.getForObject(path, ResourceOperationStatus.class);
        assertThat(initial.getStatus()).isEqualTo(RequestsDbConstant.Status.PROCESSING);
        assertThat(initial.getStatusDescription()).isEqualTo("Waiting for start");

        Map<String, String> update = Map.of("operType", "CREATE", "status", RequestsDbConstant.Status.FINISHED,
                "progress", "50", "statusDescription", "done");
        assertThat(authenticated.exchange(path, HttpMethod.PUT, new HttpEntity<>(update), Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        OperationStatus aggregate = authenticated.getForObject(
                "/v1/request-db/service-operations?serviceId=" + identifier + "&operationId=operation",
                OperationStatus.class);
        assertThat(aggregate.getProgress()).isEqualTo("100");
        assertThat(aggregate.getResult()).isEqualTo(RequestsDbConstant.Status.FINISHED);
        assertThat(authenticated.getForEntity(path + "-absent", Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void unauthenticatedRequestsAreRejected() {
        assertThat(template.getForEntity("/v1/request-db/sites/" + identifier + "/status", Void.class).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void internalArgumentFailureIsSanitizedServerError() throws Exception {
        RequestDbOperationsService operations = mock(RequestDbOperationsService.class);
        when(operations.getSiteStatus("site")).thenThrow(new IllegalArgumentException("internal-detail"));

        MockMvcBuilders.standaloneSetup(new RequestDbOperationsController(operations)).build()
                .perform(get("/v1/request-db/sites/site/status")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("RequestDB operation failed"));
    }

    @Test
    public void malformedJsonReturnsBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = authenticated.postForEntity(
                "/v1/request-db/infra-requests/" + identifier + "/updates", new HttpEntity<>("{", headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().toString()).doesNotContain("Exception", "stackTrace");
    }

    @Test
    public void infraMutationMatchesSoapForEveryField() throws Exception {
        String restId = identifier + "-rest";
        for (String requestId : new String[] {identifier, restId}) {
            InfraActiveRequests request = new InfraActiveRequests();
            request.setRequestId(requestId);
            request.setRequestStatus("IN_PROGRESS");
            infraRequests.save(request);
        }
        MsoRequestsDbAdapter legacy = soap();
        legacy.updateInfraRequest(identifier, "actor", "message", "body", RequestStatusType.COMPLETE, "75", "outputs",
                "service", "network", "vnf", "module", "volume", "service-name", "configuration", "config-name",
                "module-name");
        Map<String, String> command = Map.ofEntries(Map.entry("lastModifiedBy", "actor"),
                Map.entry("statusMessage", "message"), Map.entry("responseBody", "body"),
                Map.entry("requestStatus", "COMPLETE"), Map.entry("progress", "75"), Map.entry("vnfOutputs", "outputs"),
                Map.entry("serviceInstanceId", "service"), Map.entry("networkId", "network"), Map.entry("vnfId", "vnf"),
                Map.entry("vfModuleId", "module"), Map.entry("volumeGroupId", "volume"),
                Map.entry("serviceInstanceName", "service-name"), Map.entry("configurationId", "configuration"),
                Map.entry("configurationName", "config-name"), Map.entry("vfModuleName", "module-name"));
        assertThat(
                authenticated.postForEntity("/v1/request-db/infra-requests/" + restId + "/updates", command, Void.class)
                        .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        InfraActiveRequests expected = legacy.getInfraRequest(identifier);
        InfraActiveRequests actual =
                authenticated.getForObject("/v1/request-db/infra-requests/" + restId, InfraActiveRequests.class);
        assertThat(actual).usingRecursiveComparison().ignoringFields("requestId", "endTime", "startTime", "modifyTime")
                .isEqualTo(expected);
        assertThat(actual.getEndTime()).isNotNull();
        assertThat(expected.getEndTime()).isNotNull();
    }

    @Test
    public void nfvoMutationAndSiteLookupMatchSoap() throws Exception {
        MsoRequestsDbAdapter legacy = soap();
        String restId = identifier + "-rest";
        String syntheticPassword = UUID.randomUUID().toString();
        legacy.setInstanceNfvoMappingRepository(identifier, "nfvo", "https://nfvo.example", "test-user",
                syntheticPassword, "root");
        Map<String, String> command = Map.of("nfvoName", "nfvo", "endpoint", "https://nfvo.example", "username",
                "test-user", "password", syntheticPassword, "apiRoot", "root");
        assertThat(authenticated.exchange("/v1/request-db/instance-nfvo-mappings/" + restId, HttpMethod.PUT,
                new HttpEntity<>(command), Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        InstanceNfvoMapping actual = authenticated.getForObject("/v1/request-db/instance-nfvo-mappings/" + restId,
                InstanceNfvoMapping.class);
        assertThat(actual).usingRecursiveComparison().ignoringFields("instanceId")
                .isEqualTo(legacy.getInstanceNfvoMapping(identifier));
        assertThat(authenticated.getForObject("/v1/request-db/sites/" + identifier + "/status", Map.class))
                .containsEntry("enabled", legacy.getSiteStatus(identifier));
    }

    @Test
    public void serviceAndResourceMutationsMatchSoap() throws Exception {
        MsoRequestsDbAdapter legacy = soap();
        String restId = identifier + "-rest";
        Map<String, String> service = Map.of("serviceId", restId, "operationId", "operation", "operationType", "CREATE",
                "userId", "user", "result", "processing", "operationContent", "content", "progress", "0", "reason", "");
        String servicePath = "/v1/request-db/service-operations?serviceId=" + restId + "&operationId=operation";
        legacy.initServiceOperationStatus(identifier, "operation", "CREATE", "user", "processing", "content", "0", "");
        assertThat(
                authenticated.postForEntity("/v1/request-db/service-operations", service, Void.class).getStatusCode())
                        .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(authenticated.getForObject(servicePath, OperationStatus.class)).usingRecursiveComparison()
                .ignoringFields("serviceId", "operateAt", "finishedAt")
                .isEqualTo(legacy.getServiceOperationStatus(identifier, "operation"));
        legacy.updateServiceOperationStatus(identifier, "operation", "CREATE", "user", "processing", "content", "0",
                "");
        assertThat(authenticated.exchange(servicePath, HttpMethod.PUT, new HttpEntity<>(service), Void.class)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        legacy.initResourceOperationStatus(identifier, "operation", "CREATE", "resource:");
        assertThat(authenticated.postForEntity("/v1/request-db/resource-operations",
                Map.of("serviceId", restId, "operationId", "operation", "operationType", "CREATE",
                        "resourceTemplateUUIDs", "resource:"),
                Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        String resourcePath = "/v1/request-db/resource-operations?serviceId=" + restId
                + "&operationId=operation&resourceTemplateUUID=resource";
        assertThat(authenticated.getForObject(resourcePath, ResourceOperationStatus.class)).usingRecursiveComparison()
                .ignoringFields("serviceId")
                .isEqualTo(legacy.getResourceOperationStatus(identifier, "operation", "resource"));
        legacy.updateResourceOperationStatus(identifier, "operation", "resource", "CREATE", "instance", "job",
                RequestsDbConstant.Status.ERROR, "50", "failed", "failure");
        Map<String, String> resource = Map.of("operType", "CREATE", "resourceInstanceID", "instance", "jobId", "job",
                "status", RequestsDbConstant.Status.ERROR, "progress", "50", "errorCode", "failed", "statusDescription",
                "failure");
        assertThat(authenticated.exchange(resourcePath, HttpMethod.PUT, new HttpEntity<>(resource), Void.class)
                .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(authenticated.getForObject(resourcePath, ResourceOperationStatus.class)).usingRecursiveComparison()
                .ignoringFields("serviceId")
                .isEqualTo(legacy.getResourceOperationStatus(identifier, "operation", "resource"));
        assertThat(authenticated.getForObject(servicePath, OperationStatus.class)).usingRecursiveComparison()
                .ignoringFields("serviceId", "operateAt", "finishedAt")
                .isEqualTo(legacy.getServiceOperationStatus(identifier, "operation"));
    }
}
