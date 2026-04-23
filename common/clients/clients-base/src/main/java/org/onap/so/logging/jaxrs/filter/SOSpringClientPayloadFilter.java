/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2024 AT&T Intellectual Property. All rights reserved.
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

import java.io.IOException;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;

/**
 * Spring 6 compatible override of SpringClientPayloadFilter. The base class was compiled against Spring 5 where
 * ClientHttpResponse.getStatusCode() returned HttpStatus. In Spring 6 it returns HttpStatusCode, causing
 * NoSuchMethodError.
 */
public class SOSpringClientPayloadFilter extends SpringClientPayloadFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void logResponse(ClientHttpResponse response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("============================response begin==========================================");
            logger.debug("Status code  : {}", response.getStatusCode());
            logger.debug("Status text  : {}", response.getStatusText());
            logger.debug("Headers      : {}", response.getHeaders());
            logger.debug("============================response end============================================");
        }
    }
}
