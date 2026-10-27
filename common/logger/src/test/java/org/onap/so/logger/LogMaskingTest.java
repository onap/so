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
import org.junit.jupiter.api.Test;

class LogMaskingTest {

    @Test
    void masksSpringHttpHeadersToString() {
        String in =
                "Headers      : [traceparent:\"00-be2c-01\", Authorization:\"Basic dXNlcjpwYXNz\", Accept:\"application/json\"]";
        assertThat(LogMasking.mask(in)).isEqualTo(
                "Headers      : [traceparent:\"00-be2c-01\", Authorization:\"Basic ***REDACTED***\", Accept:\"application/json\"]");
    }

    @Test
    void masksJaxRsMultivaluedMapToString() {
        assertThat(LogMasking.mask("{Authorization=[Bearer eyJhbGciOi.x.y], X-ONAP-PartnerName=[SO]}"))
                .isEqualTo("{Authorization=[Bearer ***REDACTED***], X-ONAP-PartnerName=[SO]}");
    }

    @Test
    void masksCaseInsensitivelyAndProxyAuthorization() {
        assertThat(LogMasking.mask("authorization: basic abc")).isEqualTo("authorization: basic ***REDACTED***");
        assertThat(LogMasking.mask("Proxy-Authorization: Basic abc"))
                .isEqualTo("Proxy-Authorization: Basic ***REDACTED***");
    }

    @Test
    void masksJsonPasswordValues() {
        assertThat(LogMasking
                .mask("{\"auth\":{\"passwordCredentials\":{\"username\":\"u\",\"password\":\"s3cr\\\"et\"}}}"))
                        .doesNotContain("s3cr");
        assertThat(LogMasking.mask("{\"password\" : \"pw\"}")).isEqualTo("{\"password\" : \"***REDACTED***\"}");
    }

    @Test
    void leavesOrdinaryTextAndNullAlone() {
        assertThat(LogMasking.mask("Status code  : 200")).isEqualTo("Status code  : 200");
        assertThat(LogMasking.mask(null)).isNull();
        assertThat(LogMasking.mask("")).isEmpty();
    }
}
