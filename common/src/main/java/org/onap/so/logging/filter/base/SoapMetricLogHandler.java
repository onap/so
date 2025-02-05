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

import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

public class SoapMetricLogHandler extends AbstractBaseMetricLogFilter<SOAPMessageContext, SOAPMessageContext>
        implements SOAPHandler<SOAPMessageContext> {

    @Override
    protected int getHttpStatusCode(SOAPMessageContext ctx) {
        return (Integer) ctx.get(MessageContext.HTTP_RESPONSE_CODE);
    }

    @Override
    protected String getResponseCode(SOAPMessageContext ctx) {
        Integer responseCode = (Integer) ctx.get(MessageContext.HTTP_RESPONSE_CODE);
        return String.valueOf(responseCode);
    }

    @Override
    protected String getTargetEntity(SOAPMessageContext ctx) {
        return Constants.DefaultValues.UNKNOWN_TARGET_ENTITY;
    }

    @Override
    protected String getTargetServiceName(SOAPMessageContext ctx) {
        QName svc = (QName) ctx.get(SOAPMessageContext.WSDL_SERVICE);
        QName op = (QName) ctx.get(SOAPMessageContext.WSDL_OPERATION);
        return svc.getLocalPart() + ":" + op.getLocalPart();
    }

    @Override
    public void close(MessageContext context) {
        // pass
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        logMessage(context);
        return true;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        logMessage(context);
        return true;
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    private void logMessage(SOAPMessageContext context) {
        boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {
            this.pre(context);
        } else {
            this.post(context, context);
        }
    }

}
