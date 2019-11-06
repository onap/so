package org.onap.so.adapters.vnfmadapter.rest;

import static org.junit.Assert.assertEquals;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.InlineResponse2002;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.packagemanagement.model.PkgmSubscriptionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class Sol003SubscriptionManagementControllerTest {

    private static final String subscriptionId = "mySubscriptionId";

    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate testRestTemplate;
    private MockRestServiceServer mockRestServer;

    @Autowired
    private Sol003SubscriptionManagementController controller;

    @Before
    public void setUp() throws Exception {
        mockRestServer = MockRestServiceServer.bindTo(testRestTemplate).build();
    }

    @Test
    public void postSubscriptionRequest() throws URISyntaxException, InterruptedException {
        final PkgmSubscriptionRequest pkgmSubscriptionRequest = new PkgmSubscriptionRequest();
        final ResponseEntity<InlineResponse2002> response = controller.postSubscriptionRequest(pkgmSubscriptionRequest);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void getSubscriptions() throws URISyntaxException, InterruptedException {
        final ResponseEntity<List<InlineResponse2002>> response = controller.getSubscriptions();
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void deleteSubscription() throws URISyntaxException, InterruptedException {
        final ResponseEntity<Void> response = controller.deleteSubscription(subscriptionId);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void getSubscription() throws URISyntaxException, InterruptedException {
        final ResponseEntity<InlineResponse2002> response = controller.getSubscription(subscriptionId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
