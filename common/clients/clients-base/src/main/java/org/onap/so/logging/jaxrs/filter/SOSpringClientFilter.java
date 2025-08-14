/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import org.onap.logging.filter.spring.SpringClientFilter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.MdcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class SOSpringClientFilter extends SpringClientFilter implements ClientHttpRequestInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void post(HttpRequest request, ClientHttpResponse response) {
        setLogTimestamp();
        setElapsedTimeInvokeTimestamp();
        try {
            setResponseStatusCode(response.getRawStatusCode());
            int statusCode = response.getRawStatusCode();
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(statusCode));
            setResponseDescription(statusCode);
        } catch (IOException e) {
            logger.error("Unable to get statusCode from response", e);
        }


        clearClientMDCs();
        setOpenStackResponseCode();
    }

    protected void setOpenStackResponseCode() {
        if (MDC.get(MdcConstants.OPENSTACK_STATUS_CODE) != null) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, MDC.get(MdcConstants.OPENSTACK_STATUS_CODE));
        }
    }
}
