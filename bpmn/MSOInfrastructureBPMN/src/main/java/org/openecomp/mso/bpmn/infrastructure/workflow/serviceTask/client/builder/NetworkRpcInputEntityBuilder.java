package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.*;

import java.util.List;
import java.util.Map;

/**
 * Created by 10112215 on 2017/9/20.
 */
public class NetworkRpcInputEntityBuilder extends AbstractBuilder<Map<String, String>, RpcNetworkTopologyOperationInputEntity> {

    @Override
    public RpcNetworkTopologyOperationInputEntity build(DelegateExecution execution, Map<String, String> inputs) {
        RpcNetworkTopologyOperationInputEntity rpcNetworkTopologyOperationInputEntity = new RpcNetworkTopologyOperationInputEntity();
        NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity = getNetworkTopologyOperationInputEntity(execution, inputs);
        rpcNetworkTopologyOperationInputEntity.setInput(networkTopologyOperationInputEntity);
        return rpcNetworkTopologyOperationInputEntity;
    }

    private void loadNetwrokRequestInputEntity(Map<String, String> inputs, NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity) {
        NetworkRequestInputEntity networkRequestInputEntity = new NetworkRequestInputEntity();
        {
            NetworkInputPaarametersEntity networkInputPaarametersEntity = new NetworkInputPaarametersEntity();
            {
                List<ParamEntity> paramEntityList = getParamEntities(inputs);
                networkInputPaarametersEntity.setParamList(paramEntityList);
            }
        }
        networkTopologyOperationInputEntity.setNetworkRequestInput(networkRequestInputEntity);
    }

    private void loadRequestInformationEntity(NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity, DelegateExecution execution) {
        RequestInformationEntity requestInformationEntity = getRequestInformationEntity(execution);
        networkTopologyOperationInputEntity.setRequestInformation(requestInformationEntity);
    }

    private void loadSdncRequestHeaderEntity(NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity, DelegateExecution execution) {
        SdncRequestHeaderEntity sdncRequestHeaderEntity = new SdncRequestHeaderEntity();
        {
            sdncRequestHeaderEntity.setSvcRequestId(getRequestId(execution));
            sdncRequestHeaderEntity.setSvcAction(getOperationType(execution));
        }
        networkTopologyOperationInputEntity.setSdncRequestHeader(sdncRequestHeaderEntity);
    }

    private void loadServiceInformation(NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity, DelegateExecution execution) {
        ServiceInformationEntity serviceInformationEntity = getServiceInformationEntity(execution);
        networkTopologyOperationInputEntity.setServiceInformation(serviceInformationEntity);
    }

    private NetworkTopologyOperationInputEntity getNetworkTopologyOperationInputEntity(DelegateExecution execution, Map<String, String> inputs) {
        NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity = new NetworkTopologyOperationInputEntity();
        {
            loadSdncRequestHeaderEntity(networkTopologyOperationInputEntity, execution);
            loadRequestInformationEntity(networkTopologyOperationInputEntity, execution);
            loadServiceInformation(networkTopologyOperationInputEntity, execution);
            loadNetworkInformationEntity(execution, networkTopologyOperationInputEntity);
            loadNetwrokRequestInputEntity(inputs, networkTopologyOperationInputEntity);
        }
        return networkTopologyOperationInputEntity;
    }

    private void loadNetworkInformationEntity(DelegateExecution execution, NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity) {
        NetworkInformationEntity networkInformationEntity = new NetworkInformationEntity();
        {
            OnapModelInformationEntity onapModelInformationEntity = getOnapModelInformationEntity(execution);
            networkInformationEntity.setOnapModelInformation(onapModelInformationEntity);
        }
        networkTopologyOperationInputEntity.setNetworkInformation(networkInformationEntity);
    }

}
