/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Intel Corp.  All rights reserved.
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

package org.onap.so.client.oof.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"globalSubscriberId", "subscriberName", "subscriberCommonSiteId"})
@JsonRootName("subscriberInfo")
public class SubscriberInfo implements Serializable {

    private static final long serialVersionUID = -759180997599143791L;

    @JsonProperty("globalSubscriberId")
    private String globalSubscriberId;
    @JsonProperty("subscriberName")
    private String subscriberName;
    @JsonProperty("subscriberCommonSiteId")
    private String subscriberCommonSiteId;

    @JsonProperty("globalSubscriberId")
    public String getGlobalSubscriberId() {
        return globalSubscriberId;
    }

    @JsonProperty("globalSubscriberId")
    public void setGlobalSubscriberId(String globalSubscriberId) {
        this.globalSubscriberId = globalSubscriberId;
    }

    @JsonProperty("subscriberName")
    public String getSubscriberName() {
        return subscriberName;
    }

    @JsonProperty("subscriberName")
    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    @JsonProperty("subscriberCommonSiteId")
    public String getSubscriberCommonSiteId() {
        return subscriberCommonSiteId;
    }

    @JsonProperty("subscriberCommonSiteId")
    public void setSubscriberCommonSiteId(String subscriberCommonSiteId) {
        this.subscriberCommonSiteId = subscriberCommonSiteId;
    }
}
