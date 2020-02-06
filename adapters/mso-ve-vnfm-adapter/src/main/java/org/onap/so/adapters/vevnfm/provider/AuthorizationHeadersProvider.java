/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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

package org.onap.so.adapters.vevnfm.provider;

import java.util.List;
import org.apache.logging.log4j.util.Strings;
import org.onap.so.configuration.rest.BasicHttpHeadersProvider;
import org.springframework.http.HttpHeaders;

public class AuthorizationHeadersProvider extends BasicHttpHeadersProvider {

    private List<String> previousAuthorization;

    public void addAuthorization(final String authorization) {
        final HttpHeaders headers = getHttpHeaders();
        previousAuthorization = headers.get(AUTHORIZATION_HEADER);
        headers.set(AUTHORIZATION_HEADER, authorization);
    }

    public void resetPrevious() {
        if (!isPreviousAuthorizationBlank()) {
            getHttpHeaders().addAll(AUTHORIZATION_HEADER, previousAuthorization);
        }
    }

    private boolean isPreviousAuthorizationBlank() {
        return previousAuthorization == null || previousAuthorization.isEmpty()
                || Strings.isBlank(previousAuthorization.get(0));
    }
}
