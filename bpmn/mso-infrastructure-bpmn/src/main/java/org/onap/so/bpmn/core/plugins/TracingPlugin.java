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

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.AsyncContinuationJobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.ProcessEventJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.onap.so.utils.TraceContextVariable;
import org.springframework.stereotype.Component;

@Component
public class TracingPlugin extends AbstractProcessEnginePlugin {

    private final Tracer tracer;
    private final Propagator propagator;

    public TracingPlugin(Optional<Tracer> tracer, Optional<Propagator> propagator) {
        boolean enabled = tracer.isPresent() && propagator.isPresent();
        this.tracer = enabled ? tracer.get() : null;
        this.propagator = enabled ? propagator.get() : null;
    }

    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
        if (tracer == null) {
            return;
        }
        List<BpmnParseListener> preParseListeners = configuration.getCustomPreBPMNParseListeners();
        if (preParseListeners == null) {
            preParseListeners = new ArrayList<>();
            configuration.setCustomPreBPMNParseListeners(preParseListeners);
        }
        preParseListeners.add(new AbstractBpmnParseListener() {
            @Override
            public void parseProcess(Element processElement, ProcessDefinitionEntity processDefinition) {
                processDefinition.addListener(ExecutionListener.EVENTNAME_START,
                        (ExecutionListener) TracingPlugin.this::storeTraceContext);
            }
        });
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void postInit(ProcessEngineConfigurationImpl configuration) {
        if (tracer == null) {
            return;
        }
        Map<String, JobHandler> jobHandlers = configuration.getJobHandlers();
        for (String type : List.of(AsyncContinuationJobHandler.TYPE, ProcessEventJobHandler.TYPE)) {
            jobHandlers.computeIfPresent(type, (key, handler) -> new TracingJobHandler(handler, tracer, propagator));
        }
    }

    private void storeTraceContext(DelegateExecution execution) {
        Span current = tracer.currentSpan();
        if (current == null) {
            return;
        }
        Map<String, String> carrier = new HashMap<>();
        propagator.inject(current.context(), carrier, Map::put);
        String traceparent = carrier.get(TraceContextVariable.NAME);
        if (traceparent != null) {
            execution.setVariableLocal(TraceContextVariable.NAME, traceparent);
        }
    }
}
