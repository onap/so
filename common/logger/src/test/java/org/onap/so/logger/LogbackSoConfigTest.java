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
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.OutputStreamAppender;

class LogbackSoConfigTest {

    @TempDir
    Path logsDir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private LoggerContext configure() throws Exception {
        LoggerContext context = new LoggerContext();
        context.setMDCAdapter(new LogbackMDCAdapter());
        context.putObject(Environment.class.getName(),
                new MockEnvironment().withProperty("spring.application.name", "so-test"));
        context.putProperty("logs_dir", logsDir.toString());
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(getClass().getResource("/org/onap/so/logger/logback-so.xml"));
        OutputStreamAppender<?> stdout =
                (OutputStreamAppender<?>) context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).getAppender("STDOUT");
        stdout.setOutputStream(out);
        return context;
    }

    @Test
    void everyLineIsOneJsonObject() throws Exception {
        LoggerContext context = configure();
        context.getLogger("org.onap.so.X").info("multi\nline", new RuntimeException("boom"));
        context.getLogger("AUDIT").info("audit entry");
        context.getLogger("METRIC").info("metric entry");
        context.stop();
        String[] lines = out.toString(StandardCharsets.UTF_8).split("\n");
        assertThat(lines).hasSize(3);
        for (String line : lines) {
            assertThat(new JSONObject(line).getString("component")).isEqualTo("so-test");
        }
    }

    @Test
    void onapDebugIsOffByDefault() throws Exception {
        LoggerContext context = configure();
        context.getLogger("org.onap.so.X").debug("hidden");
        context.getLogger("org.apache.cxf.interceptor.LoggingOutInterceptor").debug("hidden");
        context.stop();
        assertThat(out.toString(StandardCharsets.UTF_8)).isEmpty();
    }

    @Test
    void springLevelPropertyOverridesXmlLevel() throws Exception {
        LoggerContext context = configure();
        context.getLogger("org.onap").setLevel(Level.DEBUG); // what logging.level.org.onap=DEBUG does after init
        context.getLogger("org.onap.so.X").debug("Authorization:\"Basic dXNlcjpwYXNz\"");
        context.stop();
        assertThat(out.toString(StandardCharsets.UTF_8)).contains(LogMasking.REDACTED).doesNotContain("dXNlcjpwYXNz");
    }

    @Test
    void writesNoFiles() throws Exception {
        LoggerContext context = configure();
        context.getLogger("org.onap.so.X").error("error");
        context.stop();
        try (var files = Files.list(logsDir)) {
            assertThat(files).isEmpty();
        }
    }
}
