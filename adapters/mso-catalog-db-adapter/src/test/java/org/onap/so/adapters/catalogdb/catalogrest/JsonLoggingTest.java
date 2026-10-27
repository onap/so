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

package org.onap.so.adapters.catalogdb.catalogrest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.onap.so.adapters.catalogdb.CatalogDbAdapterBaseTest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.test.context.TestPropertySource;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

@AutoConfigureObservability
@TestPropertySource(
        properties = {"logging.config=classpath:org/onap/so/logger/logback-so.xml", "management.tracing.enabled=true",
                "management.tracing.sampling.probability=1.0", "management.otlp.tracing.export.enabled=false"})
public class JsonLoggingTest extends CatalogDbAdapterBaseTest {

    @Rule
    public OutputCaptureRule output = new OutputCaptureRule();

    @Autowired
    Tracer tracer;

    // Boot initialises logging once per JVM; cached contexts from other test classes would otherwise
    // keep the test logback config and logging.config above would be silently ignored.
    @BeforeClass
    @AfterClass
    public static void resetLogging() {
        LoggingSystem.get(JsonLoggingTest.class.getClassLoader()).cleanUp();
    }

    @Test
    public void logLineCarriesTheActiveSpan() {
        Span span = tracer.nextSpan().name("json-logging-test").start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            LoggerFactory.getLogger("org.onap.so.JsonLoggingTest").info("json-logging-marker");
        } finally {
            span.end();
        }
        String line = output.getOut().lines().filter(l -> l.contains("json-logging-marker")).findFirst().orElse(null);
        assertNotNull("no log line captured", line);
        JSONObject json = new JSONObject(line);
        assertEquals(span.context().traceId(), json.getString("trace_id"));
        assertEquals(span.context().spanId(), json.getString("span_id"));
        assertEquals("so-catalog-db-adapter", json.getString("component"));
        assertEquals("onap-so", json.getString("project"));
    }
}
