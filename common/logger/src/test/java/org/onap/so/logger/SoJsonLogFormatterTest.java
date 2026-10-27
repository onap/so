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

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MarkerFactory;
import org.springframework.mock.env.MockEnvironment;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.LoggingEvent;

class SoJsonLogFormatterTest {

    private final LoggerContext context = new LoggerContext();
    private final Logger logger = context.getLogger("org.onap.so.Example");
    private ThrowableProxyConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ThrowableProxyConverter();
        converter.setContext(context);
        converter.start();
    }

    private SoJsonLogFormatter formatter(MockEnvironment env) {
        return new SoJsonLogFormatter(env, converter);
    }

    private LoggingEvent event(String message, Throwable t, Map<String, String> mdc) {
        LoggingEvent e = new LoggingEvent("fqcn", logger, Level.INFO, message, t, null);
        e.setMDCPropertyMap(mdc);
        e.setThreadName("http-nio-8080-exec-1");
        return e;
    }

    @Test
    void writesTheMandatoryFields() {
        String line = formatter(new MockEnvironment().withProperty("spring.application.name", "so-bpmn-infra"))
                .format(event("hello", null, Map.of("traceId", "4bf92f3577b34da6a3ce929d0e0e4736", "spanId",
                        "00f067aa0ba902b7", "RequestID", "req-1")));
        JSONObject json = new JSONObject(line);
        assertThat(json.getString("timestamp")).matches("\\d{4}-\\d{2}-\\d{2}T.*Z");
        assertThat(json.getString("log_level")).isEqualTo("INFO");
        assertThat(json.getString("message")).isEqualTo("hello");
        assertThat(json.getString("project")).isEqualTo("onap-so");
        assertThat(json.getString("component")).isEqualTo("so-bpmn-infra");
        assertThat(json.getString("class_name")).isEqualTo("org.onap.so.Example");
        assertThat(json.getString("thread")).isEqualTo("http-nio-8080-exec-1");
        assertThat(json.getString("trace_id")).isEqualTo("4bf92f3577b34da6a3ce929d0e0e4736");
        assertThat(json.getString("span_id")).isEqualTo("00f067aa0ba902b7");
        assertThat(json.getString("correlation_identifier")).isEqualTo("req-1");
        assertThat(json.has("traceId")).isFalse();
        assertThat(line).endsWith("}\n");
    }

    @Test
    void emptyMdcValuesAreOmitted() {
        JSONObject json = new JSONObject(formatter(new MockEnvironment())
                .format(event("x", null, Map.of("ErrorCode", "", "TargetEntity", "", "PartnerName", "SO"))));
        assertThat(json.has("error_code")).isFalse();
        assertThat(json.has("TargetEntity")).isFalse();
        assertThat(json.getString("PartnerName")).isEqualTo("SO");
    }

    @Test
    void errorCodeOnlyWhenSet() {
        JSONObject json =
                new JSONObject(formatter(new MockEnvironment()).format(event("x", null, Map.of("ErrorCode", "300"))));
        assertThat(json.getString("error_code")).isEqualTo("300");
    }

    @Test
    void oneLinePerEventEvenWithNewlinesAndCauses() {
        Exception e = new IllegalStateException("outer", new RuntimeException("inner\ncause"));
        String line = formatter(new MockEnvironment()).format(event("first\nsecond", e, Map.of()));
        assertThat(line.indexOf('\n')).isEqualTo(line.length() - 1);
        JSONObject json = new JSONObject(line);
        assertThat(json.getString("message")).isEqualTo("first\nsecond");
        assertThat(json.getString("stack_trace")).contains("IllegalStateException: outer").contains("inner");
    }

    @Test
    void masksCredentialsInTheMessage() {
        String line = formatter(new MockEnvironment())
                .format(event("Headers      : [Authorization:\"Basic dXNlcjpwYXNz\"]", null, Map.of()));
        assertThat(line).doesNotContain("dXNlcjpwYXNz").contains(LogMasking.REDACTED);
    }

    @Test
    void masksCredentialsInTheStackTrace() {
        String line = formatter(new MockEnvironment())
                .format(event("failed", new RuntimeException("Authorization: Basic dXNlcjpwYXNz"), Map.of()));
        assertThat(new JSONObject(line).getString("stack_trace")).doesNotContain("dXNlcjpwYXNz")
                .contains(LogMasking.REDACTED);
    }

    @Test
    void mdcKeysCollidingWithFieldsOrEachOtherAreSkipped() {
        String line = formatter(new MockEnvironment())
                .format(event("hello", null, Map.of("traceId", "a", "trace_id", "b", "message", "m")));
        assertThat(line.indexOf('\n')).isEqualTo(line.length() - 1);
        JSONObject json = new JSONObject(line);
        assertThat(json.getString("message")).isEqualTo("hello");
        assertThat(json.getString("trace_id")).isIn("a", "b");
        assertThat(line.split("\"trace_id\"", -1)).hasSize(2);
    }

    @Test
    void writesMarkerNames() {
        LoggingEvent e = event("exit", null, Map.of());
        e.addMarker(MarkerFactory.getMarker("EXIT"));
        assertThat(new JSONObject(formatter(new MockEnvironment()).format(e)).getString("marker")).isEqualTo("EXIT");
    }

    @Test
    void componentFallsBackToAppThenSo() {
        assertThat(new JSONObject(
                formatter(new MockEnvironment().withProperty("APP", "bpmn-infra")).format(event("x", null, Map.of())))
                        .getString("component")).isEqualTo("so-bpmn-infra");
        assertThat(new JSONObject(formatter(new MockEnvironment()).format(event("x", null, Map.of())))
                .getString("component")).isEqualTo("so");
    }
}
