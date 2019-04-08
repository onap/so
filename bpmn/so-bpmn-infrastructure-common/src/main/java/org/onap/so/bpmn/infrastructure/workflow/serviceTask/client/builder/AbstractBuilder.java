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

package org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.OnapModelInformationEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.ParamEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RequestInformationEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.ServiceInformationEntity;
import org.onap.so.requestsdb.RequestsDbConstant;

public abstract class AbstractBuilder<I, O> {

    public static final String OPERATION_TYPE = "operationType";
    public static final String RESOURCE_TYPE = "resourceType";

    public enum RequestAction {
        CREATE_NETWORK_INSTANCE(0, "CreateNetworkInstance"), ACTIVATE_NETWORK_INSTANCE(1,
                "ActivateNetworkInstance"), CREATE_SERVICE_INSTANCE(2,
                        "CreateServiceInstance"), DELETE_SERVICE_INSTANCE(3,
                                "DeleteServiceInstance"), DELETE_NETWORK_INSTANCE(4,
                                        "DeleteNetworkInstance"), CREATE_VNF_INSTANCE(5,
                                                "CreateVnfInstance"), ACTIVATE_VNF_INSTANCE(6,
                                                        "ActivateVnfInstance"), DELETE_VNF_INSTANCE(7,
                                                                "DeleteVnfInstance"), CREATE_VF_MODULE_INSTANCE(8,
                                                                        "CreateVfModuleInstance"), ACTIVATE_VF_MODULE_INSTANCE(
                                                                                9,
                                                                                "ActivateVfModuleInstance"), DELETE_VF_MODULE_INSTANCE(
                                                                                        10,
                                                                                        "DeleteVfModuleInstance"), CREATE_CONTRAIL_ROUTE_INSTANCE(
                                                                                                11,
                                                                                                "CreateContrailRouteInstance"), DELETE_CONTRAIL_ROUTE_INSTANCE(
                                                                                                        12,
                                                                                                        "DeleteContrailRouteInstance"), CREATE_SECURITY_ZONE_INSTANCE(
                                                                                                                13,
                                                                                                                "CreateSecurityZoneInstance"), DELETE_SECURITY_ZONE_INSTANCE(
                                                                                                                        14,
                                                                                                                        "DeleteSecurityZoneInstance"), ACTIVATE_DCI_NETWORK_INSTANCE(
                                                                                                                                15,
                                                                                                                                "ActivateDCINetworkInstance"), DEACTIVATE_DCI_NETWORK_INSTANCE(
                                                                                                                                        16,
                                                                                                                                        "DeActivateDCINetworkInstance");

        String name;
        int value;

        private RequestAction(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public int getIntValue() {
            return this.value;
        }
    }

    public enum SvcAction {
        RESERVE(0, "reserve"), ASSIGN(1, "assign"), ACTIVATE(2, "activate"), DELETE(3, "delete"), CHANGEASSIGN(4,
                "changeassign"), CHANGEDELETE(5, "changedelete"), ROLLBACK(6,
                        "rollback"), DEACTIVATE(7, "deactivate"), UNASSIGN(8, "unassign"), CREATE(9, "create");

        String name;
        int value;

        private SvcAction(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public int getIntValue() {
            return this.value;
        }
    }

    protected String requestId = null;

    abstract O build(DelegateExecution execution, I input) throws Exception;

    protected String getRequestAction(DelegateExecution execution) {
        String action = /* RequestInformation. */RequestAction.CREATE_NETWORK_INSTANCE.getName();
        String operType = (String) execution.getVariable(OPERATION_TYPE);
        String resourceType = (String) execution.getVariable(RESOURCE_TYPE);
        if (!StringUtils.isBlank(operType)) {
            if (RequestsDbConstant.OperationType.DELETE.equalsIgnoreCase(operType)) {
                if (isOverlay(resourceType)) {
                    action = /* RequestInformation. */RequestAction.DEACTIVATE_DCI_NETWORK_INSTANCE.getName();
                } else if (isUnderlay(resourceType)) {
                    action = /* RequestInformation. */RequestAction.DELETE_NETWORK_INSTANCE.getName();
                } else {
                    action = /* RequestInformation. */RequestAction.DELETE_SERVICE_INSTANCE.getName();
                }
            } else if (RequestsDbConstant.OperationType.CREATE.equalsIgnoreCase(operType)) {
                if (isOverlay(resourceType)) {
                    action = /* RequestInformation. */RequestAction.ACTIVATE_DCI_NETWORK_INSTANCE.getName();
                } else if (isUnderlay(resourceType)) {
                    action = /* RequestInformation. */RequestAction.CREATE_NETWORK_INSTANCE.getName();
                } else {
                    action = /* RequestInformation. */RequestAction.CREATE_SERVICE_INSTANCE.getName();
                }
            }
        }
        return action;
    }

    private boolean isOverlay(String resourceType) {
        return !StringUtils.isBlank(resourceType) && resourceType.toLowerCase().contains("overlay");
    }

    private boolean isUnderlay(String resourceType) {
        return !StringUtils.isBlank(resourceType) && resourceType.toLowerCase().contains("underlay");
    }

    protected String getSvcAction(DelegateExecution execution) {
        String action = /* SdncRequestHeader. */SvcAction.CREATE.getName();
        String operType = (String) execution.getVariable(OPERATION_TYPE);
        String resourceType = (String) execution.getVariable(RESOURCE_TYPE);
        if (!StringUtils.isBlank(operType)) {
            if (RequestsDbConstant.OperationType.DELETE.equalsIgnoreCase(operType)) {
                if (isOverlay(resourceType)) {
                    action = /* SdncRequestHeader. */SvcAction.DEACTIVATE.getName();
                } else if (isUnderlay(resourceType)) {
                    action = /* SdncRequestHeader. */SvcAction.DELETE.getName();
                } else {
                    action = /* SdncRequestHeader. */SvcAction.UNASSIGN.getName();
                }
            } else if (RequestsDbConstant.OperationType.CREATE.equalsIgnoreCase(operType)) {
                if (isOverlay(resourceType)) {
                    action = /* SdncRequestHeader. */SvcAction.ACTIVATE.getName();
                } else if (isUnderlay(resourceType)) {
                    action = /* SdncRequestHeader. */SvcAction.CREATE.getName();
                } else {
                    action = /* SdncRequestHeader. */SvcAction.ASSIGN.getName();
                }
            }
        }
        return action;
    }

    protected synchronized String getRequestId(DelegateExecution execution) {
        if (StringUtils.isBlank(requestId)) {
            requestId = (String) execution.getVariable("msoRequestId");
            if (StringUtils.isBlank(requestId)) {
                requestId = UUID.randomUUID().toString();
            }
        }
        return requestId;
    }

    protected OnapModelInformationEntity getOnapServiceModelInformationEntity(DelegateExecution execution) {
        OnapModelInformationEntity onapModelInformationEntity = new OnapModelInformationEntity();
        String modelInvariantUuid = (String) execution.getVariable("modelInvariantUuid");
        String modelVersion = (String) execution.getVariable("modelVersion");
        String modelUuid = (String) execution.getVariable("modelUuid");
        String modelName = (String) execution.getVariable("serviceModelName");
        onapModelInformationEntity.setModelInvariantUuid(modelInvariantUuid);
        onapModelInformationEntity.setModelVersion(modelVersion);
        onapModelInformationEntity.setModelUuid(modelUuid);
        onapModelInformationEntity.setModelName(modelName);
        return onapModelInformationEntity;
    }

    protected OnapModelInformationEntity getOnapNetworkModelInformationEntity(DelegateExecution execution) {
        OnapModelInformationEntity onapModelInformationEntity = new OnapModelInformationEntity();
        String modelInvariantUuid = (String) execution.getVariable("resourceInvariantUUID");
        String modelVersion = (String) execution.getVariable("modelVersion");
        String modelUuid = (String) execution.getVariable("resourceUUID");
        String modelName = (String) execution.getVariable(RESOURCE_TYPE);
        onapModelInformationEntity.setModelInvariantUuid(modelInvariantUuid);
        onapModelInformationEntity.setModelVersion(modelVersion);
        onapModelInformationEntity.setModelUuid(modelUuid);
        onapModelInformationEntity.setModelName(modelName);
        return onapModelInformationEntity;
    }

    protected List<ParamEntity> getParamEntities(Map<String, String> inputs) {
        List<ParamEntity> paramEntityList = new ArrayList<>();
        if (inputs != null && !inputs.isEmpty()) {
            inputs.keySet().forEach(key -> {
                ParamEntity paramEntity = new ParamEntity();
                paramEntity.setName(key);
                paramEntity.setValue(inputs.get(key));
                paramEntityList.add(paramEntity);
            });
        }
        return paramEntityList;
    }

    protected RequestInformationEntity getRequestInformationEntity(DelegateExecution execution) {
        RequestInformationEntity requestInformationEntity = new RequestInformationEntity();
        requestInformationEntity.setRequestId(getRequestId(execution));
        requestInformationEntity.setRequestAction(getRequestAction(execution));
        return requestInformationEntity;
    }

    protected ServiceInformationEntity getServiceInformationEntity(DelegateExecution execution) {
        ServiceInformationEntity serviceInformationEntity = new ServiceInformationEntity();
        serviceInformationEntity.setServiceId((String) execution.getVariable("serviceInstanceId"));
        serviceInformationEntity.setSubscriptionServiceType((String) execution.getVariable("serviceType"));
        serviceInformationEntity.setOnapModelInformation(getOnapServiceModelInformationEntity(execution));
        serviceInformationEntity.setServiceInstanceId((String) execution.getVariable("serviceInstanceId"));
        serviceInformationEntity.setGlobalCustomerId((String) execution.getVariable("globalSubscriberId"));
        return serviceInformationEntity;
    }

    protected String getServiceInstanceName(DelegateExecution execution) {
        return (String) execution.getVariable("serviceInstanceName");
    }
}
