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

import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkInformationEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkInputParametersEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkRequestInputEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkTopologyOperationInputEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.OnapModelInformationEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.ParamEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RequestInformationEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationInputEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.SdncRequestHeaderEntity;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.ServiceInformationEntity;

public class NetworkRpcInputEntityBuilder
        extends AbstractBuilder<Map<String, String>, RpcNetworkTopologyOperationInputEntity> {

    @Override
    public RpcNetworkTopologyOperationInputEntity build(DelegateExecution execution, Map<String, String> inputs) {
        RpcNetworkTopologyOperationInputEntity rpcNetworkTopologyOperationInputEntity =
                new RpcNetworkTopologyOperationInputEntity();
        NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity =
                getNetworkTopologyOperationInputEntity(execution, inputs);
        rpcNetworkTopologyOperationInputEntity.setInput(networkTopologyOperationInputEntity);
        return rpcNetworkTopologyOperationInputEntity;
    }

    private void loadNetwrokRequestInputEntity(Map<String, String> inputs,
            NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity) {
        NetworkRequestInputEntity networkRequestInputEntity = new NetworkRequestInputEntity();
        NetworkInputParametersEntity networkInputParametersEntity = new NetworkInputParametersEntity();
        List<ParamEntity> paramEntityList = getParamEntities(inputs);
        networkInputParametersEntity.setParamList(paramEntityList);
        networkRequestInputEntity.setNetworkInputPaarameters(networkInputParametersEntity);
        networkTopologyOperationInputEntity.setNetworkRequestInput(networkRequestInputEntity);
    }

    private void loadRequestInformationEntity(NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity,
            DelegateExecution execution) {
        RequestInformationEntity requestInformationEntity = getRequestInformationEntity(execution);
        networkTopologyOperationInputEntity.setRequestInformation(requestInformationEntity);
    }

    private void loadSdncRequestHeaderEntity(NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity,
            DelegateExecution execution) {
        SdncRequestHeaderEntity sdncRequestHeaderEntity = new SdncRequestHeaderEntity();
        sdncRequestHeaderEntity.setSvcRequestId(getRequestId(execution));
        sdncRequestHeaderEntity.setSvcAction(getSvcAction(execution));
        networkTopologyOperationInputEntity.setSdncRequestHeader(sdncRequestHeaderEntity);
    }

    private void loadServiceInformation(NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity,
            DelegateExecution execution) {
        ServiceInformationEntity serviceInformationEntity = getServiceInformationEntity(execution);
        networkTopologyOperationInputEntity.setServiceInformation(serviceInformationEntity);
    }

    private NetworkTopologyOperationInputEntity getNetworkTopologyOperationInputEntity(DelegateExecution execution,
            Map<String, String> inputs) {
        NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity =
                new NetworkTopologyOperationInputEntity();
        loadSdncRequestHeaderEntity(networkTopologyOperationInputEntity, execution);
        loadRequestInformationEntity(networkTopologyOperationInputEntity, execution);
        loadServiceInformation(networkTopologyOperationInputEntity, execution);
        loadNetworkInformationEntity(execution, networkTopologyOperationInputEntity);
        loadNetwrokRequestInputEntity(inputs, networkTopologyOperationInputEntity);
        return networkTopologyOperationInputEntity;
    }

    private void loadNetworkInformationEntity(DelegateExecution execution,
            NetworkTopologyOperationInputEntity networkTopologyOperationInputEntity) {
        NetworkInformationEntity networkInformationEntity = new NetworkInformationEntity();
        OnapModelInformationEntity onapModelInformationEntity = getOnapNetworkModelInformationEntity(execution);
        networkInformationEntity.setOnapModelInformation(onapModelInformationEntity);
        networkTopologyOperationInputEntity.setNetworkInformation(networkInformationEntity);
    }
}
