/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.Test;
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

/**
 * Verifies that the tracing pipeline introduced with the Spring Boot 3 migration (Micrometer Tracing + the Brave
 * bridge, which replaced Spring Cloud Sleuth) is actually wired and reports finished spans.
 *
 * {@code @AutoConfigureObservability} is required: {@code @SpringBootTest} otherwise installs Spring Boot's
 * observability test customizer, which disables tracing and registers a no-op {@code Tracer}. With it enabled, a test
 * {@link SpanHandler} bean is collected by Boot's {@code BraveAutoConfiguration} and wired into the live Brave
 * {@code Tracing}, so the assertion observes a real span flowing through the pipeline. This restores the behavioural
 * verification of the original test without its {@code Thread.sleep}-then-poll-WireMock flakiness: the handler captures
 * the span synchronously on {@code end()}.
 */
@AutoConfigureObservability
@Import(TracingTest.TestSpanHandlerConfiguration.class)
@TestPropertySource(properties = {"management.tracing.sampling.probability=1.0"})
public class TracingTest extends CatalogDbAdapterBaseTest {

    @Autowired
    ObservationRegistry observationRegistry;

    @Autowired
    Tracer tracer;

    @Autowired
    TestSpanHandler testSpanHandler;

    @Test
    public void thatTracingBeansAreAvailable() {
        assertNotNull("ObservationRegistry bean should be present", observationRegistry);
        assertNotNull("Tracer bean should be present", tracer);
    }

    @Test
    public void thatFinishedSpansAreReportedThroughThePipeline() {
        testSpanHandler.clear();

        final Span span = tracer.nextSpan().name("catalogdb-tracing-test-span").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            // the span only needs to be opened and closed for it to be reported
        } finally {
            span.end();
        }

        assertTrue("A sampled span must be reported to the registered SpanHandler",
                testSpanHandler.spanNames().contains("catalogdb-tracing-test-span"));
    }

    @TestConfiguration
    static class TestSpanHandlerConfiguration {
        @Bean
        TestSpanHandler testSpanHandler() {
            return new TestSpanHandler();
        }
    }

    /**
     * Captures the names of spans reported to Brave on {@code end()}. Brave only invokes {@code end()} for sampled
     * spans, which is why the test forces a sampling probability of 1.0.
     */
    static class TestSpanHandler extends SpanHandler {
        private final List<String> spanNames = new CopyOnWriteArrayList<>();

        @Override
        public boolean end(final TraceContext context, final MutableSpan span, final Cause cause) {
            spanNames.add(span.name());
            return true;
        }

        List<String> spanNames() {
            return spanNames;
        }

        void clear() {
            spanNames.clear();
        }
    }

}
