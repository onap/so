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

package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.OnapModelInformationEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.ParamEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RequestInformationEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.ServiceInformationEntity;
import org.openecomp.mso.requestsdb.RequestsDbConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractBuilder<IN, OUT> {

     public static final String OPERATION_TYPE = "operationType";
     public static final String RESOURCE_TYPE = "resourceType";

     public static enum RequestAction {
          CreateNetworkInstance(0, "CreateNetworkInstance"),
          ActivateNetworkInstance(1, "ActivateNetworkInstance"),
          CreateServiceInstance(2, "CreateServiceInstance"),
          DeleteServiceInstance(3, "DeleteServiceInstance"),
          DeleteNetworkInstance(4, "DeleteNetworkInstance"),
          CreateVnfInstance(5, "CreateVnfInstance"),
          ActivateVnfInstance(6, "ActivateVnfInstance"),
          DeleteVnfInstance(7, "DeleteVnfInstance"),
          CreateVfModuleInstance(8, "CreateVfModuleInstance"),
          ActivateVfModuleInstance(9, "ActivateVfModuleInstance"),
          DeleteVfModuleInstance(10, "DeleteVfModuleInstance"),
          CreateContrailRouteInstance(11, "CreateContrailRouteInstance"),
          DeleteContrailRouteInstance(12, "DeleteContrailRouteInstance"),
          CreateSecurityZoneInstance(13, "CreateSecurityZoneInstance"),
          DeleteSecurityZoneInstance(14, "DeleteSecurityZoneInstance"),
          ActivateDCINetworkInstance(15, "ActivateDCINetworkInstance"),
          DeActivateDCINetworkInstance(16, "DeActivateDCINetworkInstance");

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
          Reserve(0, "reserve"),
          Assign(1, "assign"),
          Activate(2, "activate"),
          Delete(3, "delete"),
          Changeassign(4, "changeassign"),
          Changedelete(5, "changedelete"),
          Rollback(6, "rollback"),
          Deactivate(7, "deactivate"),
          Unassign(8, "unassign"),
          Create(9, "create");

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

     abstract OUT build(DelegateExecution execution, IN input) throws Exception;

     protected String getRequestActoin(DelegateExecution execution) {
          String action = /*RequestInformation.*/RequestAction.CreateNetworkInstance.getName();
          String operType = (String) execution.getVariable(OPERATION_TYPE);
          String resourceType = ((String) execution.getVariable(RESOURCE_TYPE)).toLowerCase();
          if (!StringUtils.isBlank(operType)) {
               if (RequestsDbConstant.OperationType.DELETE.equalsIgnoreCase(operType)) {
                    if (isOverlay(resourceType)) {
                         action = /*RequestInformation.*/RequestAction.DeActivateDCINetworkInstance.getName();
                    } else if (isUnderlay(resourceType)) {
                         action = /*RequestInformation.*/RequestAction.DeleteNetworkInstance.getName();
                    } else {
                         action = /*RequestInformation.*/RequestAction.DeleteServiceInstance.getName();
                    }
               } else if (RequestsDbConstant.OperationType.CREATE.equalsIgnoreCase(operType)) {
                    if (isOverlay(resourceType)) {
                         action = /*RequestInformation.*/RequestAction.ActivateDCINetworkInstance.getName();
                    } else if (isUnderlay(resourceType)) {
                         action = /*RequestInformation.*/RequestAction.CreateNetworkInstance.getName();
                    } else {
                         action = /*RequestInformation.*/RequestAction.CreateServiceInstance.getName();
                    }
               }
          }
          return action;
     }

     private boolean isOverlay(String resourceType) {
          return !StringUtils.isBlank(resourceType) && resourceType.contains("overlay");
     }

     private boolean isUnderlay(String resourceType) {
          return !StringUtils.isBlank(resourceType) && resourceType.contains("underlay");
     }

     protected String getSvcAction(DelegateExecution execution) {
          String action = /*SdncRequestHeader.*/SvcAction.Create.getName();
          String operType = (String) execution.getVariable(OPERATION_TYPE);
          String resourceType = ((String) execution.getVariable(RESOURCE_TYPE)).toLowerCase();
          if (!StringUtils.isBlank(operType)) {
               if (RequestsDbConstant.OperationType.DELETE.equalsIgnoreCase(operType)) {
                    if (isOverlay(resourceType)) {
                         action = /*SdncRequestHeader.*/SvcAction.Deactivate.getName();
                    } else if (isUnderlay(resourceType)) {
                         action = /*SdncRequestHeader.*/SvcAction.Delete.getName();
                    } else {
                         action = /*SdncRequestHeader.*/SvcAction.Unassign.getName();
                    }
               } else if (RequestsDbConstant.OperationType.CREATE.equalsIgnoreCase(operType)) {
                    if (isOverlay(resourceType)) {
                         action = /*SdncRequestHeader.*/SvcAction.Activate.getName();
                    } else if (isUnderlay(resourceType)) {
                         action = /*SdncRequestHeader.*/SvcAction.Create.getName();
                    } else {
                         action = /*SdncRequestHeader.*/SvcAction.Assign.getName();
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
          {
               String modelInvariantUuid = (String) execution.getVariable("modelInvariantUuid");
               String modelVersion = (String) execution.getVariable("modelVersion");
               String modelUuid = (String) execution.getVariable("modelUuid");
               String modelName = (String) execution.getVariable("serviceModelName");
               onapModelInformationEntity.setModelInvariantUuid(modelInvariantUuid);
               onapModelInformationEntity.setModelVersion(modelVersion);
               onapModelInformationEntity.setModelUuid(modelUuid);
               onapModelInformationEntity.setModelName(modelName);
          }
          return onapModelInformationEntity;
     }
     
     protected OnapModelInformationEntity getOnapNetworkModelInformationEntity(DelegateExecution execution) {
         OnapModelInformationEntity onapModelInformationEntity = new OnapModelInformationEntity();
         {
              String modelInvariantUuid = (String) execution.getVariable("resourceInvariantUUID");
              String modelVersion = (String) execution.getVariable("modelVersion");
              String modelUuid = (String) execution.getVariable("resourceUUID");
              String modelName = (String) execution.getVariable("resourceType");
              onapModelInformationEntity.setModelInvariantUuid(modelInvariantUuid);
              onapModelInformationEntity.setModelVersion(modelVersion);
              onapModelInformationEntity.setModelUuid(modelUuid);
              onapModelInformationEntity.setModelName(modelName);
         }
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
          {
               requestInformationEntity.setRequestId(getRequestId(execution));
               requestInformationEntity.setRequestAction(getRequestActoin(execution));
          }
          return requestInformationEntity;
     }

     protected ServiceInformationEntity getServiceInformationEntity(DelegateExecution execution) {
          ServiceInformationEntity serviceInformationEntity = new ServiceInformationEntity();
          serviceInformationEntity.setServiceId("VOLTE_SERVICE_ID");
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
