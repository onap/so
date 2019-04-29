/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CloudRequestData {

    Object cloudRequest;
    String cloudIdentifier;

    public CloudRequestData() {}

    public CloudRequestData(Object cloudRequest, String cloudIdentifier) {
        this.cloudRequest = cloudRequest;
        this.cloudIdentifier = cloudIdentifier;
    }

    public Object getCloudRequest() {
        return cloudRequest;
    }

    public void setCloudRequest(Object cloudRequest) {
        this.cloudRequest = cloudRequest;
    }

    public String getCloudIdentifier() {
        return cloudIdentifier;
    }

    public void setCloudIdentifier(String cloudIdentifier) {
        this.cloudIdentifier = cloudIdentifier;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("cloudRequest", cloudRequest).append("cloudIdentifier", cloudIdentifier)
                .toString();
    }
}
