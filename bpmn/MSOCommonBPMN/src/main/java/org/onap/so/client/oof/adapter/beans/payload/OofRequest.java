/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited. All rights reserved.
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
package org.onap.so.client.oof.adapter.beans.payload;

public class OofRequest {

    private String apiPath;

    private Object requestDetails;

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public Object getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(Object requestDetails) {
        this.requestDetails = requestDetails;
    }

    @Override
    public String toString() {
        return "OofRequest [apiPath=" + apiPath + ", requestDetails=" + requestDetails + "]";
    }

}
