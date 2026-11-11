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
package org.onap.so.bpmn.infrastructure;

import static org.junit.Assert.assertEquals;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

public class AsyncExecutorTracingTest {

    @Test
    public void asyncTasksRunInTheCallersSpan() throws Exception {
        OpenTelemetrySdk sdk =
                OpenTelemetrySdk.builder().setTracerProvider(SdkTracerProvider.builder().build()).build();
        Tracer tracer = new OtelTracer(sdk.getTracer("test"), new OtelCurrentTraceContext(), event -> {
        });
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new DefaultTracingObservationHandler(tracer));
        MSOInfrastructureApplication application = new MSOInfrastructureApplication();
        ReflectionTestUtils.setField(application, "corePoolSize", 1);
        ReflectionTestUtils.setField(application, "maxPoolSize", 1);
        ReflectionTestUtils.setField(application, "queueCapacity", 1);
        Executor executor = application.asyncExecutor();
        MDC.put("RequestID", "request-1");
        Observation request = Observation.start("request", registry);
        try {
            String expected;
            CompletableFuture<String> seen;
            try (Observation.Scope ignored = request.openScope()) {
                expected = tracer.currentSpan().context().spanId();
                seen = CompletableFuture.supplyAsync(
                        () -> io.opentelemetry.api.trace.Span.current().getSpanContext().getSpanId(), executor);
            }

            assertEquals(expected, seen.get(30, TimeUnit.SECONDS));
        } finally {
            request.stop();
            ((ThreadPoolTaskExecutor) executor).shutdown();
            MDC.clear();
        }
    }
}
