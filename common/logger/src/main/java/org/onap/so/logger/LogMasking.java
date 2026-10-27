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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogMasking {

    public static final String REDACTED = "***REDACTED***";

    private static final Pattern CREDENTIAL = Pattern
            .compile("(?i)((?:proxy-)?authorization\"?\\s*[:=]\\s*\\[?\\s*\"?(?:basic|bearer)\\s+)([^\\s\",\\]]+)");
    private static final Pattern PASSWORD = Pattern.compile("(\"password\"\\s*:\\s*\")((?:[^\"\\\\]|\\\\.)*)(\")");
    private static final String CREDENTIAL_REPLACEMENT = "$1" + Matcher.quoteReplacement(REDACTED);
    private static final String PASSWORD_REPLACEMENT = "$1" + Matcher.quoteReplacement(REDACTED) + "$3";

    private LogMasking() {}

    public static String mask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String masked = CREDENTIAL.matcher(text).replaceAll(CREDENTIAL_REPLACEMENT);
        return PASSWORD.matcher(masked).replaceAll(PASSWORD_REPLACEMENT);
    }
}
