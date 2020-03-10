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

package org.onap.so.adapters.sdnc.impl;


public interface Constants {

    public static final String BPEL_REST_URL_PROP = "org.onap.so.adapters.sdnc.rest.bpelurl";
    public static final String BPEL_URL_PROP = "org.onap.so.adapters.sdnc.bpelurl";
    public static final String DEFAULT_BPEL_URL = "http://localhost:8080//active-bpel/services/SDNCAdapterCallbackV1";

    public static final String MY_URL_PROP = "org.onap.so.adapters.sdnc.myurl";
    public static final String SDNC_AUTH_PROP = "org.onap.so.adapters.sdnc.sdncauth";
    public static final String BPEL_AUTH_PROP = "org.onap.so.adapters.sdnc.bpelauth";


    public static final String SDNC_SVCCFGRESP_ROOT = "input";
    public static final String SDNC_REQ_ID = "/svc-request-id";
    public static final String SDNC_RESP_CODE = "/response-code";
    public static final String SDNC_RESP_MSG = "/response-message";
    public static final String SDNC_CONNECTTIME_PROP = "org.onap.so.adapters.sdnc.sdncconnecttime";
    public static final String ENCRYPTION_KEY_PROP = "org.onap.so.adapters.network.encryptionKey";

    public static final String REQUEST_TUNABLES = "org.onap.so.adapters.sdnc";

    public static final String MSO_ACTION_LCM = "lcm";
    public static final String MSO_ACTION_MDONS = "opticalservice";
    public static final String LCM_API_VER = "2.00";
    public static final int LCM_FLAGS_TTL = 65000;
    public static final String LCM_ORIGINATOR_ID = "MSO";
}
