/*-
 * ============LICENSE_START=======================================================
 * ONAP - Logging
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logging.filter.base;

import org.aspectj.lang.ProceedingJoinPoint;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;
import java.util.UUID;

public abstract class AbstractMDCSetupAspect extends MDCSetup {

    public abstract void logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable;

    protected void setupMDC(String methodName) {
        try {
            setEntryTimeStamp();
            setServerFQDN();
            String partnerName = getProperty(Constants.Property.PARTNER_NAME);
            MDC.put(ONAPLogConstants.MDCs.INVOCATION_ID, UUID.randomUUID().toString());
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.INPROGRESS.toString());
            MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, UUID.randomUUID().toString());
            MDC.put(ONAPLogConstants.MDCs.TARGET_ENTITY, partnerName);
            MDC.put(ONAPLogConstants.MDCs.TARGET_SERVICE_NAME, Constants.DefaultValues.UNKNOWN);
            MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, methodName);
            setLogTimestamp();
            setElapsedTime();
            MDC.put(ONAPLogConstants.MDCs.PARTNER_NAME, partnerName);
            logger.info(ONAPLogConstants.Markers.ENTRY, "Entering");
        } catch (Exception e) {
            logger.warn("Error in ScheduledTasksMDCSetup: {}", e.getMessage());
        }
    }

    protected void exitAndClearMDC() {
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

    protected void setStatusCode() {
        String currentStatusCode = MDC.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE);
        if (currentStatusCode == null || !currentStatusCode.equals(ONAPLogConstants.ResponseStatus.ERROR.toString())) {
            MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.COMPLETE.toString());
        }
    }

    public void errorMDCSetup(ErrorCode errorCode, String errorDescription) {
        MDC.put(ONAPLogConstants.MDCs.ERROR_CODE, String.valueOf(errorCode.getValue()));
        MDC.put(ONAPLogConstants.MDCs.ERROR_DESC, errorDescription);
        MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.ERROR.toString());
    }

}
