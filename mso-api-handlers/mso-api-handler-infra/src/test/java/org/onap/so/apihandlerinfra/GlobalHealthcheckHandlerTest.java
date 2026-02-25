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

package org.onap.so.apihandlerinfra;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.onap.so.apihandlerinfra.HealthCheckConfig.Endpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {GenericStringConverter.class, HealthCheckConverter.class},
        initializers = {ConfigDataApplicationContextInitializer.class})
@ActiveProfiles("test")
@EnableConfigurationProperties({HealthCheckConfig.class})
public class GlobalHealthcheckHandlerTest {
    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ContainerRequestContext requestContext;

    @SpyBean
    private GlobalHealthcheckHandler globalhealth;

    @Test
    public void testQuerySubsystemHealthNullResult() {
        ReflectionTestUtils.setField(globalhealth, "actuatorContextPath", "/manage");

        Mockito.when(restTemplate.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Object>>any())).thenReturn(null);

        HealthCheckSubsystem result = globalhealth
                .querySubsystemHealth(new Endpoint(SoSubsystems.BPMN, UriBuilder.fromPath("http://localhost").build()));
        assertEquals(HealthCheckStatus.DOWN, result.getStatus());
    }

    @Test
    public void testQuerySubsystemHealthNotNullResult() {
        ReflectionTestUtils.setField(globalhealth, "actuatorContextPath", "/manage");

        SubsystemHealthcheckResponse subSystemResponse = new SubsystemHealthcheckResponse();
        subSystemResponse.setStatus("UP");
        ResponseEntity<Object> r = new ResponseEntity<>(subSystemResponse, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Object>>any())).thenReturn(r);

        HealthCheckSubsystem result = globalhealth
                .querySubsystemHealth(new Endpoint(SoSubsystems.ASDC, UriBuilder.fromPath("http://localhost").build()));
        assertEquals(HealthCheckStatus.UP, result.getStatus());
    }

    private Response globalHealthcheck(String status) {
        ReflectionTestUtils.setField(globalhealth, "actuatorContextPath", "/manage");

        SubsystemHealthcheckResponse subSystemResponse = new SubsystemHealthcheckResponse();

        subSystemResponse.setStatus(status);
        ResponseEntity<Object> r = new ResponseEntity<>(subSystemResponse, HttpStatus.OK);
        Mockito.when(restTemplate.exchange(ArgumentMatchers.any(URI.class), ArgumentMatchers.any(HttpMethod.class),
                ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<Object>>any())).thenReturn(r);

        Mockito.when(requestContext.getProperty(anyString())).thenReturn("1234567890");
        Response response = globalhealth.globalHealthcheck(true, requestContext);
        return response;
    }

    @Test
    public void globalHealthcheckAllUPTest() throws JSONException {

        HealthCheckResponse expected = new HealthCheckResponse();

        for (Subsystem system : SoSubsystems.values()) {
            expected.getSubsystems().add(new HealthCheckSubsystem(system,
                    UriBuilder.fromUri("http://localhost").build(), HealthCheckStatus.UP));
        }
        expected.setMessage("HttpStatus: 200");
        Response response = globalHealthcheck("UP");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        HealthCheckResponse root;
        root = (HealthCheckResponse) response.getEntity();
        assertThat(root, sameBeanAs(expected).ignoring("subsystems.uri").ignoring("subsystems.subsystem"));

    }

    @Test
    public void globalHealthcheckAllDOWNTest() throws JSONException {
        HealthCheckResponse expected = new HealthCheckResponse();

        for (Subsystem system : SoSubsystems.values()) {
            expected.getSubsystems().add(new HealthCheckSubsystem(system,
                    UriBuilder.fromUri("http://localhost").build(), HealthCheckStatus.DOWN));
        }
        expected.setMessage("HttpStatus: 200");
        Response response = globalHealthcheck("DOWN");
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        HealthCheckResponse root;
        root = (HealthCheckResponse) response.getEntity();
        assertThat(root, sameBeanAs(expected).ignoring("subsystems.uri").ignoring("subsystems.subsystem"));
    }

    @Test
    public void buildHttpEntityForRequestTest() {
        HttpEntity<String> he = globalhealth.buildHttpEntityForRequest();
        assertEquals(MediaType.APPLICATION_JSON, he.getHeaders().getAccept().get(0));
        assertEquals(MediaType.APPLICATION_JSON, he.getHeaders().getContentType());
    }


    @Test
    public void processResponseFromSubsystemTest() {
        SubsystemHealthcheckResponse subSystemResponse = new SubsystemHealthcheckResponse();
        subSystemResponse.setStatus("UP");
        ResponseEntity<SubsystemHealthcheckResponse> r = new ResponseEntity<>(subSystemResponse, HttpStatus.OK);
        Endpoint endpoint = new Endpoint(SoSubsystems.BPMN, UriBuilder.fromUri("http://localhost").build());
        HealthCheckStatus result = globalhealth.processResponseFromSubsystem(r, endpoint);
        assertEquals(HealthCheckStatus.UP, result);
    }

}
