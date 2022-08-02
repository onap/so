/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.utils;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;

public final class WorkflowResourceIdsUtils {

    private WorkflowResourceIdsUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void setResourceIdByWorkflowType(WorkflowResourceIds workflowResourceIds, WorkflowType resourceType,
            String resourceId) {
        switch (resourceType) {
            case SERVICE:
                workflowResourceIds.setServiceInstanceId(resourceId);
                break;
            case VNF:
                workflowResourceIds.setVnfId(resourceId);
                break;
            case PNF:
                workflowResourceIds.setPnfId(resourceId);
                break;
            case VFMODULE:
                workflowResourceIds.setVfModuleId(resourceId);
                break;
            case VOLUMEGROUP:
                workflowResourceIds.setVolumeGroupId(resourceId);
                break;
            case NETWORK:
                workflowResourceIds.setNetworkId(resourceId);
                break;
            case NETWORKCOLLECTION:
                workflowResourceIds.setNetworkCollectionId(resourceId);
                break;
            case CONFIGURATION:
                workflowResourceIds.setConfigurationId(resourceId);
                break;
            case INSTANCE_GROUP:
                workflowResourceIds.setInstanceGroupId(resourceId);
                break;
        }
    }

    public static void setInstanceNameByWorkflowType(WorkflowResourceIds workflowResourceIds, WorkflowType resourceType,
            String instanceName) {
        if (resourceType == WorkflowType.VNF) {
            workflowResourceIds.setVnfInstanceName(instanceName);
        } else if (resourceType == WorkflowType.VFMODULE) {
            workflowResourceIds.setVfModuleInstanceName(instanceName);
        } else if (resourceType == WorkflowType.PNF) {
            workflowResourceIds.setPnfInstanceName(instanceName);
        }
    }

    public static String getResourceIdByWorkflowType(WorkflowResourceIds workflowResourceIds,
            WorkflowType resourceType) {
        switch (resourceType) {
            case SERVICE:
                return StringUtils.defaultString(workflowResourceIds.getServiceInstanceId());
            case VNF:
                return StringUtils.defaultString(workflowResourceIds.getVnfId());
            case PNF:
                return StringUtils.defaultString(workflowResourceIds.getPnfId());
            case VFMODULE:
                return StringUtils.defaultString(workflowResourceIds.getVfModuleId());
            case VOLUMEGROUP:
                return StringUtils.defaultString(workflowResourceIds.getVolumeGroupId());
            case NETWORK:
                return StringUtils.defaultString(workflowResourceIds.getNetworkId());
            case NETWORKCOLLECTION:
                return StringUtils.defaultString(workflowResourceIds.getNetworkCollectionId());
            case CONFIGURATION:
                return StringUtils.defaultString(workflowResourceIds.getConfigurationId());
            case INSTANCE_GROUP:
                return StringUtils.defaultString(workflowResourceIds.getInstanceGroupId());
            default:
                return "";
        }
    }


    public static WorkflowResourceIds getWorkflowResourceIdsFromExecution(DelegateExecution execution) {
        WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
        workflowResourceIds.setServiceInstanceId((String) execution.getVariable("serviceInstanceId"));
        workflowResourceIds.setNetworkId((String) execution.getVariable("networkId"));
        workflowResourceIds.setVfModuleId((String) execution.getVariable("vfModuleId"));
        workflowResourceIds.setVnfId((String) execution.getVariable("vnfId"));
        workflowResourceIds.setVolumeGroupId((String) execution.getVariable("volumeGroupId"));
        workflowResourceIds.setInstanceGroupId((String) execution.getVariable("instanceGroupId"));
        workflowResourceIds.setVnfInstanceName((String) execution.getVariable("vnfInstanceName"));
        workflowResourceIds.setVfModuleInstanceName((String) execution.getVariable("vfModuleInstanceName"));
        workflowResourceIds.setPnfInstanceName((String) execution.getVariable("pnfInstanceName"));
        return workflowResourceIds;
    }

}
