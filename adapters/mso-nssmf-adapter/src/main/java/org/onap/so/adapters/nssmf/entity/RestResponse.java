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

package org.onap.so.adapters.nssmf.entity;

import java.util.Map;

public class RestResponse {

    // the response content
    private String responseContent;

    // the response status
    private int status;

    // the response header
    private Map<String, String> respHeaderMap;

    public RestResponse() {
        this.status = -1;

        this.respHeaderMap = null;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, String> getRespHeaderMap() {
        return this.respHeaderMap;
    }

    public void setRespHeaderMap(Map<String, String> header) {
        this.respHeaderMap = header;
    }

    public int getRespHeaderInt(String key) {
        if (this.respHeaderMap != null) {
            String result = this.respHeaderMap.get(key);
            if (result != null) {
                return Integer.parseInt(result);
            }
        }
        return -1;
    }

    public long getRespHeaderLong(String key) {
        if (this.respHeaderMap != null) {
            String result = this.respHeaderMap.get(key);
            if (result != null) {
                return Long.parseLong(result);
            }
        }
        return -1L;
    }

    public String getRespHeaderStr(String key) {
        if (this.respHeaderMap != null) {
            return this.respHeaderMap.get(key);
        }
        return null;
    }

    public String getResponseContent() {
        return this.responseContent;
    }

    public void setResponseContent(String responseString) {
        this.responseContent = responseString;
    }
}
