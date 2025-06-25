/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.common.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * java object of the resource recipe , it will be passed to the Camunda process
 */
@JsonPropertyOrder({"resourceInput", "host", "requestId", "requestAction", "serviceInstanceId", "serviceType",
        "recipeParams"})
@JsonRootName("variables")
public class ResourceRecipeRequest {

    private static Logger logger = LoggerFactory.getLogger(ResourceRecipeRequest.class);
    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
    }

    @JsonProperty("resourceInput")
    private BpmnParam resourceInput;

    @JsonProperty("host")
    private BpmnParam host;

    @JsonProperty("mso-request-id")
    private BpmnParam requestId;

    @JsonProperty("requestAction")
    private BpmnParam requestAction;

    @JsonProperty("serviceInstanceId")
    private BpmnParam serviceInstanceId;

    @JsonProperty("serviceType")
    private BpmnParam serviceType;

    @JsonProperty("recipeParams")
    private BpmnParam recipeParams;

    @JsonProperty("mso-service-request-timeout")
    private BpmnIntegerParam recipeTimeout;

    @JsonProperty("resourceInput")
    public BpmnParam getResourceInput() {
        return resourceInput;
    }

    @JsonProperty("resourceInput")
    public void setResourceInput(BpmnParam resourceInput) {
        this.resourceInput = resourceInput;
    }

    @JsonProperty("host")
    public BpmnParam getHost() {
        return host;
    }

    @JsonProperty("host")
    public void setHost(BpmnParam host) {
        this.host = host;
    }

    @JsonProperty("mso-request-id")
    public BpmnParam getRequestId() {
        return requestId;
    }

    @JsonProperty("mso-request-id")
    public void setRequestId(BpmnParam requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("requestAction")
    public BpmnParam getRequestAction() {
        return requestAction;
    }

    @JsonProperty("requestAction")
    public void setRequestAction(BpmnParam requestAction) {
        this.requestAction = requestAction;
    }

    @JsonProperty("serviceInstanceId")
    public BpmnParam getServiceInstanceId() {
        return serviceInstanceId;
    }

    @JsonProperty("serviceInstanceId")
    public void setServiceInstanceId(BpmnParam serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    @JsonProperty("serviceType")
    public BpmnParam getServiceType() {
        return serviceType;
    }

    @JsonProperty("serviceType")
    public void setServiceType(BpmnParam serviceType) {
        this.serviceType = serviceType;
    }

    @JsonProperty("recipeParams")
    public BpmnParam getRecipeParams() {
        return recipeParams;
    }

    @JsonProperty("recipeParams")
    public void setRecipeParams(BpmnParam recipeParams) {
        this.recipeParams = recipeParams;
    }

    @JsonProperty("mso-service-request-timeout")
    public BpmnIntegerParam getRecipeTimeout() {
        return recipeTimeout;
    }

    @JsonProperty("mso-service-request-timeout")
    public void setRecipeTimeout(BpmnIntegerParam recipeTimeout) {
        this.recipeTimeout = recipeTimeout;
    }

    @Override
    public String toString() {
        String jsonStr = "ResourceRecipeRequest";
        try {
            jsonStr = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException", e);
        }
        return jsonStr;
    }
}
