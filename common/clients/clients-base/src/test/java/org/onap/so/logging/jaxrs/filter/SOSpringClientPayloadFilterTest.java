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

package org.onap.so.logging.jaxrs.filter;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.onap.so.logger.LogMasking;
import org.springframework.http.HttpHeaders;

class SOSpringClientPayloadFilterTest {

    @Test
    void redactsCredentialHeadersCaseInsensitively() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("authorization", "Basic dXNlcjpwYXNz");
        headers.add("Proxy-Authorization", "Basic abc");
        headers.add("Cookie", "JSESSIONID=1");
        headers.add("Set-Cookie", "JSESSIONID=1");
        headers.add("X-ONAP-RequestID", "r-1");
        headers.add("X-Auth-Token", "gAAAA-token");
        headers.add("X-Subject-Token", "gAAAA-subject");

        HttpHeaders redacted = SOSpringClientPayloadFilter.redact(headers);

        assertThat(redacted.get("Authorization")).containsExactly(LogMasking.REDACTED);
        assertThat(redacted.get("Proxy-Authorization")).containsExactly(LogMasking.REDACTED);
        assertThat(redacted.get("Cookie")).containsExactly(LogMasking.REDACTED);
        assertThat(redacted.get("Set-Cookie")).containsExactly(LogMasking.REDACTED);
        assertThat(redacted.get("X-ONAP-RequestID")).isEqualTo(List.of("r-1"));
        assertThat(redacted.get("X-Auth-Token")).containsExactly(LogMasking.REDACTED);
        assertThat(redacted.get("X-Subject-Token")).containsExactly(LogMasking.REDACTED);
        assertThat(headers.getFirst("Authorization")).as("original must be untouched").isEqualTo("Basic dXNlcjpwYXNz");
    }

    @Test
    void noMainSourceRegistersTheUnmaskedBaseFilter() throws Exception {
        Path root = Path.of("../../..").toRealPath();
        List<String> offenders = new java.util.ArrayList<>();
        Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        String name = dir.getFileName().toString();
                        if ("target".equals(name) || ".git".equals(name) || "node_modules".equals(name)
                                || ".superpowers".equals(name)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.toString().endsWith(".java") && file.toString().contains("/src/main/java/")) {
                            try {
                                if (Files.readString(file).contains("new SpringClientPayloadFilter(")) {
                                    offenders.add(file.toString());
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
        assertThat(offenders).isEmpty();
    }
}
