/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.aaiclient.spring.compat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import org.onap.aaiclient.client.aai.AAIProperties;
import org.onap.so.utils.CryptoUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Value
public class RestTemplateDefaultHeaderInterceptor implements ClientHttpRequestInterceptor {

    private final AAIProperties aaiProperties;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-FromAppId", aaiProperties.getSystemName());
        headers.add("X-TransactionId", "");

        String auth = aaiProperties.getAuth();
        String key = aaiProperties.getKey();
        if (auth != null && key != null) {
            String base64Auth = decrypt(auth, key);
            headers.add("Authorization", "Basic " + base64Auth);
        }

        return execution.execute(request, body);

    }

    private String decrypt(String auth, String key) {
        try {
            byte[] decryptedAuth = CryptoUtils.decrypt(auth, key).getBytes();
            return Base64.getEncoder().encodeToString(decryptedAuth);
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException("AAI auth could not be decoded", e);
        }
    }
}
