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

package org.onap.so.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MultivaluedMap;
import org.javatuples.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.logging.filter.base.ONAPComponentsList;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.transport.SenderContext;

public class RestClientTracingTest {

    private static final String TRACEPARENT = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    private final RecordingHandler handler = new RecordingHandler();

    @Before
    public void registerHandler() {
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        ClientObservationRegistry.setRegistry(registry);
    }

    @After
    public void resetRegistry() {
        ClientObservationRegistry.setRegistry(ObservationRegistry.NOOP);
    }

    @Test
    public void requestCarriesTheTraceContext() throws Exception {
        wireMockRule.stubFor(get("/traced").willReturn(aResponse().withStatus(200).withBody("{}")));

        client(wireMockUrl(), "/traced").get();

        wireMockRule.verify(getRequestedFor(urlEqualTo("/traced")).withHeader("traceparent", equalTo(TRACEPARENT)));
        Observation.Context context = handler.stoppedFor("/traced");
        assertEquals("http get", context.getContextualName());
        assertEquals("200", lowCardinalityValue(context, "status"));
        assertEquals(ONAPComponents.BPMN.toString(), lowCardinalityValue(context, "client.name"));
        assertNull(context.getError());
    }

    @Test
    public void errorResponseIsRecorded() throws Exception {
        wireMockRule.stubFor(get("/missing").willReturn(aResponse().withStatus(404)));

        assertThrows(NotFoundException.class, () -> client(wireMockUrl(), "/missing").get());

        Observation.Context context = handler.stoppedFor("/missing");
        assertEquals("404", lowCardinalityValue(context, "status"));
        assertNotNull(context.getError());
    }

    @Test
    public void observationIsStoppedWhenTheServiceIsUnreachable() throws Exception {
        URL unreachable;
        try (ServerSocket socket = new ServerSocket(0)) {
            unreachable = new URL("http://localhost:" + socket.getLocalPort());
        }

        assertThrows(ProcessingException.class, () -> client(unreachable, "/unreachable").get());

        Observation.Context context = handler.stoppedFor("/unreachable");
        assertEquals("CLIENT_ERROR", lowCardinalityValue(context, "status"));
        assertNotNull(context.getError());
    }

    private URL wireMockUrl() throws MalformedURLException {
        return new URL("http://localhost:" + wireMockRule.port());
    }

    private static String lowCardinalityValue(Observation.Context context, String key) {
        KeyValue keyValue = context.getLowCardinalityKeyValue(key);
        return keyValue == null ? null : keyValue.getValue();
    }

    private static RestClient client(URL endpoint, String path) {
        RestProperties props = new RestProperties() {
            @Override
            public URL getEndpoint() {
                return endpoint;
            }

            @Override
            public String getSystemName() {
                return "MSO";
            }

            @Override
            public Integer getRetries() {
                return 0;
            }
        };
        return new RestClient(props, Optional.of(URI.create(path))) {
            @Override
            protected void initializeHeaderMap(MultivaluedMap<String, Pair<String, String>> headerMap) {
                // no static headers needed
            }

            @Override
            protected ONAPComponentsList getTargetEntity() {
                return ONAPComponents.BPMN;
            }
        };
    }

    /**
     * Stands in for the tracing handler that injects the propagation headers on start.
     */
    private static class RecordingHandler implements ObservationHandler<Observation.Context> {
        private final List<Observation.Context> stopped = new CopyOnWriteArrayList<>();

        @Override
        @SuppressWarnings("unchecked")
        public void onStart(Observation.Context context) {
            SenderContext<Map<String, String>> senderContext = (SenderContext<Map<String, String>>) context;
            senderContext.getSetter().set(senderContext.getCarrier(), "traceparent", TRACEPARENT);
        }

        @Override
        public void onStop(Observation.Context context) {
            stopped.add(context);
        }

        @Override
        public boolean supportsContext(Observation.Context context) {
            return context instanceof SenderContext;
        }

        Observation.Context stoppedFor(String path) {
            return stopped.stream().filter(context -> {
                KeyValue url = context.getHighCardinalityKeyValue("http.url");
                return url != null && url.getValue().endsWith(path);
            }).findFirst().orElseThrow(() -> new AssertionError("no stopped observation for " + path));
        }
    }
}
