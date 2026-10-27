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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class BasicAuth {

    public static final String SO_USERNAME_PROPERTY = "so.auth.username";
    public static final String SO_PASSWORD_PROPERTY = "so.auth.password";
    public static final String AAI_USERNAME_PROPERTY = "aai.username";
    public static final String AAI_PASSWORD_PROPERTY = "aai.password";

    private BasicAuth() {}

    /**
     * @return {@code username:password}, or {@code null} when no username is configured
     */
    public static String credentials(String username, String password) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return username + ":" + (password == null ? "" : password);
    }

    /**
     * @return the value of an {@code Authorization} header, or {@code null} when no username is configured
     */
    public static String header(String username, String password) {
        String credentials = credentials(username, password);
        if (credentials == null) {
            return null;
        }
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
