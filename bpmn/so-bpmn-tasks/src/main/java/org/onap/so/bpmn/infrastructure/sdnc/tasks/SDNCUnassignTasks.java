/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import org.onap.sdnc.northbound.client.model.GenericResourceApiNetworkOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiServiceOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVfModuleOperationInformation;
import org.onap.sdnc.northbound.client.model.GenericResourceApiVnfOperationInformation;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.SDNCNetworkResources;
import org.onap.so.client.orchestration.SDNCServiceInstanceResources;
import org.onap.so.client.orchestration.SDNCVfModuleResources;
import org.onap.so.client.orchestration.SDNCVnfResources;
import org.onap.so.client.sdnc.beans.SDNCRequest;
import org.onap.so.client.sdnc.endpoint.SDNCTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SDNCUnassignTasks extends AbstractSDNCTask {

    private static final Logger logger = LoggerFactory.getLogger(SDNCUnassignTasks.class);
    public static final String SDNC_REQUEST = "SDNCRequest";
    @Autowired
    private SDNCServiceInstanceResources sdncSIResources;
    @Autowired
    private SDNCVfModuleResources sdncVfModuleResources;
    @Autowired
    private SDNCVnfResources sdncVnfResources;
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private SDNCNetworkResources sdncNetworkResources;
    @Autowired
    private Environment env;

    /**
     * This method is used to prepare a SDNC request and set it to the execution Object.
     *
     * Which is used for unassign the ServiceInstance.
     *
     * @param execution
     */
    public void unassignServiceInstance(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            RequestContext requestContext = gBBInput.getRequestContext();
            Customer customer = gBBInput.getCustomer();
            GenericResourceApiServiceOperationInformation req =
                    sdncSIResources.unassignServiceInstance(serviceInstance, customer, requestContext);
            SDNCRequest sdncRequest = new SDNCRequest();
            sdncRequest.setSDNCPayload(req);
            sdncRequest.setTopology(SDNCTopology.SERVICE);
            execution.setVariable(SDNC_REQUEST, sdncRequest);
        } catch (Exception ex) {
            logger.error("Exception occurred in SDNCUnassignTasks unassignServiceInstance", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * This method is used to prepare a SDNC request and set it to the execution Object.
     *
     * Which is used for unassign the VfModule.
     *
     * @param execution
     */
    public void unassignVfModule(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            RequestContext requestContext = gBBInput.getRequestContext();
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            SDNCRequest sdncRequest = new SDNCRequest();
            GenericResourceApiVfModuleOperationInformation req = sdncVfModuleResources.unassignVfModule(vfModule, vnf,
                    serviceInstance, requestContext, buildCallbackURI(sdncRequest));
            sdncRequest.setSDNCPayload(req);
            sdncRequest.setTopology(SDNCTopology.VFMODULE);
            execution.setVariable(SDNC_REQUEST, sdncRequest);
        } catch (Exception ex) {
            logger.error("Exception occurred in SDNCUnassignTasks unassignVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * This method is used to prepare a SDNC request and set it to the execution Object.
     *
     * Which is used for unassign the Vnf.
     *
     * @param execution
     */
    public void unassignVnf(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            RequestContext requestContext = gBBInput.getRequestContext();
            Customer customer = gBBInput.getCustomer();
            CloudRegion cloudRegion = gBBInput.getCloudRegion();
            SDNCRequest sdncRequest = new SDNCRequest();
            GenericResourceApiVnfOperationInformation req = sdncVnfResources.unassignVnf(vnf, serviceInstance, customer,
                    cloudRegion, requestContext, buildCallbackURI(sdncRequest));
            sdncRequest.setSDNCPayload(req);
            sdncRequest.setTopology(SDNCTopology.VNF);
            execution.setVariable(SDNC_REQUEST, sdncRequest);
        } catch (Exception ex) {
            logger.error("Exception occurred in SDNCUnassignTasks unassignVnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * This method is used to prepare a SDNC request and set it to the execution Object.
     *
     * Which is used for unassign the Network.
     *
     * @param execution
     */
    public void unassignNetwork(BuildingBlockExecution execution) throws Exception {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            L3Network network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            Customer customer = gBBInput.getCustomer();
            RequestContext requestContext = gBBInput.getRequestContext();
            CloudRegion cloudRegion = gBBInput.getCloudRegion();
            String cloudRegionSdnc = execution.getVariable("cloudRegionSdnc");
            cloudRegion.setLcpCloudRegionId(cloudRegionSdnc);
            GenericResourceApiNetworkOperationInformation req = sdncNetworkResources.unassignNetwork(network,
                    serviceInstance, customer, requestContext, cloudRegion);
            SDNCRequest sdncRequest = new SDNCRequest();
            sdncRequest.setSDNCPayload(req);
            sdncRequest.setTopology(SDNCTopology.NETWORK);
            execution.setVariable(SDNC_REQUEST, sdncRequest);
        } catch (Exception ex) {
            logger.error("Exception occurred in SDNCUnassignTasks unassignNetwork", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
