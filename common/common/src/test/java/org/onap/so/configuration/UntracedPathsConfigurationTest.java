/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom. All rights reserved.
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

package org.onap.so.configuration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;

public class UntracedPathsConfigurationTest {

    private final ObservationPredicate predicate =
            new UntracedPathsConfiguration().untracedPathsPredicate("/manage", true);

    @Test
    public void healthEndpointIsNotObserved() {
        assertFalse(observed("", "/manage/health"));
    }

    @Test
    public void healthGroupsAreNotObserved() {
        assertFalse(observed("", "/manage/health/liveness"));
        assertFalse(observed("", "/manage/health/readiness"));
    }

    @Test
    public void fetchAndLockIsNotObserved() {
        assertFalse(observed("", "/sobpmnengine/external-task/fetchAndLock"));
    }

    @Test
    public void completingAnExternalTaskIsObserved() {
        assertTrue(observed("", "/sobpmnengine/external-task/4711/complete"));
    }

    @Test
    public void otherActuatorEndpointsAreObserved() {
        assertTrue(observed("", "/manage/info"));
        assertTrue(observed("", "/manage"));
    }

    @Test
    public void apiRequestsAreObserved() {
        assertTrue(observed("", "/onap/so/infra/serviceInstantiation/v7/serviceInstances"));
    }

    @Test
    public void pathIsMatchedWithinTheContextPath() {
        assertFalse(observed("/so", "/so/manage/health"));
        assertTrue(observed("/so", "/so/onap/so/infra/orchestrationRequests/v7"));
    }

    @Test
    public void actuatorBasePathIsTakenFromTheConfiguration() {
        ObservationPredicate defaultBasePath =
                new UntracedPathsConfiguration().untracedPathsPredicate("/actuator", true);
        assertFalse(defaultBasePath.test("http.server.requests", serverContext("", "/actuator/health")));
        assertTrue(defaultBasePath.test("http.server.requests", serverContext("", "/manage/health")));
    }

    @Test
    public void everythingIsObservedWhenDisabled() {
        ObservationPredicate disabled = new UntracedPathsConfiguration().untracedPathsPredicate("/manage", false);
        assertTrue(disabled.test("http.server.requests", serverContext("", "/manage/health")));
    }

    @Test
    public void nonServerObservationsAreObserved() {
        assertTrue(predicate.test("http.client.requests", new Observation.Context()));
    }

    private boolean observed(String contextPath, String requestUri) {
        return predicate.test("http.server.requests", serverContext(contextPath, requestUri));
    }

    private static ServerRequestObservationContext serverContext(String contextPath, String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
        request.setContextPath(contextPath);
        return new ServerRequestObservationContext(request, new MockHttpServletResponse());
    }
}
