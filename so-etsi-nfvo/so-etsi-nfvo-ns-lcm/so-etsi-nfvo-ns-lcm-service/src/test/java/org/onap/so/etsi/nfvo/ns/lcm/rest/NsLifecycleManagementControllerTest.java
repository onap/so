/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.etsi.nfvo.ns.lcm.Constants;
import org.onap.so.etsi.nfvo.ns.lcm.JSON;
import org.onap.so.etsi.nfvo.ns.lcm.TestApplication;
import org.onap.so.etsi.nfvo.ns.lcm.model.CreateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.InstantiateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.TerminateNsRequest;
import org.springframework.boot.test.context.SpringBootTest;
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
    @LocalServerPort
    private int port;

    private TestRestTemplate testRestTemplate;

    @Before
    public void setUp() {
        final Gson gson = JSON.createGson().create();
        testRestTemplate = new TestRestTemplate(
                new RestTemplateBuilder().additionalMessageConverters(new GsonHttpMessageConverter(gson)));
    }

    @Test
    public void testCreateNs_ValidCreateNsRequest() {
        final String baseUrl = getNsLcmBaseUrl() + "/ns_instances";
        final HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.HTTP_GLOBAL_CUSTOMER_ID_HTTP_HEADER_PARM_NAME, "GLOBAL_CUSTOMER_ID");
        final HttpEntity<?> request = new HttpEntity<>(getCreateNsRequest(), headers);
        final ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, request, Void.class);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
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
        final String baseUrl = getNsLcmBaseUrl() + "/ns_instances/" + UUID.randomUUID().toString() + "/instantiate";
        final HttpEntity<?> request = new HttpEntity<>(getInstantiateNsRequest());
        final ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.POST, request, Void.class);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
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
        return new CreateNsRequest().nsdId(UUID.randomUUID().toString());
    }

    private String getNsLcmBaseUrl() {
        return "http://localhost:" + port + Constants.NS_LIFE_CYCLE_MANAGEMENT_BASE_URL;
    }
}

