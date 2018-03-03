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

package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class ServiceRequestInputEntity {

    @JsonProperty("GENERIC-RESOURCE-API:service-instance-name")
    private String serviceInstanceName;

    @JsonProperty("GENERIC-RESOURCE-API:service-input-parameters")
    private ServiceInputParametersEntity serviceInputParametersEntity;

    public String getServiceInstanceName() {
        return serviceInstanceName;
    }

    public void setServiceInstanceName(String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
    }

    public ServiceInputParametersEntity getServiceInputParametersEntity() {
        return serviceInputParametersEntity;
    }

    public void setServiceInputParametersEntity(ServiceInputParametersEntity serviceInputParametersEntity) {
        this.serviceInputParametersEntity = serviceInputParametersEntity;
    }
}
