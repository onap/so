package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class ServiceRequestInputEntity {

    @JsonProperty("service-instance-name")
    private String serviceInstanceName;

    @JsonProperty("service-input-parameters")
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
