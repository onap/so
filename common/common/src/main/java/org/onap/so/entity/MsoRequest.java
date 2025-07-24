/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.entity;


import java.io.Serializable;

/**
 * This simple bean holds tracking information for MSO requests within the adapters. This tracking information should be
 * added to logs, metrics, alarms as appropriate.
 * 
 *
 */
public class MsoRequest implements Serializable {
    private static final long serialVersionUID = 1797142528913733469L;
    private String requestId;
    private String serviceInstanceId;

    public MsoRequest() {
        this.requestId = null;
        this.serviceInstanceId = null;
    }

    public MsoRequest(String r, String s) {
        this.requestId = r;
        this.serviceInstanceId = s;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }
}
