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


import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import org.onap.logging.filter.base.MetricLogClientFilter;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.logger.MdcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


public class SOMetricLogClientFilter {

    protected static Logger logger = LoggerFactory.getLogger(SOMetricLogClientFilter.class);
    private static final Marker INVOKE_RETURN = MarkerFactory.getMarker("INVOKE-RETURN");


    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        try {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(responseContext.getStatus()));
            logger.info(INVOKE_RETURN, "InvokeReturn");
            setOpenStackResponseCode();
        } catch (Exception e) {
            logger.warn("Error in JAX-RS request,response client filter", e);
        }
    }

    protected void setOpenStackResponseCode() {
        if (MDC.get(MdcConstants.OPENSTACK_STATUS_CODE) != null) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, MDC.get(MdcConstants.OPENSTACK_STATUS_CODE));
        }
    }
}
