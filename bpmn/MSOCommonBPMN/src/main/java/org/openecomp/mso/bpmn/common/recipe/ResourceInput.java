/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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
package org.openecomp.mso.bpmn.common.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * the inputs for the resource recipe
 * <br>
 * <p>
 * </p>
 * 
 * @author
 * @version     ONAP Beijing Release  2018-03-08
 */
@JsonPropertyOrder({"resourceInstanceName", "resourceInstanceDes", "globalSubscriberId", "serviceType", "serviceId", "operationId", "resourceType","resourceCustomizationUuid"})
@JsonRootName("variables")
public class ResourceInput {

    @JsonProperty("resourceInstanceName")
    private String resourceInstanceName;
    
    @JsonProperty("resourceInstanceDes")
    private String resourceInstanceDes;
    
    @JsonProperty("globalSubscriberId")
    private String globalSubscriberId;
    
    @JsonProperty("serviceType")
    private String serviceType;
    
    @JsonProperty("serviceInstanceId")
    private String serviceInstanceId;
    
    @JsonProperty("operationId")
    private String operationId;
    
    @JsonProperty("resourceType")
    private String resourceType;
    
    //for create resource
    @JsonProperty("resourceCustomizationUuid")
    private String resourceCustomizationUuid;
    
    //for delete resource
    @JsonProperty("resourceInstancenUuid")
    private String resourceInstancenUuid;
    
    @JsonProperty("resourceParameters")
    private String resourceParameters;
    
    @JsonProperty("operationType")
    private String operationType;

    
    /**
     * @return Returns the resourceInstanceName.
     */
    @JsonProperty("resourceInstanceName")
    public String getResourceInstanceName() {
        return resourceInstanceName;
    }

    
    /**
     * @param resourceInstanceName The resourceInstanceName to set.
     */
    @JsonProperty("resourceInstanceName")
    public void setResourceInstanceName(String resourceInstanceName) {
        this.resourceInstanceName = resourceInstanceName;
    }

    
    /**
     * @return Returns the resourceInstanceDes.
     */
    @JsonProperty("resourceInstanceDes")
    public String getResourceInstanceDes() {
        return resourceInstanceDes;
    }

    
    /**
     * @param resourceInstanceDes The resourceInstanceDes to set.
     */
    @JsonProperty("resourceInstanceDes")
    public void setResourceInstanceDes(String resourceInstanceDes) {
        this.resourceInstanceDes = resourceInstanceDes;
    }

    
    /**
     * @return Returns the globalSubscriberId.
     */
    @JsonProperty("globalSubscriberId")
    public String getGlobalSubscriberId() {
        return globalSubscriberId;
    }

    
    /**
     * @param globalSubscriberId The globalSubscriberId to set.
     */
    @JsonProperty("globalSubscriberId")
    public void setGlobalSubscriberId(String globalSubscriberId) {
        this.globalSubscriberId = globalSubscriberId;
    }

    
    /**
     * @return Returns the serviceType.
     */
    @JsonProperty("serviceType")
    public String getServiceType() {
        return serviceType;
    }

    
    /**
     * @param serviceType The serviceType to set.
     */
    @JsonProperty("serviceType")
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    
    /**
     * @return Returns the serviceId.
     */
    @JsonProperty("serviceInstanceId")
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    
    /**
     * @param serviceId The serviceId to set.
     */
    @JsonProperty("serviceInstanceId")
    public void setServiceInstanceId(String serviceId) {
        this.serviceInstanceId = serviceId;
    }

    
    /**
     * @return Returns the operationId.
     */
    @JsonProperty("operationId")
    public String getOperationId() {
        return operationId;
    }

    
    /**
     * @param operationId The operationId to set.
     */
    @JsonProperty("operationId")
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    
    /**
     * @return Returns the resourceType.
     */
    @JsonProperty("resourceType")
    public String getResourceType() {
        return resourceType;
    }

    
    /**
     * @param resourceType The resourceType to set.
     */
    @JsonProperty("resourceType")
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    
    /**
     * @return Returns the resourceCustomizationUuid.
     */
    @JsonProperty("resourceCustomizationUuid")
    public String getResourceCustomizationUuid() {
        return resourceCustomizationUuid;
    }

    
    /**
     * @param resourceCustomizationUuid The resourceCustomizationUuid to set.
     */
    @JsonProperty("resourceCustomizationUuid")
    public void setResourceCustomizationUuid(String resourceCustomizationUuid) {
        this.resourceCustomizationUuid = resourceCustomizationUuid;
    }

    
    /**
     * @return Returns the resourceParameters.
     */
    @JsonProperty("resourceParameters")
    public String getResourceParameters() {
        return resourceParameters;
    }

    
    /**
     * @param resourceParameters The resourceParameters to set.
     */
    @JsonProperty("resourceParameters")
    public void setResourceParameters(String resourceParameters) {
        this.resourceParameters = resourceParameters;
    }

    
    /**
     * @return Returns the operationType.
     */
    @JsonProperty("operationType")
    public String getOperationType() {
        return operationType;
    }

    
    /**
     * @param operationType The operationType to set.
     */
    @JsonProperty("operationType")
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }


    
    /**
     * @return Returns the resourceInstancenUuid.
     */
    @JsonProperty("resourceInstancenUuid")
    public String getResourceInstancenUuid() {
        return resourceInstancenUuid;
    }


    
    /**
     * @param resourceInstancenUuid The resourceInstancenUuid to set.
     */
    @JsonProperty("resourceInstancenUuid")
    public void setResourceInstancenUuid(String resourceInstancenUuid) {
        this.resourceInstancenUuid = resourceInstancenUuid;
    }
    
    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        String jsonStr = "";
        try {
            jsonStr = mapper.writeValueAsString(this);
        } catch(JsonProcessingException e) {

            e.printStackTrace();
        }
        return jsonStr;
    }
}
