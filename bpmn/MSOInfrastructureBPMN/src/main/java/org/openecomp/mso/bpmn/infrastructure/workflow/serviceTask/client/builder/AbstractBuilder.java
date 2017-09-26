package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.request.information.RequestInformation;
import org.opendaylight.yang.gen.v1.org.onap.sdnc.northbound.generic.resource.rev170824.sdnc.request.header.SdncRequestHeader;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.SdncUnderlayVpnPreprocessTask;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.OnapModelInformationEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.ParamEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RequestInformationEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.ServiceInformationEntity;
import org.openecomp.mso.requestsdb.RequestsDbConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by 10112215 on 2017/9/20.
 */
public abstract class AbstractBuilder<IN, OUT> {
     protected String requestId = null;

     abstract OUT build(DelegateExecution execution, IN input) throws Exception;

     protected String getRequestActoin(DelegateExecution execution) {
          String action = RequestInformation.RequestAction.CreateNetworkInstance.name();
          String operType = getOperType(execution);
          if (!StringUtils.isBlank(operType)) {
               if (RequestsDbConstant.OperationType.DELETE.equals(operType)) {
                    action = RequestInformation.RequestAction.DeleteNetworkInstance.name();
               } else if (RequestsDbConstant.OperationType.CREATE.equals(operType)) {
                    action = RequestInformation.RequestAction.CreateNetworkInstance.name();
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

     protected String getOperationType(DelegateExecution execution) {
          String action = SdncRequestHeader.SvcAction.Create.name();
          String operType = getOperType(execution);
          if (!StringUtils.isBlank(operType)) {
               if (RequestsDbConstant.OperationType.DELETE.equals(operType)) {
                    action = SdncRequestHeader.SvcAction.Delete.name();
               } else if (RequestsDbConstant.OperationType.CREATE.equals(operType)) {
                    action = SdncRequestHeader.SvcAction.Create.name();
               }
          }
          return action;
     }

     protected String getOperType(DelegateExecution execution) {
          return (String) execution.getVariable(SdncUnderlayVpnPreprocessTask.RESOURCE_OPER_TYPE);
     }

     protected OnapModelInformationEntity getOnapModelInformationEntity(DelegateExecution execution) {
          OnapModelInformationEntity onapModelInformationEntity = new OnapModelInformationEntity();
          {
               String modelInvariantUuid = (String) execution.getVariable("modelInvariantUuid");
               String modelVersion = (String) execution.getVariable("modelVersion");
               String modelUuid = (String) execution.getVariable("modelUuid");
               String modelName = (String) execution.getVariable("modelName");
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
          serviceInformationEntity.setServiceId((String) execution.getVariable("productFamilyId"));
          serviceInformationEntity.setSubscriptionServiceType((String) execution.getVariable("subscriptionServiceType"));
          serviceInformationEntity.setOnapModelInformation(getOnapModelInformationEntity(execution));
          serviceInformationEntity.setServiceInstanceId((String) execution.getVariable("serviceInstanceId"));
          serviceInformationEntity.setGlobalCustomerId((String) execution.getVariable("globalSubscriberId"));
          return serviceInformationEntity;
     }

     protected String getServiceInstanceName(DelegateExecution execution) {
          return (String) execution.getVariable("serviceInstanceName");
     }
}
