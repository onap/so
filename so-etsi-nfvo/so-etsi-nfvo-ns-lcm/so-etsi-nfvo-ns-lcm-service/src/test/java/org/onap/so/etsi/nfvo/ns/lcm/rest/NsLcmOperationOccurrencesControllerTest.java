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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.etsi.nfvo.ns.lcm.Constants;
import org.onap.so.etsi.nfvo.ns.lcm.TestApplication;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.GsonProvider;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NsLcmOpOcc;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NsLcmOpType;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.OperationStateEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.model.InlineResponse400;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsLcmOpOccsNsLcmOpOcc;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NsLcmOperationOccurrencesControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseServiceProvider databaseServiceProvider;

    @Autowired
    private GsonProvider gsonProvider;

    private TestRestTemplate testRestTemplate;

    @Before
    public void setUp() {
        final Gson gson = gsonProvider.getGson();
        testRestTemplate = new TestRestTemplate(
                new RestTemplateBuilder().additionalMessageConverters(new GsonHttpMessageConverter(gson)));
    }

    @Test
    public void testGetOperationStatus_validNsLcmOpOccId_returnsNsLcmOpOcc() {
        final String nsLcmOpOccId = addDummyNsLcmOpOccToDatabase();
        final String baseUrl = getNsLcmBaseUrl() + "/ns_lcm_op_occs/" + nsLcmOpOccId;
        final HttpEntity<?> request = new HttpEntity<>(new HttpHeaders());
        final ResponseEntity<NsLcmOpOccsNsLcmOpOcc> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.GET, request, NsLcmOpOccsNsLcmOpOcc.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testGetOperationStatus_nsLcmOpOccIdNotFound_returnsInlineResponse400() {
        final String nsLcmOpOccId = UUID.randomUUID().toString();
        final Optional<NsLcmOpOcc> optionalNsLcmOpOcc = databaseServiceProvider.getNsLcmOpOcc(nsLcmOpOccId);
        assertTrue(optionalNsLcmOpOcc.isEmpty());
        final String baseUrl = getNsLcmBaseUrl() + "/ns_lcm_op_occs/" + nsLcmOpOccId;
        final HttpEntity<?> request = new HttpEntity<>(new HttpHeaders());
        final ResponseEntity<InlineResponse400> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.GET, request, InlineResponse400.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertTrue(responseEntity.hasBody());
        assertNotNull(responseEntity.getBody());
    }

    private String addDummyNsLcmOpOccToDatabase() {
        final LocalDateTime currentDateTime = LocalDateTime.now();

        final NfvoNsInst nsInst = new NfvoNsInst().name("name").nsdId("id").status(State.NOT_INSTANTIATED)
                .nsdInvariantId("id").statusUpdatedTime(currentDateTime);
        databaseServiceProvider.saveNfvoNsInst(nsInst);

        final NsLcmOpOcc nsLcmOpOcc = new NsLcmOpOcc().nfvoNsInst(nsInst).operationState(OperationStateEnum.PROCESSING)
                .isCancelPending(false).isAutoInvocation(false).operation(NsLcmOpType.INSTANTIATE)
                .startTime(currentDateTime).stateEnteredTime(currentDateTime).operationParams("");
        databaseServiceProvider.addNSLcmOpOcc(nsLcmOpOcc);

        return nsLcmOpOcc.getId();
    }

    private String getNsLcmBaseUrl() {
        return "http://localhost:" + port + Constants.NS_LIFE_CYCLE_MANAGEMENT_BASE_URL;
    }
}

