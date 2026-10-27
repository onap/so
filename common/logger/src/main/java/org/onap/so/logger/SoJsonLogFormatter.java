/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom
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

package org.onap.so.logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.slf4j.Marker;
import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLogFormatter;
import org.springframework.core.env.Environment;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class SoJsonLogFormatter implements StructuredLogFormatter<ILoggingEvent> {

    static final String PROJECT = "onap-so";

    private static final Map<String, String> MDC_FIELD_NAMES = Map.of("traceId", "trace_id", "spanId", "span_id",
            "RequestID", "correlation_identifier", "ErrorCode", "error_code");

    private static final Set<String> RESERVED_FIELD_NAMES = Set.of("timestamp", "log_level", "message", "project",
            "component", "class_name", "thread", "marker", "stack_trace");

    private final JsonWriter<ILoggingEvent> writer;

    public SoJsonLogFormatter(Environment environment, ThrowableProxyConverter throwableProxyConverter) {
        String component = component(environment);
        this.writer = JsonWriter.<ILoggingEvent>of(members -> {
            members.add("timestamp", event -> event.getInstant().toString());
            members.add("log_level", event -> event.getLevel().toString());
            members.add("message", event -> LogMasking.mask(event.getFormattedMessage()));
            members.add("project", PROJECT);
            members.add("component", component);
            members.add("class_name", ILoggingEvent::getLoggerName);
            members.add("thread", ILoggingEvent::getThreadName);
            members.add("marker", SoJsonLogFormatter::markers).whenNotNull();
            members.add().usingPairs((ILoggingEvent event, BiConsumer<String, String> pairs) -> {
                Set<String> written = new HashSet<>(RESERVED_FIELD_NAMES);
                event.getMDCPropertyMap().forEach((key, value) -> {
                    String name = MDC_FIELD_NAMES.getOrDefault(key, key);
                    if (value != null && !value.isEmpty() && written.add(name)) {
                        pairs.accept(name, value);
                    }
                });
            });
            members.add("stack_trace", event -> event.getThrowableProxy() == null ? null
                    : LogMasking.mask(throwableProxyConverter.convert(event))).whenNotNull();
        }).withNewLineAtEnd();
    }

    @Override
    public String format(ILoggingEvent event) {
        return writer.writeToString(event);
    }

    private static String component(Environment environment) {
        String name = environment.getProperty("spring.application.name");
        if (name != null && !name.isBlank()) {
            return name;
        }
        String app = environment.getProperty("APP");
        return (app == null || app.isBlank()) ? "so" : "so-" + app;
    }

    private static String markers(ILoggingEvent event) {
        if (event.getMarkerList() == null || event.getMarkerList().isEmpty()) {
            return null;
        }
        return event.getMarkerList().stream().map(Marker::getName).collect(Collectors.joining(","));
    }
}
