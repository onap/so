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

package org.onap.so.adapters.vevnfm.service;

import java.util.List;
import java.util.Map;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AaiConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(AaiConnectionService.class);

    @Value("${aai.url}")
    private String aaiUrl;

    @Value("${aai.path}")
    private String aaiPath;

    @Autowired
    private HttpRestServiceProvider restProvider;

    public Map receiveVnfm() {
        final ResponseEntity<List> response = restProvider.getHttpResponse(getUrl(), List.class);

        final HttpStatus statusCode = response.getStatusCode();
        final List body = response.getBody();

        logger.info("The VNFM replied with the code {} and the body {}", statusCode, body);

        return (Map) body.get(0);
    }

    private String getUrl() {
        return aaiUrl + aaiPath;
    }
}
