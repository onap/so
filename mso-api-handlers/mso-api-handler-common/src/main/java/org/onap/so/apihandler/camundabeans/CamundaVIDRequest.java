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

package org.onap.so.apihandler.camundabeans;


import org.onap.so.apihandler.common.CommonConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * JavaBean JSON class for a "variables" which contains the JSON payload that will be passed to the Camunda process
 */
@JsonPropertyOrder({CommonConstants.CAMUNDA_SERVICE_INPUT, CommonConstants.REQUEST_ID_VARIABLE,
        CommonConstants.REQUEST_ID_HEADER, CommonConstants.IS_BASE_VF_MODULE_VARIABLE,
        CommonConstants.RECIPE_TIMEOUT_VARIABLE, CommonConstants.REQUEST_ACTION_VARIABLE,
        CommonConstants.SERVICE_INSTANCE_ID_VARIABLE, CommonConstants.PNF_CORRELATION_ID,
        CommonConstants.VNF_ID_VARIABLE, CommonConstants.VF_MODULE_ID_VARIABLE,
        CommonConstants.VOLUME_GROUP_ID_VARIABLE, CommonConstants.NETWORK_ID_VARIABLE,
        CommonConstants.CONFIGURATION_ID_VARIABLE, CommonConstants.SERVICE_TYPE_VARIABLE,
        CommonConstants.VNF_TYPE_VARIABLE, CommonConstants.VF_MODULE_TYPE_VARIABLE,
        CommonConstants.NETWORK_TYPE_VARIABLE, CommonConstants.CAMUNDA_SERVICE_INPUT, CommonConstants.RECIPE_PARAMS})

@JsonRootName(CommonConstants.CAMUNDA_ROOT_INPUT)
public class CamundaVIDRequest {

    @JsonProperty(CommonConstants.CAMUNDA_SERVICE_INPUT)
    private CamundaInput serviceInput;

    @JsonProperty(CommonConstants.CAMUNDA_HOST)
    private CamundaInput host;

    @JsonProperty(CommonConstants.REQUEST_ID_VARIABLE)
    private CamundaInput requestId;

    @JsonProperty(CommonConstants.REQUEST_ID_HEADER)
    private CamundaInput msoRequestId;

    @JsonProperty(CommonConstants.IS_BASE_VF_MODULE_VARIABLE)
    private CamundaBooleanInput isBaseVfModule;

    @JsonProperty(CommonConstants.RECIPE_TIMEOUT_VARIABLE)
    private CamundaIntegerInput recipeTimeout;

    @JsonProperty(CommonConstants.REQUEST_ACTION_VARIABLE)
    private CamundaInput requestAction;

    @JsonProperty(CommonConstants.SERVICE_INSTANCE_ID_VARIABLE)
    private CamundaInput serviceInstanceId;
    @JsonProperty(CommonConstants.OPERATION_TYPE)
    private CamundaInput operationType;
    @JsonProperty(CommonConstants.PNF_CORRELATION_ID)
    private CamundaInput pnfCorrelationId;

    @JsonProperty(CommonConstants.VNF_ID_VARIABLE)
    private CamundaInput vnfId;

    @JsonProperty(CommonConstants.VF_MODULE_ID_VARIABLE)
    private CamundaInput vfModuleId;

    @JsonProperty(CommonConstants.VOLUME_GROUP_ID_VARIABLE)
    private CamundaInput volumeGroupId;

    @JsonProperty(CommonConstants.NETWORK_ID_VARIABLE)
    private CamundaInput networkId;

    @JsonProperty(CommonConstants.CONFIGURATION_ID_VARIABLE)
    private CamundaInput configurationId;

    @JsonProperty(CommonConstants.SERVICE_TYPE_VARIABLE)
    private CamundaInput serviceType;

    @JsonProperty(CommonConstants.VNF_TYPE_VARIABLE)
    private CamundaInput vnfType;

    @JsonProperty(CommonConstants.VF_MODULE_TYPE_VARIABLE)
    private CamundaInput vfModuleType;

    @JsonProperty(CommonConstants.NETWORK_TYPE_VARIABLE)
    private CamundaInput networkType;

    @JsonProperty(CommonConstants.RECIPE_PARAMS)
    private CamundaInput recipeParams;

    @JsonProperty(CommonConstants.API_VERSION)
    private CamundaInput apiVersion;

    @JsonProperty(CommonConstants.ALACARTE)
    private CamundaBooleanInput aLaCarte;

    @JsonProperty(CommonConstants.REQUEST_URI)
    private CamundaInput requestUri;

    @JsonProperty(CommonConstants.INSTANCE_GROUP_ID)
    private CamundaInput instanceGroupId;

    @JsonProperty(CommonConstants.CAMUNDA_SERVICE_INPUT)
    public CamundaInput getServiceInput() {
        return serviceInput;
    }

    @JsonProperty(CommonConstants.GENERATE_IDS)
    private CamundaBooleanInput generateIds;

    @JsonProperty(CommonConstants.CAMUNDA_SERVICE_INPUT)
    public void setServiceInput(CamundaInput serviceInput) {
        this.serviceInput = serviceInput;
    }

    @JsonProperty(CommonConstants.CAMUNDA_HOST)
    public CamundaInput getHost() {
        return host;
    }

    @JsonProperty(CommonConstants.CAMUNDA_HOST)
    public void setHost(CamundaInput host) {
        this.host = host;
    }

    @JsonProperty(CommonConstants.REQUEST_ID_VARIABLE)
    public CamundaInput getRequestId() {
        return requestId;
    }

    @JsonProperty(CommonConstants.REQUEST_ID_VARIABLE)
    public void setRequestId(CamundaInput requestId) {
        this.requestId = requestId;
    }

    @JsonProperty(CommonConstants.REQUEST_ID_HEADER)
    public CamundaInput getMsoRequestId() {
        return msoRequestId;
    }

    @JsonProperty(CommonConstants.REQUEST_ID_HEADER)
    public void setMsoRequestId(CamundaInput msoRequestIdp) {
        this.msoRequestId = msoRequestIdp;
    }

    @JsonProperty(CommonConstants.IS_BASE_VF_MODULE_VARIABLE)
    public CamundaBooleanInput getIsBaseVfModule() {
        return isBaseVfModule;
    }

    @JsonProperty(CommonConstants.IS_BASE_VF_MODULE_VARIABLE)
    public void setIsBaseVfModule(CamundaBooleanInput isBaseVfModule) {
        this.isBaseVfModule = isBaseVfModule;
    }

    @JsonProperty(CommonConstants.RECIPE_TIMEOUT_VARIABLE)
    public CamundaIntegerInput getRecipeTimeout() {
        return recipeTimeout;
    }

    @JsonProperty(CommonConstants.RECIPE_TIMEOUT_VARIABLE)
    public void setRecipeTimeout(CamundaIntegerInput recipeTimeout) {
        this.recipeTimeout = recipeTimeout;
    }

    @JsonProperty(CommonConstants.REQUEST_ACTION_VARIABLE)
    public CamundaInput getRequestAction() {
        return requestAction;
    }

    @JsonProperty(CommonConstants.REQUEST_ACTION_VARIABLE)
    public void setRequestAction(CamundaInput requestAction) {
        this.requestAction = requestAction;
    }

    @JsonProperty(CommonConstants.SERVICE_INSTANCE_ID_VARIABLE)
    public CamundaInput getServiceInstanceId() {
        return serviceInstanceId;
    }

    @JsonProperty(CommonConstants.SERVICE_INSTANCE_ID_VARIABLE)
    public void setServiceInstanceId(CamundaInput serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    @JsonProperty(CommonConstants.OPERATION_TYPE)
    public CamundaInput getOperationType() {
        return operationType;
    }

    @JsonProperty(CommonConstants.OPERATION_TYPE)
    public void setOperationType(CamundaInput operationType) {
        this.operationType = operationType;
    }

    @JsonProperty(CommonConstants.PNF_CORRELATION_ID)
    public CamundaInput getPnfCorrelationId() {
        return pnfCorrelationId;
    }

    @JsonProperty(CommonConstants.PNF_CORRELATION_ID)
    public void setPnfCorrelationId(CamundaInput pnfCorrelationId) {
        this.pnfCorrelationId = pnfCorrelationId;
    }

    @JsonProperty(CommonConstants.VNF_ID_VARIABLE)
    public CamundaInput getVnfId() {
        return vnfId;
    }

    @JsonProperty(CommonConstants.VNF_ID_VARIABLE)
    public void setVnfId(CamundaInput vnfId) {
        this.vnfId = vnfId;
    }

    @JsonProperty(CommonConstants.VF_MODULE_ID_VARIABLE)
    public CamundaInput getVfModuleId() {
        return vfModuleId;
    }

    @JsonProperty(CommonConstants.VF_MODULE_ID_VARIABLE)
    public void setVfModuleId(CamundaInput vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    @JsonProperty(CommonConstants.VOLUME_GROUP_ID_VARIABLE)
    public CamundaInput getVolumeGroupId() {
        return volumeGroupId;
    }

    @JsonProperty(CommonConstants.VOLUME_GROUP_ID_VARIABLE)
    public void setVolumeGroupId(CamundaInput volumeGroupId) {
        this.volumeGroupId = volumeGroupId;
    }

    @JsonProperty(CommonConstants.NETWORK_ID_VARIABLE)
    public CamundaInput getNetworkId() {
        return networkId;
    }

    @JsonProperty(CommonConstants.NETWORK_ID_VARIABLE)
    public void setNetworkId(CamundaInput networkId) {
        this.networkId = networkId;
    }

    @JsonProperty(CommonConstants.CONFIGURATION_ID_VARIABLE)
    public CamundaInput getConfigurationId() {
        return configurationId;
    }

    @JsonProperty(CommonConstants.CONFIGURATION_ID_VARIABLE)
    public void setConfigurationId(CamundaInput configurationId) {
        this.configurationId = configurationId;
    }

    @JsonProperty(CommonConstants.SERVICE_TYPE_VARIABLE)
    public CamundaInput getServiceType() {
        return serviceType;
    }

    @JsonProperty(CommonConstants.SERVICE_TYPE_VARIABLE)
    public void setServiceType(CamundaInput serviceType) {
        this.serviceType = serviceType;
    }

    @JsonProperty(CommonConstants.VNF_TYPE_VARIABLE)
    public CamundaInput getVnfType() {
        return vnfType;
    }

    @JsonProperty(CommonConstants.VNF_TYPE_VARIABLE)
    public void setVnfType(CamundaInput vnfType) {
        this.vnfType = vnfType;
    }

    @JsonProperty(CommonConstants.VF_MODULE_TYPE_VARIABLE)
    public CamundaInput getVfModuleType() {
        return vfModuleType;
    }

    @JsonProperty(CommonConstants.VF_MODULE_TYPE_VARIABLE)
    public void setVfModuleType(CamundaInput vfModuleType) {
        this.vfModuleType = vfModuleType;
    }

    @JsonProperty(CommonConstants.NETWORK_TYPE_VARIABLE)
    public CamundaInput getNetworkType() {
        return networkType;
    }

    @JsonProperty(CommonConstants.NETWORK_TYPE_VARIABLE)
    public void setNetworkType(CamundaInput networkType) {
        this.networkType = networkType;
    }

    @JsonProperty(CommonConstants.API_VERSION)
    public CamundaInput getApiVersion() {
        return apiVersion;
    }

    @JsonProperty(CommonConstants.API_VERSION)
    public void setApiVersion(CamundaInput apiVersion) {
        this.apiVersion = apiVersion;
    }

    @JsonProperty(CommonConstants.ALACARTE)
    public CamundaBooleanInput getaLaCarte() {
        return aLaCarte;
    }

    @JsonProperty(CommonConstants.ALACARTE)
    public void setaLaCarte(CamundaBooleanInput aLaCarte) {
        this.aLaCarte = aLaCarte;
    }

    @JsonProperty(CommonConstants.REQUEST_URI)
    public CamundaInput getRequestUri() {
        return requestUri;
    }

    @JsonProperty(CommonConstants.REQUEST_URI)
    public void setRequestUri(CamundaInput requestUri) {
        this.requestUri = requestUri;
    }

    public CamundaInput getRecipeParams() {
        return recipeParams;
    }

    public void setRecipeParams(CamundaInput recipeParams) {
        this.recipeParams = recipeParams;
    }

    @JsonProperty(CommonConstants.INSTANCE_GROUP_ID)
    public void setInstanceGroupId(CamundaInput instanceGroupIdInput) {
        this.instanceGroupId = instanceGroupIdInput;
    }

    @JsonProperty(CommonConstants.INSTANCE_GROUP_ID)
    public CamundaInput getInstanceGroupId() {
        return instanceGroupId;
    }

    @Override
    public String toString() {
        // return "CamundaRequest [requestId=" + + ", host="
        // + host + ", schema=" + schema + ", reqid=" + reqid + ", svcid="
        // + svcid + ", timeout=" + timeout + "]";
        return "CamundaRequest";
    }

    public CamundaBooleanInput getGenerateIds() {
        return generateIds;
    }

    public void setGenerateIds(CamundaBooleanInput generateIds) {
        this.generateIds = generateIds;
    }
}
