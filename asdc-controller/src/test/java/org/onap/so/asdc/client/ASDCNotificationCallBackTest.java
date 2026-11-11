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

package org.onap.so.asdc.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.NotificationMessageContext;
import org.springframework.test.util.ReflectionTestUtils;
import io.micrometer.observation.ObservationHandler.FirstMatchingCompositeObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.handler.PropagatingReceiverTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelPropagator;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;

public class ASDCNotificationCallBackTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String PARENT_SPAN_ID = "00f067aa0ba902b7";
    private static final String TOPIC = "SDC-DISTR-NOTIF-TOPIC-AUTO";

    private final List<SpanData> exportedSpans = new CopyOnWriteArrayList<>();
    private final ASDCController asdcController = mock(ASDCController.class);
    private final ASDCNotificationCallBack callBack = new ASDCNotificationCallBack();
    private SdkTracerProvider tracerProvider;

    @Before
    public void setUp() {
        tracerProvider = SdkTracerProvider.builder().addSpanProcessor(SimpleSpanProcessor.create(new SpanExporter() {
            @Override
            public CompletableResultCode export(Collection<SpanData> spans) {
                exportedSpans.addAll(spans);
                return CompletableResultCode.ofSuccess();
            }

            @Override
            public CompletableResultCode flush() {
                return CompletableResultCode.ofSuccess();
            }

            @Override
            public CompletableResultCode shutdown() {
                return CompletableResultCode.ofSuccess();
            }
        })).build();
        io.opentelemetry.api.trace.Tracer otelTracer = tracerProvider.get("test");
        OtelTracer tracer = new OtelTracer(otelTracer, new OtelCurrentTraceContext(), event -> {
        });
        OtelPropagator propagator =
                new OtelPropagator(ContextPropagators.create(W3CTraceContextPropagator.getInstance()), otelTracer);

        ObservationRegistry observationRegistry = ObservationRegistry.create();
        observationRegistry.observationConfig()
                .observationHandler(new FirstMatchingCompositeObservationHandler(
                        new PropagatingReceiverTracingObservationHandler<>(tracer, propagator),
                        new DefaultTracingObservationHandler(tracer)));

        ReflectionTestUtils.setField(callBack, "asdcController", asdcController);
        ReflectionTestUtils.setField(callBack, "observationRegistry", observationRegistry);
    }

    @After
    public void tearDown() {
        tracerProvider.close();
    }

    @Test
    public void activateCallback_ContinuesProducerTraceInConsumerSpan() {
        INotificationData notification = mock(INotificationData.class);
        NotificationMessageContext messageContext = new NotificationMessageContext(TOPIC, 0, 42L,
                Map.of("traceparent", "00-" + TRACE_ID + "-" + PARENT_SPAN_ID + "-01"));
        doReturn(messageContext).when(notification).getMessageContext();
        doReturn("distribution-id").when(notification).getDistributionID();
        AtomicReference<String> traceIdDuringProcessing = new AtomicReference<>();
        doAnswer(invocation -> {
            traceIdDuringProcessing.set(Span.current().getSpanContext().getTraceId());
            return null;
        }).when(asdcController).treatNotification(notification);

        callBack.activateCallback(notification);

        verify(asdcController).treatNotification(notification);
        assertEquals(1, exportedSpans.size());
        SpanData span = exportedSpans.get(0);
        assertEquals(SpanKind.CONSUMER, span.getKind());
        assertEquals(TOPIC + " process", span.getName());
        assertEquals(TRACE_ID, span.getTraceId());
        assertEquals(PARENT_SPAN_ID, span.getParentSpanId());
        assertEquals(TRACE_ID, traceIdDuringProcessing.get());
        assertEquals("42", span.getAttributes().get(AttributeKey.stringKey("messaging.kafka.message.offset")));
    }

    @Test
    public void activateCallback_StartsNewTraceWithoutTraceparent() {
        INotificationData notification = mock(INotificationData.class);
        doReturn(NotificationMessageContext.EMPTY).when(notification).getMessageContext();

        callBack.activateCallback(notification);

        verify(asdcController).treatNotification(notification);
        assertEquals(1, exportedSpans.size());
        SpanData span = exportedSpans.get(0);
        assertEquals(SpanKind.CONSUMER, span.getKind());
        assertFalse(span.getParentSpanContext().isValid());
    }
}
