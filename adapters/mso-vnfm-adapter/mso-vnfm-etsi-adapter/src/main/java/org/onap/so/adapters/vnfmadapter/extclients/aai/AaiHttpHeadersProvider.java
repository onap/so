/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vnfmadapter.extclients.aai;

import java.util.Collections;
import java.util.UUID;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Provides headers required for REST calls to AAI.
 */
public class AaiHttpHeadersProvider extends BasicHttpHeadersProvider {

    public AaiHttpHeadersProvider(final String plainTextBasicAuth) {
        super(plainTextBasicAuth);
    }

    @Override
    public HttpHeaders getHttpHeaders() {
        final HttpHeaders headers = super.getHttpHeaders();
        headers.set("X-FromAppId", "SO");
        String transactionId = MDC.get(ONAPLogConstants.MDCs.REQUEST_ID);
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }
        headers.set("X-TransactionId", transactionId);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

}
