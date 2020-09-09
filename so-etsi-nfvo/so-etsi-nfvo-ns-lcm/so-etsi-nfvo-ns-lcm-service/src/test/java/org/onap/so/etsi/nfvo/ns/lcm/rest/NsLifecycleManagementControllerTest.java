/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.rest;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.etsi.nfvo.ns.lcm.Constants;
import org.onap.so.etsi.nfvo.ns.lcm.TestApplication;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.GsonProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.exceptions.NsRequestProcessingException;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.service.JobExecutorService;
import org.onap.so.etsi.nfvo.ns.lcm.model.CreateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.InlineResponse400;
import org.onap.so.etsi.nfvo.ns.lcm.model.InstantiateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesNsInstance;
import org.onap.so.etsi.nfvo.ns.lcm.model.TerminateNsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.google.gson.Gson;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NsLifecycleManagementControllerTest {
    private static final String EXPECTED_BASE_URL =
            "http://so-etsi-nfvo-ns-lcm.onap:9095/so/so-etsi-nfvo-ns-lcm/v1/api/nslcm/v1";
    private static final String RANDOM_NS_LCM_OP_OCC_ID = UUID.randomUUID().toString();
    private static final String RANDOM_NS_INST_ID = UUID.randomUUID().toString();
    private static final String SERVICE_TYPE = "NetworkService";
    private static final String GLOBAL_CUSTOMER_ID = UUID.randomUUID().toString();
    private static final String EXPECTED_CREATE_REQ_LOCATION_URL =
            EXPECTED_BASE_URL + "/ns_instances/" + RANDOM_NS_INST_ID;
    private static final String EXPECTED_INSTANTIATE_REQ_LOCATION_URL =
            EXPECTED_BASE_URL + "/ns_lcm_op_occs/" + RANDOM_NS_LCM_OP_OCC_ID;

    @LocalServerPort
    private int port;

    private TestRestTemplate testRestTemplate;

    @Autowired
    private GsonProvider gsonProvider;

    @MockBean
    private JobExecutorService mockedJobExecutorService;

    @Before
    public void setUp() {
        final Gson gson = gsonProvider.getGson();
        testRestTemplate = new TestRestTemplate(
                new RestTemplateBuilder().additionalMessageConverters(new GsonHttpMessageConverter(gson)));
    }

    @Test
    public void testCreateNs_ValidCreateNsRequest() throws URISyntaxException {

        final CreateNsRequest createNsRequest = getCreateNsRequest();

        when(mockedJobExecutorService.runCreateNsJob(eq(createNsRequest), eq(GLOBAL_CUSTOMER_ID), eq(SERVICE_TYPE)))
                .thenReturn(new NsInstancesNsInstance().id(RANDOM_NS_INST_ID));

        final String baseUrl = getNsLcmBaseUrl() + "/ns_instances";
        final HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.HTTP_GLOBAL_CUSTOMER_ID_HTTP_HEADER_PARM_NAME, GLOBAL_CUSTOMER_ID);
        final HttpEntity<?> request = new HttpEntity<>(createNsRequest, headers);
        final ResponseEntity<NsInstancesNsInstance> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, request, NsInstancesNsInstance.class);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());

        final HttpHeaders httpHeaders = responseEntity.getHeaders();
        assertTrue(httpHeaders.containsKey(HttpHeaders.LOCATION));
        final List<String> actual = httpHeaders.get(HttpHeaders.LOCATION);
        assertEquals(1, actual.size());
        assertEquals(EXPECTED_CREATE_REQ_LOCATION_URL, actual.get(0));
    }

    @Test
    public void testCreateNs_createNsRequest_nsRequestProcessingExceptionThrown_returnInlineResponse400()
            throws URISyntaxException {

        final CreateNsRequest createNsRequest = getCreateNsRequest();

        final String message = "Unable to process request";
        when(mockedJobExecutorService.runCreateNsJob(eq(createNsRequest), eq(GLOBAL_CUSTOMER_ID), eq(SERVICE_TYPE)))
                .thenThrow(new NsRequestProcessingException(message, new InlineResponse400().detail(message)));

        final String baseUrl = getNsLcmBaseUrl() + "/ns_instances";
        final HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.HTTP_GLOBAL_CUSTOMER_ID_HTTP_HEADER_PARM_NAME, GLOBAL_CUSTOMER_ID);
        final HttpEntity<?> request = new HttpEntity<>(createNsRequest, headers);
        final ResponseEntity<InlineResponse400> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, request, InlineResponse400.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());

        final InlineResponse400 body = responseEntity.getBody();
        assertEquals(message, body.getDetail());

    }

    @Test
    public void testCreateNs_createNsRequest_exceptionThrown_returnInlineResponse400() throws URISyntaxException {

        final CreateNsRequest createNsRequest = getCreateNsRequest();

        final String message = "Unable to process request";
        when(mockedJobExecutorService.runCreateNsJob(eq(createNsRequest), eq(GLOBAL_CUSTOMER_ID), eq(SERVICE_TYPE)))
                .thenThrow(new RuntimeException(message));

        final String baseUrl = getNsLcmBaseUrl() + "/ns_instances";
        final HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.HTTP_GLOBAL_CUSTOMER_ID_HTTP_HEADER_PARM_NAME, GLOBAL_CUSTOMER_ID);
        final HttpEntity<?> request = new HttpEntity<>(createNsRequest, headers);
        final ResponseEntity<InlineResponse400> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, request, InlineResponse400.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());

        final InlineResponse400 body = responseEntity.getBody();
        assertEquals(message, body.getDetail());

    }

    @Test
    public void testCreateNs_ValidDeleteNsRequest() {
        final String baseUrl = getNsLcmBaseUrl() + "/ns_instances/" + UUID.randomUUID().toString();
        final ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    @Test
    public void testInstantiateNs_ValidInstantiateNsRequest() {

        final InstantiateNsRequest instantiateNsRequest = getInstantiateNsRequest();
        when(mockedJobExecutorService.runInstantiateNsJob(eq(RANDOM_NS_INST_ID), eq(instantiateNsRequest)))
                .thenReturn(RANDOM_NS_LCM_OP_OCC_ID);

        final String baseUrl = getNsLcmBaseUrl() + "/ns_instances/" + RANDOM_NS_INST_ID + "/instantiate";
        final HttpEntity<?> request = new HttpEntity<>(instantiateNsRequest);
        final ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, request, Void.class);
        assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode());

        final HttpHeaders httpHeaders = responseEntity.getHeaders();
        assertTrue(httpHeaders.containsKey(HttpHeaders.LOCATION));
        final List<String> actual = httpHeaders.get(HttpHeaders.LOCATION);
        assertEquals(1, actual.size());
        assertEquals(EXPECTED_INSTANTIATE_REQ_LOCATION_URL, actual.get(0));
    }

    @Test
    public void testInstantiateNs_instantiateNsRequest_nsRequestProcessingExceptionThrown_returnInlineResponse400() {
        final String message = "Unable to process request";
        final InstantiateNsRequest instantiateNsRequest = getInstantiateNsRequest();
        when(mockedJobExecutorService.runInstantiateNsJob(eq(RANDOM_NS_INST_ID), eq(instantiateNsRequest)))
                .thenThrow(new NsRequestProcessingException(message, new InlineResponse400().detail(message)));

        final String baseUrl = getNsLcmBaseUrl() + "/ns_instances/" + RANDOM_NS_INST_ID + "/instantiate";
        final HttpEntity<?> request = new HttpEntity<>(instantiateNsRequest);
        final ResponseEntity<InlineResponse400> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, request, InlineResponse400.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testTerminateNs_ValidInstantiateNsRequest() {
        final String baseUrl = getNsLcmBaseUrl() + "/ns_instances/" + UUID.randomUUID().toString() + "/terminate";
        final HttpEntity<?> request = new HttpEntity<>(getTerminateNsRequest());
        final ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, request, Void.class);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }


    private TerminateNsRequest getTerminateNsRequest() {
        return new TerminateNsRequest().terminationTime(LocalDateTime.now());
    }

    private InstantiateNsRequest getInstantiateNsRequest() {
        return new InstantiateNsRequest().nsFlavourId("FLAVOUR_ID");
    }

    private CreateNsRequest getCreateNsRequest() {
        return new CreateNsRequest().nsdId(RANDOM_NS_INST_ID);
    }

    private String getNsLcmBaseUrl() {
        return "http://localhost:" + port + Constants.NS_LIFE_CYCLE_MANAGEMENT_BASE_URL;
    }
}

