/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandler.camundabeans;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.onap.so.apihandler.common.CommonConstants;

/**
 * POJO which encapsulates the fields required to create a JSON request to invoke generic macro BPEL.
 */
@JsonPropertyOrder({CommonConstants.G_REQUEST_ID, CommonConstants.G_ACTION})
@JsonRootName(CommonConstants.CAMUNDA_ROOT_INPUT)
public class CamundaMacroRequest {

    @JsonProperty(CommonConstants.G_REQUEST_ID)
    private CamundaInput requestId;

    @JsonProperty(CommonConstants.G_ACTION)
    private CamundaInput action;

    @JsonProperty(CommonConstants.G_SERVICEINSTANCEID)
    private CamundaInput serviceInstanceId;


    /**
     * Sets new requestId.
     *
     * @param requestId New value of requestId.
     */
    public void setRequestId(CamundaInput requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets action.
     *
     * @return Value of action.
     */
    public CamundaInput getAction() {
        return action;
    }

    /**
     * Sets new action.
     *
     * @param action New value of action.
     */
    public void setAction(CamundaInput action) {
        this.action = action;
    }

    /**
     * Gets requestId.
     *
     * @return Value of requestId.
     */
    public CamundaInput getRequestId() {
        return requestId;
    }

    /**
     * Sets new serviceInstanceId.
     *
     * @param serviceInstanceId New value of serviceInstanceId.
     */
    public void setServiceInstanceId(CamundaInput serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    /**
     * Gets serviceInstanceId.
     *
     * @return Value of serviceInstanceId.
     */
    public CamundaInput getServiceInstanceId() {
        return serviceInstanceId;
    }

    @Override
    public String toString() {
        return "CamundaMacroRequest{" + "requestId=" + requestId + ", action=" + action + ", serviceInstanceId="
                + serviceInstanceId + '}';
    }
}
