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

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.*;

import java.util.List;
import java.util.Map;

public class ServiceRpcInputEntityBuilder extends AbstractBuilder<Map<String, String>, RpcServiceTopologyOperationInputEntity> {
    @Override
    public RpcServiceTopologyOperationInputEntity build(DelegateExecution execution, Map<String, String> inputs) throws Exception {
        RpcServiceTopologyOperationInputEntity rpcServiceTopologyOperationInputEntity = new RpcServiceTopologyOperationInputEntity();
        ServiceTopologyOperationInputEntity serviceTopologyOperationInputEntity = new ServiceTopologyOperationInputEntity();
        {
            loadSdncRequestHeaderEntity(serviceTopologyOperationInputEntity, execution);
            loadRequestInformationEntity(serviceTopologyOperationInputEntity, execution);
            loadServiceInformation(serviceTopologyOperationInputEntity, execution);
            loadServiceRequestInputEntity(serviceTopologyOperationInputEntity, execution);
        }
        rpcServiceTopologyOperationInputEntity.setServiceTopologyOperationInputEntity(serviceTopologyOperationInputEntity);
        return rpcServiceTopologyOperationInputEntity;
    }

    private void loadServiceRequestInputEntity(ServiceTopologyOperationInputEntity serviceTopologyOperationInputEntity, DelegateExecution execution) {
        ServiceRequestInputEntity serviceRequestInputEntity = getServiceRequestInputEntity(execution);
        serviceTopologyOperationInputEntity.setServiceRequestInput(serviceRequestInputEntity);
    }

    private ServiceRequestInputEntity getServiceRequestInputEntity(DelegateExecution execution) {
        ServiceRequestInputEntity serviceRequestInputEntity = new ServiceRequestInputEntity();
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
            sdncRequestHeaderEntity.setSvcAction(getSvcAction(execution));
        }
        serviceTopologyOperationInputEntity.setSdncRequestHeader(sdncRequestHeaderEntity);
    }
}
