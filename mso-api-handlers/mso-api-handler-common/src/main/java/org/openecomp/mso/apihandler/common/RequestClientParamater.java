/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.apihandler.common;

public class RequestClientParamater {

    private String requestId;
    private boolean isBaseVfModule;
    private int recipeTimeout;
    private String requestAction;
    private String serviceInstanceId;
    private String vnfId;
    private String vfModuleId;
    private String volumeGroupId;
    private String networkId;
    private String configurationId;
    private String serviceType;
    private String vnfType;
    private String vfModuleType;
    private String networkType;
    private String correlationId;
    private String requestDetails;
    private String recipeParamXsd;

    private RequestClientParamater(Builder builder) {
        requestId = builder.requestId;
        isBaseVfModule = builder.isBaseVfModule;
        recipeTimeout = builder.recipeTimeout;
        requestAction = builder.requestAction;
        serviceInstanceId = builder.serviceInstanceId;
        vnfId = builder.vnfId;
        vfModuleId = builder.vfModuleId;
        volumeGroupId = builder.volumeGroupId;
        networkId = builder.networkId;
        configurationId = builder.configurationId;
        serviceType = builder.serviceType;
        vnfType = builder.vnfType;
        vfModuleType = builder.vfModuleType;
        networkType = builder.networkType;
        correlationId = builder.correlationId;
        requestDetails = builder.requestDetails;
        recipeParamXsd = builder.recipeParamXsd;
    }

    public String getRequestId() {
        return requestId;
    }

    public boolean isBaseVfModule() {
        return isBaseVfModule;
    }

    public int getRecipeTimeout() {
        return recipeTimeout;
    }

    public String getRequestAction() {
        return requestAction;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public String getVnfId() {
        return vnfId;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public String getVolumeGroupId() {
        return volumeGroupId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getVnfType() {
        return vnfType;
    }

    public String getVfModuleType() {
        return vfModuleType;
    }

    public String getNetworkType() {
        return networkType;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getRequestDetails() {
        return requestDetails;
    }

    public String getRecipeParamXsd() {
        return recipeParamXsd;
    }

    public static class Builder {
        private String requestId;
        private boolean isBaseVfModule;
        private int recipeTimeout;
        private String requestAction;
        private String serviceInstanceId;
        private String vnfId;
        private String vfModuleId;
        private String volumeGroupId;
        private String networkId;
        private String configurationId;
        private String serviceType;
        private String vnfType;
        private String vfModuleType;
        private String networkType;
        private String correlationId;
        private String requestDetails;
        private String recipeParamXsd;

        public Builder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder setBaseVfModule(boolean baseVfModule) {
            isBaseVfModule = baseVfModule;
            return this;
        }

        public Builder setRecipeTimeout(int recipeTimeout) {
            this.recipeTimeout = recipeTimeout;
            return this;
        }

        public Builder setRequestAction(String requestAction) {
            this.requestAction = requestAction;
            return this;
        }

        public Builder setServiceInstanceId(String serviceInstanceId) {
            this.serviceInstanceId = serviceInstanceId;
            return this;
        }

        public Builder setVnfId(String vnfId) {
            this.vnfId = vnfId;
            return this;
        }

        public Builder setVfModuleId(String vfModuleId) {
            this.vfModuleId = vfModuleId;
            return this;
        }

        public Builder setVolumeGroupId(String volumeGroupId) {
            this.volumeGroupId = volumeGroupId;
            return this;
        }

        public Builder setNetworkId(String networkId) {
            this.networkId = networkId;
            return this;
        }

        public Builder setConfigurationId(String configurationId) {
            this.configurationId = configurationId;
            return this;
        }

        public Builder setServiceType(String serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public Builder setVnfType(String vnfType) {
            this.vnfType = vnfType;
            return this;
        }

        public Builder setVfModuleType(String vfModuleType) {
            this.vfModuleType = vfModuleType;
            return this;
        }

        public Builder setNetworkType(String networkType) {
            this.networkType = networkType;
            return this;
        }

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }

        public Builder setRequestDetails(String requestDetails) {
            this.requestDetails = requestDetails;
            return this;
        }

        public Builder setRecipeParamXsd(String recipeParamXsd) {
            this.recipeParamXsd = recipeParamXsd;
            return this;
        }

        public RequestClientParamater build(){
            return new RequestClientParamater(this);
        }
    }


}
