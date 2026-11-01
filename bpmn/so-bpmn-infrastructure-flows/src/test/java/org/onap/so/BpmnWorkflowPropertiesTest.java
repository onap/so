/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
package org.onap.so;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.contract.wiremock.WireMockConfiguration;
import org.springframework.cloud.contract.wiremock.WireMockTestExecutionListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

public class BpmnWorkflowPropertiesTest {
    private static final String KEY = "mso.workflow.fixture.property";
    private final StandardEnvironment environment = new StandardEnvironment();
    private final Fixture fixture = new Fixture();
    private final WireMockServer server = mock(WireMockServer.class);

    @Before
    public void configureFixture() {
        ReflectionTestUtils.setField(fixture, "environment", environment);
        fixture.wireMockServer = server;
    }

    @Test
    public void cleanupRestoresTheOriginalPropertyWithinTheSameEnvironment() {
        environment.getPropertySources()
                .addFirst(new MapPropertySource("original", Collections.singletonMap(KEY, "original")));
        int originalSources = environment.getPropertySources().size();
        fixture.setWorkflowProperties(Collections.singletonMap(KEY, "override"));
        assertEquals("override", environment.getProperty(KEY));
        assertEquals(originalSources + 1, environment.getPropertySources().size());
        fixture.shutDown();
        assertEquals("original", environment.getProperty(KEY));
        assertEquals(originalSources, environment.getPropertySources().size());
        verify(server).resetAll();
    }

    @Test
    public void repeatedOverridesReplaceOnlyTheFixturesOwnSourceAndCopyTheInputMap() {
        int originalSources = environment.getPropertySources().size();
        Map<String, Object> properties = new HashMap<>();
        properties.put(KEY, "first");
        fixture.setWorkflowProperties(properties);
        properties.put(KEY, "mutated after installation");
        assertEquals("first", environment.getProperty(KEY));
        fixture.setWorkflowProperties(Collections.singletonMap(KEY, "second"));
        assertEquals("second", environment.getProperty(KEY));
        assertEquals(originalSources + 1, environment.getPropertySources().size());
        fixture.shutDown();
        assertNull(environment.getProperty(KEY));
        assertEquals(originalSources, environment.getPropertySources().size());
    }

    @Test
    public void cleanupIsIdempotentWhenNoOverrideWasInstalled() {
        int originalSources = environment.getPropertySources().size();
        fixture.shutDown();
        fixture.shutDown();
        assertEquals(originalSources, environment.getPropertySources().size());
        verify(server, times(2)).resetAll();
    }

    @Test
    public void classCleanupDoesNotRestartTheContextOwnedWireMockServer() {
        try (AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, "wiremock.server.port=0");
            applicationContext.registerBean(WireMockConfiguration.class.getName(), WireMockConfiguration.class);
            applicationContext.refresh();
            WireMockServer liveServer = applicationContext.getBean(WireMockServer.class);
            liveServer.stubFor(get(urlEqualTo("/connection-lifecycle")).willReturn(aResponse().withStatus(204)));
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(1000);
            factory.setReadTimeout(1000);
            RestTemplate client = new RestTemplate(factory);
            String url = "http://localhost:" + liveServer.port() + "/connection-lifecycle";
            assertEquals(HttpStatus.NO_CONTENT, client.getForEntity(url, Void.class).getStatusCode());
            TestContext testContext = mock(TestContext.class);
            when(testContext.getApplicationContext()).thenReturn(applicationContext);
            doReturn(Fixture.class).when(testContext).getTestClass();

            new WireMockTestExecutionListener().afterTestClass(testContext);

            liveServer.stubFor(put(urlEqualTo("/connection-lifecycle")).willReturn(aResponse().withStatus(204)));
            assertEquals(HttpStatus.NO_CONTENT,
                    client.exchange(url, HttpMethod.PUT, new HttpEntity<>("status"), Void.class).getStatusCode());
            liveServer.verify(1, putRequestedFor(urlEqualTo("/connection-lifecycle")));
        }
    }

    private static class Fixture extends BaseBPMNTest {
    }
}
