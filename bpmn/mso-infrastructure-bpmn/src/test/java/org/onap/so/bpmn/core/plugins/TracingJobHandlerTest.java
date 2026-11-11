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
package org.onap.so.bpmn.core.plugins;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelPropagator;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.junit.Test;

public class TracingJobHandlerTest {

    @Test
    @SuppressWarnings("unchecked")
    public void jobWithoutExecutionIsDelegatedUntouched() {
        InMemorySpanExporter exporter = InMemorySpanExporter.create();
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(
                        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build())
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).build();
        io.opentelemetry.api.trace.Tracer otel = sdk.getTracer("test");
        JobHandler<JobHandlerConfiguration> delegate = mock(JobHandler.class);
        JobHandlerConfiguration configuration = mock(JobHandlerConfiguration.class);

        new TracingJobHandler<>(delegate, new OtelTracer(otel, new OtelCurrentTraceContext(), event -> {
        }), new OtelPropagator(sdk.getPropagators(), otel)).execute(configuration, null, null, null);

        verify(delegate).execute(configuration, null, null, null);
        assertTrue(exporter.getFinishedSpanItems().isEmpty());
    }
}
