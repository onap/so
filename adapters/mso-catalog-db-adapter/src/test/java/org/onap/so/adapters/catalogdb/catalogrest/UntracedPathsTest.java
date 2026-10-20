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

package org.onap.so.adapters.catalogdb.catalogrest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.TestPropertySource;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * Exercises the filtering against the running application: the predicate from {@code UntracedPathsConfiguration}, and
 * the two observation switches OOM sets for every SO component, which this test sets the same way.
 */
@AutoConfigureObservability
@Import(UntracedPathsTest.TestConfig.class)
@TestPropertySource(properties = {"management.tracing.enabled=true", "management.tracing.sampling.probability=1.0",
        "management.otlp.tracing.export.enabled=false", "management.observations.enable.spring.security=false",
        "management.observations.enable.tasks.scheduled.execution=false"})
public class UntracedPathsTest extends CatalogDbAdapterBaseTest {

    private static final String SERVICE_RESOURCES =
            "/ecomp/mso/catalog/v2/serviceResources?serviceModelUuid=5df8b6de-2083-11e7-93ae-92361f002671";

    private final TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

    @Autowired
    private RecordingSpanProcessor spans;

    @Autowired
    private FrequentTask frequentTask;

    @Before
    public void clearSpans() {
        spans.clear();
    }

    @Test
    public void healthProbeIsNotTracedWhileApiRequestsAre() throws InterruptedException {
        assertEquals(200, get("/manage/health").getStatusCode().value());
        assertEquals(200, get(SERVICE_RESOURCES).getStatusCode().value());

        awaitTrue("the API request must still be traced",
                () -> spans.urls().stream().anyMatch(url -> url.startsWith("/ecomp/mso/catalog/v2/serviceResources")));
        assertTrue("health requests were traced: " + spans.urls(),
                spans.urls().stream().noneMatch(url -> url.startsWith("/manage/health")));
    }

    @Test
    public void securityObservationsAreNotTraced() throws InterruptedException {
        assertEquals(200, get(SERVICE_RESOURCES).getStatusCode().value());

        awaitTrue("the API request must still be traced", () -> !spans.urls().isEmpty());
        assertTrue("security spans were traced: " + spans.names(),
                spans.names().stream().noneMatch(name -> name.startsWith("security") || name.startsWith("authorize")
                        || name.startsWith("secured") || name.startsWith("authenticate")));
    }

    @Test
    public void scheduledTasksAreNotTraced() throws InterruptedException {
        int runs = frequentTask.runs.get();
        awaitTrue("the scheduled task must keep running", () -> frequentTask.runs.get() >= runs + 3);

        assertTrue("scheduled task spans were traced: " + spans.names(),
                spans.names().stream().noneMatch(name -> name.startsWith("task ")));
    }

    private ResponseEntity<String> get(String path) {
        return restTemplate.getForEntity("http://localhost:" + port + path, String.class);
    }

    private static void awaitTrue(String message, BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 10_000;
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError(message);
            }
            Thread.sleep(50);
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        RecordingSpanProcessor recordingSpanProcessor() {
            return new RecordingSpanProcessor();
        }

        @Bean
        FrequentTask frequentTask() {
            return new FrequentTask();
        }
    }

    static class FrequentTask {
        private final AtomicInteger runs = new AtomicInteger();

        @Scheduled(fixedRate = 100)
        public void run() {
            runs.incrementAndGet();
        }
    }

    static class RecordingSpanProcessor implements SpanProcessor {
        private static final AttributeKey<String> HTTP_URL = AttributeKey.stringKey("http.url");

        private final List<ReadableSpan> finished = new CopyOnWriteArrayList<>();

        @Override
        public void onStart(final Context parentContext, final ReadWriteSpan span) {
            // only finished spans are of interest
        }

        @Override
        public boolean isStartRequired() {
            return false;
        }

        @Override
        public void onEnd(final ReadableSpan span) {
            finished.add(span);
        }

        @Override
        public boolean isEndRequired() {
            return true;
        }

        List<String> names() {
            return finished.stream().map(ReadableSpan::getName).collect(Collectors.toList());
        }

        List<String> urls() {
            return finished.stream().map(span -> span.getAttribute(HTTP_URL)).filter(url -> url != null)
                    .collect(Collectors.toList());
        }

        void clear() {
            finished.clear();
        }
    }
}
