package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.*;

import java.util.List;
import java.util.Map;

/**
 * Created by 10112215 on 2017/9/26.
 */
public class ServiceRpcInputEntityBuilder extends AbstractBuilder<Map<String, String>, RpcServiceTopologyOperationInputEntity> {
    @Override
    public RpcServiceTopologyOperationInputEntity build(DelegateExecution execution, Map<String, String> inputs) throws Exception {
        RpcServiceTopologyOperationInputEntity rpcServiceTopologyOperationInputEntity = new RpcServiceTopologyOperationInputEntity();
        ServiceTopologyOperationInputEntity serviceTopologyOperationInputEntity = new ServiceTopologyOperationInputEntity();
        {
            loadSdncRequestHeaderEntity(serviceTopologyOperationInputEntity, execution);
            loadRequestInformationEntity(serviceTopologyOperationInputEntity, execution);
            loadServiceInformation(serviceTopologyOperationInputEntity, execution);
            loadServiceRequestInputEntity(inputs, serviceTopologyOperationInputEntity, execution);
        }
        rpcServiceTopologyOperationInputEntity.setServiceTopologyOperationInputEntity(serviceTopologyOperationInputEntity);
        return rpcServiceTopologyOperationInputEntity;
    }

    private void loadServiceRequestInputEntity(Map<String, String> inputs, ServiceTopologyOperationInputEntity serviceTopologyOperationInputEntity, DelegateExecution execution) {
        ServiceRequestInputEntity serviceRequestInputEntity = getServiceRequestInputEntity(inputs, execution);
        serviceTopologyOperationInputEntity.setServiceRequestInput(serviceRequestInputEntity);
    }

    private ServiceRequestInputEntity getServiceRequestInputEntity(Map<String, String> inputs, DelegateExecution execution) {
        ServiceRequestInputEntity serviceRequestInputEntity = new ServiceRequestInputEntity();
        {
            ServiceInputParametersEntity serviceInputParametersEntity = getServiceInputParametersEntity(inputs);
            serviceRequestInputEntity.setServiceInputParametersEntity(serviceInputParametersEntity);
        }
        serviceRequestInputEntity.setServiceInstanceName(getServiceInstanceName(execution));
        return serviceRequestInputEntity;
    }

    private ServiceInputParametersEntity getServiceInputParametersEntity(Map<String, String> inputs) {
        List<ParamEntity> paramEntityList = getParamEntities(inputs);
        ServiceInputParametersEntity serviceInputParametersEntity = new ServiceInputParametersEntity();
        serviceInputParametersEntity.setParamList(paramEntityList);
        return serviceInputParametersEntity;
    }

    private void loadServiceInformation(ServiceTopologyOperationInputEntity serviceTopologyOperationInputEntity, DelegateExecution execution) {
        ServiceInformationEntity serviceInformationEntity = getServiceInformationEntity(execution);
        serviceTopologyOperationInputEntity.setServiceInformation(serviceInformationEntity);
    }

    private void loadRequestInformationEntity(ServiceTopologyOperationInputEntity serviceTopologyOperationInputEntity, DelegateExecution execution) {
        RequestInformationEntity requestInformationEntity = getRequestInformationEntity(execution);
        serviceTopologyOperationInputEntity.setRequestInformation(requestInformationEntity);
    }

    private void loadSdncRequestHeaderEntity(ServiceTopologyOperationInputEntity serviceTopologyOperationInputEntity, DelegateExecution execution) {
        SdncRequestHeaderEntity sdncRequestHeaderEntity = new SdncRequestHeaderEntity();
        {
            sdncRequestHeaderEntity.setSvcRequestId(getRequestId(execution));
            sdncRequestHeaderEntity.setSvcAction(getOperationType(execution));
        }
        serviceTopologyOperationInputEntity.setSdncRequestHeader(sdncRequestHeaderEntity);
    }
}
