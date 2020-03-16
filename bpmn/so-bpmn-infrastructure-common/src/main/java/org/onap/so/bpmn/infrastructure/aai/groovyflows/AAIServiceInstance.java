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

package org.onap.so.bpmn.infrastructure.aai.groovyflows;

public class AAIServiceInstance {
    String serviceInstanceName;
    String serviceType;
    String serviceRole;
    String orchestrationStatus;
    String modelInvariantUuid;
    String modelVersionId;
    String environmentContext;
    String workloadContext;

    public static class AAIServiceInstanceBuilder {
        private String serviceInstanceName;
        private String serviceType;
        private String serviceRole;
        private String orchestrationStatus;
        private String modelInvariantUuid;
        private String modelVersionId;
        private String environmentContext;
        private String workloadContext;

        public AAIServiceInstanceBuilder setServiceInstanceName(String serviceInstanceName) {
            this.serviceInstanceName = serviceInstanceName;
            return this;
        }

        public AAIServiceInstanceBuilder setServiceType(String serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public AAIServiceInstanceBuilder setServiceRole(String serviceRole) {
            this.serviceRole = serviceRole;
            return this;
        }

        public AAIServiceInstanceBuilder setOrchestrationStatus(String orchestrationStatus) {
            this.orchestrationStatus = orchestrationStatus;
            return this;
        }

        public AAIServiceInstanceBuilder setModelInvariantUuid(String modelInvariantUuid) {
            this.modelInvariantUuid = modelInvariantUuid;
            return this;
        }

        public AAIServiceInstanceBuilder setModelVersionId(String modelVersionId) {
            this.modelVersionId = modelVersionId;
            return this;
        }

        public AAIServiceInstanceBuilder setEnvironmentContext(String environmentContext) {
            this.environmentContext = environmentContext;
            return this;
        }

        public AAIServiceInstanceBuilder setWorkloadContext(String workloadContext) {
            this.workloadContext = workloadContext;
            return this;
        }

        public AAIServiceInstance createAAIServiceInstance() {
            return new AAIServiceInstance(this);
        }
    }

    public AAIServiceInstance(AAIServiceInstanceBuilder aaiServiceInstanceBuilder) {
        this.serviceInstanceName = aaiServiceInstanceBuilder.serviceInstanceName;
        this.serviceType = aaiServiceInstanceBuilder.serviceType;
        this.serviceRole = aaiServiceInstanceBuilder.serviceRole;
        this.orchestrationStatus = aaiServiceInstanceBuilder.orchestrationStatus;
        this.modelInvariantUuid = aaiServiceInstanceBuilder.modelInvariantUuid;
        this.modelVersionId = aaiServiceInstanceBuilder.modelVersionId;
        this.environmentContext = aaiServiceInstanceBuilder.environmentContext;
        this.workloadContext = aaiServiceInstanceBuilder.workloadContext;
    }

    public String getServiceInstanceName() {
        return serviceInstanceName;
    }

    public void setServiceInstanceName(String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(String serviceRole) {
        this.serviceRole = serviceRole;
    }

    public String getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(String orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public String getModelInvariantUuid() {
        return modelInvariantUuid;
    }

    public void setModelInvariantUuid(String modelInvariantUuid) {
        this.modelInvariantUuid = modelInvariantUuid;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    public String getEnvironmentContext() {
        return environmentContext;
    }

    public void setEnvironmentContext(String environmentContext) {
        this.environmentContext = environmentContext;
    }

    public String getWorkloadContext() {
        return workloadContext;
    }

    public void setWorkloadContext(String workloadContext) {
        this.workloadContext = workloadContext;
    }


}
