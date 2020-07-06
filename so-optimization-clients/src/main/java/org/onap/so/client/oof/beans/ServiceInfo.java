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
@JsonPropertyOrder({"serviceInstanceId", "serviceName", "modelInfo"})
@JsonRootName("serviceInfo")
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = -759180997599143791L;

    @JsonProperty("serviceInstanceId")
    private String serviceInstanceId;
    @JsonProperty("serviceName")
    private String serviceName;
    @JsonProperty("modelInfo")
    private ModelInfo modelInfo;

    @JsonProperty("serviceInstanceId")
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    @JsonProperty("serviceInstanceId")
    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    @JsonProperty("serviceName")
    public String getServiceName() {
        return serviceName;
    }

    @JsonProperty("serviceName")
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @JsonProperty("modelInfo")
    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    @JsonProperty("modelInfo")
    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

}
