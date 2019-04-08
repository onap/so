/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.sdnc.impl;

public class RequestTunables {



    public static final String GENERATED_KEY = "Generated key: ";

    // criteria
    private String reqId = "";
    private String msoAction = "";
    private String operation = "";
    private String action = "";

    // tunables
    private String reqMethod = "POST";
    private String sdncUrl = null;
    private String timeout = "60000";
    private String headerName = "sdnc-request-header";
    private String namespace = "";
    private String asyncInd = "N"; // future use

    private String sdncaNotificationUrl = null;

    public RequestTunables(String reqId, String msoAction, String operation, String action) {
        super();
        if (reqId != null) {
            this.reqId = reqId;
        }
        if (msoAction != null) {
            this.msoAction = msoAction;
        }
        if (operation != null) {
            this.operation = operation;
        }
        if (action != null) {
            this.action = action;
        }
    }

    public RequestTunables(RequestTunables original) {
        this.reqId = original.reqId;
        this.action = original.action;
        this.msoAction = original.msoAction;
        this.operation = original.operation;
        this.reqMethod = original.reqMethod;
        this.sdncUrl = original.sdncUrl;
        this.timeout = original.timeout;
        this.headerName = original.headerName;
        this.namespace = original.namespace;
        this.asyncInd = original.asyncInd;
        this.sdncaNotificationUrl = original.sdncaNotificationUrl;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getReqMethod() {
        return reqMethod;
    }

    public void setReqMethod(String reqMethod) {
        this.reqMethod = reqMethod;
    }

    public String getMsoAction() {
        return msoAction;
    }

    public void setMsoAction(String msoAction) {
        this.msoAction = msoAction;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getSdncUrl() {
        return sdncUrl;
    }

    public void setSdncUrl(String sdncUrl) {
        this.sdncUrl = sdncUrl;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getAsyncInd() {
        return asyncInd;
    }

    public void setAsyncInd(String asyncInd) {
        this.asyncInd = asyncInd;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }


    public String getSdncaNotificationUrl() {
        return sdncaNotificationUrl;
    }

    public void setSdncaNotificationUrl(String sdncaNotificationUrl) {
        this.sdncaNotificationUrl = sdncaNotificationUrl;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return "RequestTunables [reqId=" + reqId + ", msoAction=" + msoAction + ", operation=" + operation + ", action="
                + action + ", reqMethod=" + reqMethod + ", sdncUrl=" + sdncUrl + ", timeout=" + timeout
                + ", headerName=" + headerName + ", sdncaNotificationUrl=" + sdncaNotificationUrl + ", namespace="
                + namespace + "]";
    }

}
