/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.client.sdnc.common;

public interface SDNCConstants {
    String SYSTEM_NAME = "MSO";

    String LCM_API_VER = "2.00";

    String LCM_FLAGS_MODE_NORMAL = "NORMAL";
    String LCM_FLAGS_MODE_EXCLUSIVE = "EXCLUSIVE";

    String LCM_FLAGS_FORCE_TRUE = "TRUE";
    String LCM_FLAGS_FORCE_FALSE = "FALSE";

    int LCM_FLAGS_TTL = 65000;

    String LCM_API_BASE_PATH = "/restconf/operations/LCM:";

    String LCM_DMAAP_MSG_VER = "1.0";
    String LCM_DMAAP_MSG_TYPE_REQUEST = "request";
    String LCM_DMAAP_MSG_TYPE_RESPONSE = "response";

    String LCM_DMAAP_PARTITION = "MSOLCM";
    String LCM_DMAAP_TIMEOUT = "20000";
    String LCM_DMAAP_ENVIRONMENT = "TEST";
    String LCM_DMAAP_READ_TOPIC = "SDNC-LCM-WRITE";
    String LCM_DMAAP_WRITE_TOPIC = "SDNC-LCM-READ";

    long LCM_ACTION_TIMEOUT = 300000; // Millisecond
    int LCM_OUTPUT_SUCCESS_CODE = 400;
}
