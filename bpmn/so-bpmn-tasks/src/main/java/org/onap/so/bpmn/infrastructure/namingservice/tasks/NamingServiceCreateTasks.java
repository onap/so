/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

package org.onap.so.bpmn.infrastructure.namingservice.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VpnBinding;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.namingservice.NamingRequestObject;
import org.onap.so.client.namingservice.NamingServiceConstants;
import org.onap.so.client.orchestration.NamingServiceResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NamingServiceCreateTasks {

    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    @Autowired
    private NamingServiceResources namingServiceResources;
    @Autowired
    protected InjectionHelper injectionHelper;
    @Autowired
    protected BBInputSetupUtils bbInputSetupUtils;

    public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
        this.bbInputSetupUtils = bbInputSetupUtils;
    }

    public void createInstanceGroupName(BuildingBlockExecution execution) throws BBObjectNotFoundException {
        InstanceGroup instanceGroup = extractPojosForBB.extractByKey(execution, ResourceKey.INSTANCE_GROUP_ID);
        String policyInstanceName = execution.getVariable("policyInstanceName");
        String nfNamingCode = execution.getVariable("nfNamingCode");
        String generatedInstanceGroupName = "";
        try {
            generatedInstanceGroupName =
                    namingServiceResources.generateInstanceGroupName(instanceGroup, policyInstanceName, nfNamingCode);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        instanceGroup.setInstanceGroupName(generatedInstanceGroupName);
    }

    public void createWanTransportServiceName(BuildingBlockExecution execution) throws BBObjectNotFoundException {
        ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        NamingRequestObject namingRequestObject = new NamingRequestObject();
        namingRequestObject.setExternalKeyValue(serviceInstance.getServiceInstanceId());
        namingRequestObject.setNamingTypeValue(NamingServiceConstants.NAMING_TYPE_SERVICE);
        namingRequestObject.setResourceNameValue(NamingServiceConstants.RESOURCE_NAME_SERVICE_INSTANCE_NAME);
        namingRequestObject.setPolicyInstanceNameValue(serviceInstance.getModelInfoServiceInstance().getNamingPolicy());
        namingRequestObject.setServiceModelNameValue(serviceInstance.getModelInfoServiceInstance().getModelName());
        namingRequestObject.setModelVersionValue(serviceInstance.getModelInfoServiceInstance().getModelVersion());

        String generatedWanTransportServiceName = "";
        try {
            generatedWanTransportServiceName = namingServiceResources.generateServiceInstanceName(namingRequestObject);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        serviceInstance.setServiceInstanceName(generatedWanTransportServiceName);
    }

    public void createVpnBondingServiceName(BuildingBlockExecution execution) throws BBObjectNotFoundException {
        ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        L3Network network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
        VpnBinding vpnBinding = extractPojosForBB.extractByKey(execution, ResourceKey.VPN_ID);
        NamingRequestObject namingRequestObject = new NamingRequestObject();
        namingRequestObject.setExternalKeyValue(serviceInstance.getServiceInstanceId());
        namingRequestObject.setPolicyInstanceNameValue(serviceInstance.getModelInfoServiceInstance().getNamingPolicy());
        namingRequestObject.setNamingTypeValue(NamingServiceConstants.NAMING_TYPE_SERVICE);
        namingRequestObject.setServiceModelNameValue(serviceInstance.getModelInfoServiceInstance().getModelName());
        namingRequestObject.setModelVersionValue(serviceInstance.getModelInfoServiceInstance().getModelVersion());
        namingRequestObject.setNetworkNameValue(network.getNetworkName());
        namingRequestObject.setVpnNameValue(vpnBinding.getVpnName());
        namingRequestObject.setResourceNameValue(NamingServiceConstants.RESOURCE_NAME_SERVICE_INSTANCE_NAME);

        String generatedVpnBondingServiceName = "";
        try {
            generatedVpnBondingServiceName = namingServiceResources.generateServiceInstanceName(namingRequestObject);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        serviceInstance.setServiceInstanceName(generatedVpnBondingServiceName);
    }
}
