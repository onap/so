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
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * Verifies that the tracing pipeline (Micrometer Tracing + the OpenTelemetry bridge) is actually wired and reports
 * finished spans.
 *
 * {@code @AutoConfigureObservability} is required: {@code @SpringBootTest} otherwise installs Spring Boot's
 * observability test customizer, which disables tracing and registers a no-op {@code Tracer}. With it enabled, a test
 * {@link SpanProcessor} bean is collected by Boot's {@code OpenTelemetryTracingAutoConfiguration} and wired into the
 * live {@code SdkTracerProvider}, so the assertion observes a real span flowing through the pipeline. The processor
 * captures the span synchronously on {@code end()}.
 */
@AutoConfigureObservability
@Import(TracingTest.TestSpanProcessorConfiguration.class)
@TestPropertySource(properties = {"management.tracing.enabled=true", "management.tracing.sampling.probability=1.0",
        "management.otlp.tracing.export.enabled=false"})
public class TracingTest extends CatalogDbAdapterBaseTest {

    @Autowired
    ObservationRegistry observationRegistry;

    @Autowired
    Tracer tracer;

    @Autowired
    TestSpanProcessor testSpanProcessor;

    @Test
    public void thatTracingBeansAreAvailable() {
        assertNotNull("ObservationRegistry bean should be present", observationRegistry);
        assertNotNull("Tracer bean should be present", tracer);
    }

    @Test
    public void thatFinishedSpansAreReportedThroughThePipeline() {
        testSpanProcessor.clear();

        final Span span = tracer.nextSpan().name("catalogdb-tracing-test-span").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            // the span only needs to be opened and closed for it to be reported
        } finally {
            span.end();
        }

        assertTrue("A sampled span must be reported to the registered SpanProcessor",
                testSpanProcessor.spanNames().contains("catalogdb-tracing-test-span"));
    }

    @TestConfiguration
    static class TestSpanProcessorConfiguration {
        @Bean
        TestSpanProcessor testSpanProcessor() {
            return new TestSpanProcessor();
        }
    }

    /**
     * Captures the names of finished spans. The SDK only records sampled spans, which is why the test forces a sampling
     * probability of 1.0.
     */
    static class TestSpanProcessor implements SpanProcessor {
        private final List<String> spanNames = new CopyOnWriteArrayList<>();

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
            spanNames.add(span.getName());
        }

        @Override
        public boolean isEndRequired() {
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
