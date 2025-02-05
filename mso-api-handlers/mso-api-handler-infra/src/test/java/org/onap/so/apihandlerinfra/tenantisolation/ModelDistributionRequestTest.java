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

package org.onap.so.apihandlerinfra.tenantisolation;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import java.io.IOException;
import jakarta.inject.Provider;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.so.apihandler.common.ErrorNumbers;
import org.onap.so.apihandlerinfra.BaseTest;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Action;
import org.onap.so.apihandlerinfra.tenantisolationbeans.Distribution;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelDistributionRequestTest extends BaseTest {

    private static final String requestJSON =
            "{\"status\": \"DISTRIBUTION_COMPLETE_ERROR\", \"errorReason\": \"Distribution failed in AAI\" }";

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private Provider<TenantIsolationRunnable> thread;
    @InjectMocks
    @Spy
    private ModelDistributionRequest request = new ModelDistributionRequest();
    @Mock
    private TenantIsolationRunnable runnable = new TenantIsolationRunnable();

    @Before
    public void beforeTest() {
        Mockito.when(thread.get()).thenReturn(runnable);
    }

    @Test
    public void testObjectMapperError() throws ApiException {
        thrown.expect(ValidateException.class);
        thrown.expectMessage(startsWith("Mapping of request to JSON object failed"));
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_BAD_REQUEST)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));
        request.updateModelDistributionStatus("", null, null);
    }

    @Test
    public void testParseError1() throws ApiException {
        thrown.expect(ValidateException.class);
        thrown.expectMessage(startsWith("No valid status is specified"));
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_BAD_REQUEST)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));
        String requestErrorJSON = "{\"errorReason\": \"Distribution failed in AAI\" }";
        request.updateModelDistributionStatus(requestErrorJSON, null, null);
    }

    @Test
    public void testParseError2() throws ApiException {
        thrown.expect(ValidateException.class);
        thrown.expectMessage(startsWith("No valid errorReason is specified"));
        thrown.expect(hasProperty("httpResponseCode", is(HttpStatus.SC_BAD_REQUEST)));
        thrown.expect(hasProperty("messageID", is(ErrorNumbers.SVC_BAD_PARAMETER)));
        String requestErrorJSON = "{\"status\": \"DISTRIBUTION_COMPLETE_ERROR\"}";
        request.updateModelDistributionStatus(requestErrorJSON, null, null);
    }

    @Test
    public void testSuccess() throws ApiException {
        doNothing().when(runnable).run(any(Action.class), anyString(), any(CloudOrchestrationRequest.class),
                anyString());

        Response response = request.updateModelDistributionStatus(requestJSON, null, null);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testSuccess_PATCH() throws ApiException, IOException {
        String path = "/onap/so/infra/modelDistributions/v1/distributions/ff3514e3-5a33-55df-13ab-12abad84e7fa";
        ObjectMapper mapper = new ObjectMapper();
        Distribution distRequest = mapper.readValue(requestJSON, Distribution.class);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON);
        headers.set("Content-Type", MediaType.APPLICATION_JSON);
        HttpEntity<Distribution> entity = new HttpEntity<Distribution>(distRequest, headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(path));
        ResponseEntity<String> response =
                restTemplate.exchange(builder.toUriString(), HttpMethod.PATCH, entity, String.class);
        assertEquals(200, response.getStatusCodeValue());

    }

}
