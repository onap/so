/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.sniro.beans;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("subscriberInfo")
public class SubscriberInfo implements Serializable {

    private static final long serialVersionUID = -6350949051379748872L;

    @JsonProperty("globalSubscriberId")
    private String globalSubscriberId;
    @JsonProperty("subscriberName")
    private String subscriberName;
    @JsonProperty("subscriberCommonSiteId")
    private String subscriberCommonSiteId;


    public String getGlobalSubscriberId() {
        return globalSubscriberId;
    }

    public void setGlobalSubscriberId(String globalSubscriberId) {
        this.globalSubscriberId = globalSubscriberId;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public String getSubscriberCommonSiteId() {
        return subscriberCommonSiteId;
    }

    public void setSubscriberCommonSiteId(String subscriberCommonSiteId) {
        this.subscriberCommonSiteId = subscriberCommonSiteId;
    }

}
