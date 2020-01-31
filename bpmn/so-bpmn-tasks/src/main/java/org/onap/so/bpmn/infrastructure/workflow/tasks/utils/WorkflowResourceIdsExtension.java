package org.onap.so.bpmn.infrastructure.workflow.tasks.utils;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import java.util.function.Consumer;

public class WorkflowResourceIdsExtension extends WorkflowResourceIds {

    public void setResourceId(WorkflowType resourceType, String resourceId) {
        getSetterForResourceType(resourceType).accept(resourceId);
    }

    public void populateFromExecution(DelegateExecution execution) {
        this.setServiceInstanceId((String) execution.getVariable("serviceInstanceId"));
        this.setNetworkId((String) execution.getVariable("networkId"));
        this.setVfModuleId((String) execution.getVariable("vfModuleId"));
        this.setVnfId((String) execution.getVariable("vnfId"));
        this.setVolumeGroupId((String) execution.getVariable("volumeGroupId"));
        this.setInstanceGroupId((String) execution.getVariable("instanceGroupId"));
    }

    private Consumer<String> getSetterForResourceType(WorkflowType resourceType) {
        switch (resourceType) {
            case SERVICE:
                return this::setServiceInstanceId;
            case VNF:
                return this::setVnfId;
            case PNF:
                return this::setPnfId;
            case VFMODULE:
                return this::setVfModuleId;
            case VOLUMEGROUP:
                return this::setVolumeGroupId;
            case NETWORK:
                return this::setNetworkId;
            case NETWORKCOLLECTION:
                return this::setNetworkCollectionId;
            case CONFIGURATION:
                return this::setConfigurationId;
            case INSTANCE_GROUP:
                return this::setInstanceGroupId;
            default:
                return __ -> {
                };
        }
    }
}
