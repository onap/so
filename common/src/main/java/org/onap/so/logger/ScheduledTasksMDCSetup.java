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

package org.onap.so.logger;

import java.util.UUID;
import org.onap.logging.filter.base.Constants;
import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.filter.base.ONAPComponentsList;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasksMDCSetup extends MDCSetup {

    public void mdcSetup(ONAPComponentsList targetEntity, String serviceName) {
        try {
            setEntryTimeStamp();
            setServerFQDN();
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, UUID.randomUUID().toString());
            MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, targetEntity.toString());
            MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, Constants.DefaultValues.UNKNOWN);
            MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, serviceName);
            setLogTimestamp();
            setElapsedTime();
            MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, getProperty(Constants.Property.PARTNER_NAME));
            logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
        } catch (Exception e) {
            logger.warn("Error in ScheduledTasksMDCSetup mdcSetup: {}", e.getMessage());
        }
    }

    public void errorMDCSetup(ErrorCode errorCode, String errorDescription) {
        MDC.put(ONAPLogConstants.MDCs.ERROR_CODE, String.valueOf(errorCode.getValue()));
        MDC.put(ONAPLogConstants.MDCs.ERROR_DESC, errorDescription);
    }

    public void exitAndClearMDC() {
        try {
            setStatusCode();
            setLogTimestamp();
            setElapsedTime();
            logger.info(ONAPLogConstants.Markers.EXIT, "Exiting.");
        } catch (Exception e) {
            logger.warn("Error in ScheduledTasksMDCSetup clear MDC: {}", e.getMessage());
        }
        MDC.clear();
    }

    public void setStatusCode() {
        String currentStatusCode = MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
        if (currentStatusCode == null || !currentStatusCode.equals(ONAPLogConstants.ResponseStatus.ERROR.toString())) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.COMPLETE.toString());
        }
    }
}
