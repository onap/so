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

import java.io.Serializable;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"requestParameters", "subscriberInfo", "placementDemands"})
@JsonRootName("placementInfo")
public class PlacementInfo implements Serializable {

    private static final long serialVersionUID = -759180997599143791L;

    @JsonProperty("requestParameters")
    private OofRequestParameters requestParameters;
    @JsonProperty("subscriberInfo")
    private SubscriberInfo subscriberInfo;
    @JsonProperty("placementDemands")
    private ArrayList<PlacementDemand> placementDemands = new ArrayList<>();

    @JsonProperty("requestParameters")
    public OofRequestParameters getRequestParameters() {
        return requestParameters;
    }

    @JsonProperty("requestParameters")
    public void setRequestParameters(OofRequestParameters requestParameters) {
        this.requestParameters = requestParameters;
    }

    @JsonProperty("subscriberInfo")
    public SubscriberInfo getSubscriberInfo() {
        return subscriberInfo;
    }

    @JsonProperty("subscriberInfo")
    public void setSubscriberInfo(SubscriberInfo subscriberInfo) {
        this.subscriberInfo = subscriberInfo;
    }

    @JsonProperty("placementDemands")
    public ArrayList<PlacementDemand> getPlacementDemands() {
        return placementDemands;
    }

    @JsonProperty("placementDemands")
    public void setPlacementDemands(ArrayList<PlacementDemand> placementDemands) {
        this.placementDemands = placementDemands;
    }

}
