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
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.etsi.nfvo.ns.lcm.Constants;
import org.onap.so.etsi.nfvo.ns.lcm.JSON;
import org.onap.so.etsi.nfvo.ns.lcm.TestApplication;
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
 * 
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NsLcmOperationOccurrencesControllerTest {
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
    public void testGetOperationStatusS_ValidNsLcmOpOccId() {
        final String baseUrl = getNsLcmBaseUrl() + "/ns_lcm_op_occs/" + UUID.randomUUID().toString();
        final HttpEntity<?> request = new HttpEntity<>(new HttpHeaders());
        final ResponseEntity<Void> responseEntity =
                testRestTemplate.exchange(baseUrl, HttpMethod.GET, request, Void.class);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, responseEntity.getStatusCode());
    }

    private String getNsLcmBaseUrl() {
        return "http://localhost:" + port + Constants.NS_LIFE_CYCLE_MANAGEMENT_BASE_URL;
    }
}

