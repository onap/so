/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.logging.cxf.interceptor;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.onap.logging.filter.base.MDCSetup;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.onap.so.logging.cxf.interceptor.MDCSetup;

public class SOAPLoggingOutInterceptor extends AbstractSoapInterceptor {

    private static final String _500 = "500";

    protected static Logger logger = LoggerFactory.getLogger(SOAPLoggingOutInterceptor.class);



    public SOAPLoggingOutInterceptor() {
        super(Phase.WRITE);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        try {
            MDCSetup mdcSetup = new MDCSetup();
            Exception ex = message.getContent(Exception.class);
            if (ex == null) {
                MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE,
                        ONAPLogConstants.ResponseStatus.COMPLETE.toString());
            } else {
                int responseCode = 0;
                responseCode = (int) message.get(Message.RESPONSE_CODE);
                if (responseCode != 0)
                    MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, String.valueOf(responseCode));
                else
                    MDC.put(ONAPLogConstants.MDCs.RESPONSE_CODE, _500);

                MDC.put(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE, ONAPLogConstants.ResponseStatus.ERROR.toString());
            }
            mdcSetup.setLogTimestamp();
            mdcSetup.setElapsedTime();
            logger.info(ONAPLogConstants.Markers.EXIT, "Exiting");
            mdcSetup.setLogTimestamp();
            mdcSetup.setElapsedTime();
        } catch (Exception e) {
            logger.warn("Error in incoming SOAP Message Inteceptor", e);
        }
    }
}
