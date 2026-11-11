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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelPropagator;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.micrometer.tracing.propagation.Propagator;
import io.opentelemetry.api.trace.SpanContext;
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
import java.util.UUID;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.utils.TraceContextVariable;

public class TracingPluginTest {

    private InMemorySpanExporter exporter;
    private OtelTracer tracer;
    private OtelPropagator propagator;
    private ProcessEngine engine;

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
        propagator = new OtelPropagator(sdk.getPropagators(), otel);
        RecordingDelegate.seen = null;
    }

    @After
    public void closeEngine() {
        if (engine != null) {
            engine.close();
        }
    }

    @Test
    public void storesTheStartingSpanOnTheProcessInstance() {
        buildEngine(Optional.of(tracer), Optional.of(propagator));
        Span request = tracer.nextSpan().name("request").start();

        ProcessInstance instance = startIn(request, "traced");

        assertEquals(traceparent(request), variable(instance));
    }

    @Test
    public void jobRunsInAChildSpanOfTheStoredContext() {
        buildEngine(Optional.of(tracer), Optional.of(propagator));
        Span request = tracer.nextSpan().name("request").start();
        ProcessInstance instance = startIn(request, "traced");
        exporter.reset();

        runJob(instance);

        SpanData job = single();
        assertEquals(request.context().traceId(), job.getTraceId());
        assertEquals(request.context().spanId(), job.getParentSpanId());
        assertEquals("camunda job traced/work", job.getName());
        assertEquals(job.getSpanId(), RecordingDelegate.seen.getSpanId());
    }

    @Test
    public void failingJobMarksTheSpanAsError() {
        buildEngine(Optional.of(tracer), Optional.of(propagator));
        ProcessInstance instance = startIn(tracer.nextSpan().name("request").start(), "failing");
        exporter.reset();

        assertThrows(RuntimeException.class, () -> runJob(instance));

        assertEquals(StatusCode.ERROR, single().getStatus().getStatusCode());
    }

    @Test
    public void calledProcessInheritsTheCallingJobSpan() {
        buildEngine(Optional.of(tracer), Optional.of(propagator));
        ProcessInstance parent = startIn(tracer.nextSpan().name("request").start(), "parent");
        exporter.reset();

        runJob(parent);

        SpanData job = single();
        ProcessInstance child =
                engine.getRuntimeService().createProcessInstanceQuery().processDefinitionKey("child").singleResult();
        assertEquals("00-" + job.getTraceId() + "-" + job.getSpanId() + "-01", variable(child));
    }

    @Test
    public void processStartedWithoutASpanKeepsTheOldBehaviour() {
        buildEngine(Optional.of(tracer), Optional.of(propagator));

        ProcessInstance instance = engine.getRuntimeService().startProcessInstanceByKey("traced");
        assertNull(variable(instance));
        runJob(instance);

        assertNull(RecordingDelegate.seen);
        assertTrue(exporter.getFinishedSpanItems().isEmpty());
    }

    @Test
    public void jobExecutedInsideACurrentSpanIsNotWrapped() {
        buildEngine(Optional.of(tracer), Optional.of(propagator));
        ProcessInstance instance = startIn(tracer.nextSpan().name("request").start(), "traced");
        exporter.reset();
        Span outer = tracer.nextSpan().name("outer").start();

        try (Tracer.SpanInScope ignored = tracer.withSpan(outer)) {
            runJob(instance);
        } finally {
            outer.end();
        }

        assertEquals(outer.context().spanId(), RecordingDelegate.seen.getSpanId());
        assertEquals("outer", single().getName());
    }

    @Test
    public void withoutTracingNothingIsStoredOrWrapped() {
        buildEngine(Optional.empty(), Optional.empty());
        ProcessInstance instance = startIn(tracer.nextSpan().name("request").start(), "traced");
        assertNull(variable(instance));
        exporter.reset();

        runJob(instance);

        assertNull(RecordingDelegate.seen);
        assertTrue(exporter.getFinishedSpanItems().isEmpty());
    }

    @Test
    public void batchOperationsStillWork() {
        buildEngine(Optional.of(tracer), Optional.of(propagator));
        ProcessInstance instance = startIn(tracer.nextSpan().name("request").start(), "traced");

        assertNotNull(engine.getRuntimeService().deleteProcessInstancesAsync(List.of(instance.getId()), "test"));
    }

    @Test
    public void repeatingTimerSchedulesTheNextCycle() {
        buildEngine(Optional.of(tracer), Optional.of(propagator));
        ProcessInstance instance = startIn(tracer.nextSpan().name("request").start(), "ticking");

        runJob(instance);

        assertEquals(1,
                engine.getManagementService().createJobQuery().processInstanceId(instance.getId()).timers().count());
    }

    private void buildEngine(Optional<Tracer> tracer, Optional<Propagator> propagator) {
        ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        configuration.setJdbcUrl("jdbc:h2:mem:tracing-" + UUID.randomUUID());
        configuration.setJobExecutorActivate(false);
        configuration.setHistory(ProcessEngineConfiguration.HISTORY_NONE);
        configuration.setProcessEnginePlugins(List.of(new TracingPlugin(tracer, propagator)));
        engine = configuration.buildProcessEngine();
        engine.getRepositoryService().createDeployment()
                .addModelInstance("traced.bpmn",
                        Bpmn.createExecutableProcess("traced").startEvent().serviceTask("work").camundaAsyncBefore()
                                .camundaClass(RecordingDelegate.class.getName()).endEvent().done())
                .addModelInstance("failing.bpmn",
                        Bpmn.createExecutableProcess("failing").startEvent().serviceTask("work").camundaAsyncBefore()
                                .camundaClass(FailingDelegate.class.getName()).endEvent().done())
                .addModelInstance("parent.bpmn",
                        Bpmn.createExecutableProcess("parent").startEvent().callActivity("call").calledElement("child")
                                .camundaAsyncBefore().endEvent().done())
                .addModelInstance("ticking.bpmn",
                        Bpmn.createExecutableProcess("ticking").startEvent().receiveTask("wait").endEvent()
                                .moveToActivity("wait").boundaryEvent("tick").cancelActivity(false)
                                .timerWithCycle("R3/PT1M").endEvent().done())
                .addModelInstance("child.bpmn",
                        Bpmn.createExecutableProcess("child").startEvent().receiveTask("wait").endEvent().done())
                .deploy();
    }

    private ProcessInstance startIn(Span span, String processKey) {
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            return engine.getRuntimeService().startProcessInstanceByKey(processKey);
        } finally {
            span.end();
        }
    }

    private void runJob(ProcessInstance instance) {
        String jobId = engine.getManagementService().createJobQuery().processInstanceId(instance.getId()).singleResult()
                .getId();
        engine.getManagementService().executeJob(jobId);
    }

    private Object variable(ProcessInstance instance) {
        return engine.getRuntimeService().getVariable(instance.getId(), TraceContextVariable.NAME);
    }

    private static String traceparent(Span span) {
        return "00-" + span.context().traceId() + "-" + span.context().spanId() + "-01";
    }

    private SpanData single() {
        List<SpanData> spans = exporter.getFinishedSpanItems();
        assertEquals(1, spans.size());
        return spans.get(0);
    }

    public static class RecordingDelegate implements JavaDelegate {
        static volatile SpanContext seen;

        @Override
        public void execute(DelegateExecution execution) {
            SpanContext current = io.opentelemetry.api.trace.Span.current().getSpanContext();
            seen = current.isValid() ? current : null;
        }
    }

    public static class FailingDelegate implements JavaDelegate {
        @Override
        public void execute(DelegateExecution execution) {
            throw new IllegalStateException("boom");
        }
    }
}
