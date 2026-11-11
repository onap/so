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
package org.onap.so.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelPropagator;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Before;
import org.junit.Test;

public class ExternalTaskTracingTest {

    private static final String REMOTE_TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String REMOTE_SPAN_ID = "00f067aa0ba902b7";
    private static final String REMOTE = "00-" + REMOTE_TRACE_ID + "-" + REMOTE_SPAN_ID + "-01";

    private final ExternalTask task = mock(ExternalTask.class);
    private final ExternalTaskService service = mock(ExternalTaskService.class);
    private final ClientRequestContext request = mock(ClientRequestContext.class);
    private InMemorySpanExporter exporter;
    private OtelTracer tracer;
    private ExternalTaskTracing tracing;

    @Before
    public void setUp() {
        exporter = InMemorySpanExporter.create();
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(
                        SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(exporter)).build())
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance())).build();
        io.opentelemetry.api.trace.Tracer otel = sdk.getTracer("test");
        tracer = new OtelTracer(otel, new OtelCurrentTraceContext(), event -> {
        });
        tracing = new ExternalTaskTracing(Optional.of(tracer),
                Optional.of(new OtelPropagator(sdk.getPropagators(), otel)));
        when(task.getTopicName()).thenReturn("InventoryCreate");
        when(task.getId()).thenReturn("task-1");
    }

    @Test
    public void handlerRunsInAChildSpanOfTheProcessTraceContext() {
        when(task.getVariable(TraceContextVariable.NAME)).thenReturn(REMOTE);
        AtomicReference<Span> inside = new AtomicReference<>();

        tracing.wrap((t, s) -> inside.set(tracer.currentSpan())).execute(task, service);

        SpanData span = single();
        assertEquals(REMOTE_TRACE_ID, span.getTraceId());
        assertEquals(REMOTE_SPAN_ID, span.getParentSpanId());
        assertEquals("external-task InventoryCreate", span.getName());
        assertEquals(SpanKind.CONSUMER, span.getKind());
        assertEquals(span.getSpanId(), inside.get().context().spanId());
    }

    @Test
    public void spansStartedInsideTheHandlerAreChildren() {
        when(task.getVariable(TraceContextVariable.NAME)).thenReturn(REMOTE);

        tracing.wrap((t, s) -> tracer.nextSpan().name("client").start().end()).execute(task, service);

        List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(2, spans.size());
        SpanData client = spans.get(0);
        SpanData wrapper = spans.get(1);
        assertEquals("client", client.getName());
        assertEquals(wrapper.getSpanId(), client.getParentSpanId());
        assertEquals(REMOTE_TRACE_ID, client.getTraceId());
    }

    @Test
    public void handlerWithoutTraceContextGetsARootSpan() {
        AtomicReference<Span> inside = new AtomicReference<>();

        tracing.wrap((t, s) -> inside.set(tracer.currentSpan())).execute(task, service);

        SpanData span = single();
        assertFalse(span.getParentSpanContext().isValid());
        assertNotNull(inside.get());
    }

    @Test
    public void malformedOrNonStringTraceContextIsIgnored() {
        for (Object value : new Object[] {"garbage", 42}) {
            exporter.reset();
            when(task.getVariable(TraceContextVariable.NAME)).thenReturn(value);
            AtomicReference<Boolean> ran = new AtomicReference<>(false);

            tracing.wrap((t, s) -> ran.set(true)).execute(task, service);

            assertTrue(ran.get());
            assertFalse(single().getParentSpanContext().isValid());
        }
    }

    @Test
    public void failingHandlerEndsTheSpanWithError() {
        when(task.getVariable(TraceContextVariable.NAME)).thenReturn(REMOTE);
        ExternalTaskHandler failing = (t, s) -> {
            throw new IllegalStateException("boom");
        };

        assertThrows(IllegalStateException.class, () -> tracing.wrap(failing).execute(task, service));

        assertEquals(StatusCode.ERROR, single().getStatus().getStatusCode());
    }

    @Test
    public void interceptorPropagatesTheCurrentSpan() {
        Span span = tracer.nextSpan().name("handler").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            tracing.intercept(request);
        } finally {
            span.end();
        }

        verify(request).addHeader("traceparent",
                "00-" + span.context().traceId() + "-" + span.context().spanId() + "-01");
    }

    @Test
    public void interceptorOutsideASpanAddsNothing() {
        tracing.intercept(request);

        verifyNoInteractions(request);
    }

    @Test
    public void withoutTracingTheHandlerIsUnchanged() {
        ExternalTaskTracing disabled = new ExternalTaskTracing(Optional.empty(), Optional.empty());
        ExternalTaskHandler handler = (t, s) -> {
        };

        assertSame(handler, disabled.wrap(handler));
        disabled.intercept(request);
        verifyNoInteractions(request);
    }

    private SpanData single() {
        List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(1, spans.size());
        return spans.get(0);
    }
}
