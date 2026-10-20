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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

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

    private static class Fixture extends BaseBPMNTest {
    }
}
