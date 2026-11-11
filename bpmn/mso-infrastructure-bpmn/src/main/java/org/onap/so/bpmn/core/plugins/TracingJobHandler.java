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
import java.util.Map;
import java.util.Objects;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.onap.so.utils.TraceContextVariable;

class TracingJobHandler<T extends JobHandlerConfiguration> implements JobHandler<T> {

    private final JobHandler<T> delegate;
    private final Tracer tracer;
    private final Propagator propagator;

    TracingJobHandler(JobHandler<T> delegate, Tracer tracer, Propagator propagator) {
        this.delegate = delegate;
        this.tracer = tracer;
        this.propagator = propagator;
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public void execute(T configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
        Object traceparent = execution == null || tracer.currentSpan() != null ? null
                : execution.getVariable(TraceContextVariable.NAME);
        if (!(traceparent instanceof String)) {
            delegate.execute(configuration, execution, commandContext, tenantId);
            return;
        }
        Span span = propagator.extract(Map.of(TraceContextVariable.NAME, (String) traceparent), Map::get)
                .name("camunda job " + execution.getProcessDefinition().getKey() + "/"
                        + Objects.toString(execution.getActivityId(), delegate.getType()))
                .tag("camunda.job.type", delegate.getType())
                .tag("camunda.process_instance.id", execution.getProcessInstanceId()).start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            delegate.execute(configuration, execution, commandContext, tenantId);
        } catch (RuntimeException | Error e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Override
    public T newConfiguration(String canonicalString) {
        return delegate.newConfiguration(canonicalString);
    }

    @Override
    public void onDelete(T configuration, JobEntity jobEntity) {
        delegate.onDelete(configuration, jobEntity);
    }
}
