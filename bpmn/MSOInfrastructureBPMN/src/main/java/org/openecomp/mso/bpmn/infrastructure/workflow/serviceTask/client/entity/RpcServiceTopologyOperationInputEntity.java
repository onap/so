package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class RpcServiceTopologyOperationInputEntity {
    public ServiceTopologyOperationInputEntity getServiceTopologyOperationInputEntity() {
        return serviceTopologyOperationInputEntity;
    }

    public void setServiceTopologyOperationInputEntity(ServiceTopologyOperationInputEntity serviceTopologyOperationInputEntity) {
        this.serviceTopologyOperationInputEntity = serviceTopologyOperationInputEntity;
    }

    @JsonProperty("input")
    private ServiceTopologyOperationInputEntity serviceTopologyOperationInputEntity;
}
