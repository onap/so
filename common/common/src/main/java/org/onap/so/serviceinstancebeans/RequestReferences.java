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

package org.onap.so.serviceinstancebeans;

import java.net.URL;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonRootName(value = "requestReferences")
@JsonInclude(Include.NON_DEFAULT)
public class RequestReferences {

    String requestId;
    String instanceId;
    URL requestSelfLink;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public URL getRequestSelfLink() {
        return requestSelfLink;
    }

    public void setRequestSelfLink(URL requestSelfLink) {
        this.requestSelfLink = requestSelfLink;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("requestId", requestId).append("instanceId", instanceId)
                .append("requestSelfLink", requestSelfLink).toString();
    }


}
