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

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import java.util.Map;
import java.util.Optional;
import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.stereotype.Component;

@Component
public class ExternalTaskTracing implements ClientRequestInterceptor {

    private final Tracer tracer;
    private final Propagator propagator;

    public ExternalTaskTracing(Optional<Tracer> tracer, Optional<Propagator> propagator) {
        boolean enabled = tracer.isPresent() && propagator.isPresent();
        this.tracer = enabled ? tracer.get() : null;
        this.propagator = enabled ? propagator.get() : null;
    }

    public ExternalTaskHandler wrap(ExternalTaskHandler handler) {
        if (tracer == null) {
            return handler;
        }
        return (task, service) -> {
            Span span = spanBuilder(task.getVariable(TraceContextVariable.NAME))
                    .name("external-task " + task.getTopicName()).kind(Span.Kind.CONSUMER)
                    .tag("camunda.external_task.id", task.getId()).start();
            try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
                handler.execute(task, service);
            } catch (RuntimeException | Error e) {
                span.error(e);
                throw e;
            } finally {
                span.end();
            }
        };
    }

    @Override
    public void intercept(ClientRequestContext requestContext) {
        if (tracer == null) {
            return;
        }
        Span current = tracer.currentSpan();
        if (current != null) {
            propagator.inject(current.context(), requestContext, ClientRequestContext::addHeader);
        }
    }

    private Span.Builder spanBuilder(Object traceparent) {
        if (traceparent instanceof String) {
            return propagator.extract(Map.of(TraceContextVariable.NAME, (String) traceparent), Map::get);
        }
        return tracer.spanBuilder();
    }
}
