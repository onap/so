package org.onap.so.db.request.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class RequestDbOperationsClientTest {

    private MockRestServiceServer server;
    private RequestDbOperationsClient client;

    @Before
    public void setup() {
        RestTemplate template = new RestTemplate();
        server = MockRestServiceServer.bindTo(template).build();
        client = new RequestDbOperationsClient(template, "http://localhost/", "Basic test");
    }

    @After
    public void verifyRequests() {
        server.verify();
    }

    private ResponseActions expect(HttpMethod verb, String path) {
        return server.expect(requestTo("http://localhost/v1/request-db" + path)).andExpect(method(verb))
                .andExpect(header("Authorization", "Basic test"))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void infraUpdateOmitsNullFieldsButPreservesEmptyStrings() {
        RequestDbCommands.InfraRequestUpdate command = new RequestDbCommands.InfraRequestUpdate();
        command.setLastModifiedBy("REST");
        command.setStatusMessage("");
        expect(HttpMethod.POST, "/infra-requests/request/updates")
                .andExpect(content().json("{\"lastModifiedBy\":\"REST\",\"statusMessage\":\"\"}", true))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        client.updateInfraRequest("request", command);
    }

    @Test
    public void infraLookupEncodesReservedCharactersOnce() {
        expect(HttpMethod.GET, "/infra-requests/request%2F%25%20%2B")
                .andRespond(withSuccess("{\"requestId\":\"request/% +\"}", MediaType.APPLICATION_JSON));

        assertThat(client.getInfraRequest("request/% +").getRequestId()).isEqualTo("request/% +");
    }

    @Test
    public void missingInfraLookupPropagatesNotFound() {
        expect(HttpMethod.GET, "/infra-requests/missing").andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getInfraRequest("missing")).isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    public void nfvoMappingSupportsWriteAndRead() {
        RequestDbCommands.InstanceNfvoMappingUpdate command = new RequestDbCommands.InstanceNfvoMappingUpdate();
        command.setNfvoName("nfvo");
        expect(HttpMethod.PUT, "/instance-nfvo-mappings/instance")
                .andExpect(content().json("{\"nfvoName\":\"nfvo\"}", true))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));
        expect(HttpMethod.GET, "/instance-nfvo-mappings/instance")
                .andRespond(withSuccess("{\"nfvoName\":\"nfvo\"}", MediaType.APPLICATION_JSON));

        client.setInstanceNfvoMappingRepository("instance", command);
        assertThat(client.getInstanceNfvoMapping("instance").getNfvoName()).isEqualTo("nfvo");
    }

    @Test
    public void missingNfvoMappingReturnsNull() {
        expect(HttpMethod.GET, "/instance-nfvo-mappings/missing").andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.getInstanceNfvoMapping("missing")).isNull();
    }

    @Test
    public void siteStatusReturnsEnabledFlag() {
        expect(HttpMethod.GET, "/sites/site/status")
                .andRespond(withSuccess("{\"enabled\":false}", MediaType.APPLICATION_JSON));

        assertThat(client.getSiteStatus("site")).isFalse();
    }

    @Test
    public void emptyOperationSelectsServiceAndEncodesQueryValues() {
        expect(HttpMethod.GET, "/service-operations?serviceId=service%26%2B%25&operationId=")
                .andRespond(withSuccess("{\"operationId\":\"operation\"}", MediaType.APPLICATION_JSON));

        assertThat(client.getServiceOperationStatus("service&+%", null).getOperationId()).isEqualTo("operation");
    }

    @Test
    public void missingServiceOperationReturnsNull() {
        expect(HttpMethod.GET, "/service-operations?serviceId=service&operationId=missing")
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.getServiceOperationStatus("service", "missing")).isNull();
    }

    @Test
    public void serviceOperationSupportsInitializationAndUpdate() {
        RequestDbCommands.ServiceOperationWrite command = new RequestDbCommands.ServiceOperationWrite();
        command.setServiceId("service");
        command.setOperationId("operation");
        command.setProgress("25");
        expect(HttpMethod.POST, "/service-operations")
                .andExpect(content()
                        .json("{\"serviceId\":\"service\",\"operationId\":\"operation\",\"progress\":\"25\"}", true))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));
        expect(HttpMethod.PUT, "/service-operations?serviceId=service&operationId=operation")
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        client.initServiceOperationStatus(command);
        client.updateServiceOperationStatus(command);
    }

    @Test
    public void resourceOperationSupportsInitializationLookupAndUpdate() {
        RequestDbCommands.ResourceOperationInit initial = new RequestDbCommands.ResourceOperationInit();
        initial.setServiceId("service");
        initial.setOperationId("operation");
        initial.setResourceTemplateUUIDs("resource:");
        RequestDbCommands.ResourceOperationUpdate update = new RequestDbCommands.ResourceOperationUpdate();
        update.setProgress("50");
        String path = "/resource-operations?serviceId=service&operationId=operation&resourceTemplateUUID=resource";
        expect(HttpMethod.POST, "/resource-operations").andRespond(withStatus(HttpStatus.NO_CONTENT));
        expect(HttpMethod.GET, path).andRespond(withSuccess("{\"progress\":\"0\"}", MediaType.APPLICATION_JSON));
        expect(HttpMethod.PUT, path).andExpect(content().json("{\"progress\":\"50\"}", true))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        client.initResourceOperationStatus(initial);
        assertThat(client.getResourceOperationStatus("service", "operation", "resource").getProgress()).isEqualTo("0");
        client.updateResourceOperationStatus("service", "operation", "resource", update);
    }

    @Test
    public void failedWriteIsNotRetried() {
        expect(HttpMethod.POST, "/infra-requests/request/updates")
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> client.updateInfraRequest("request", new RequestDbCommands.InfraRequestUpdate()))
                .isInstanceOf(HttpServerErrorException.class);
    }

    @Test
    public void unauthorizedLookupIsNotTreatedAsMissing() {
        expect(HttpMethod.GET, "/instance-nfvo-mappings/instance").andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.getInstanceNfvoMapping("instance"))
                .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }
}
