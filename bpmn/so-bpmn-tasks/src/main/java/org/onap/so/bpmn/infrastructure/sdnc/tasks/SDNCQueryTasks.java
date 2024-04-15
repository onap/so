/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.SDNCVnfResources;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.client.orchestration.SDNCVfModuleResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used for querying the SDNC.
 */
@Component
public class SDNCQueryTasks {
    private static final Logger logger = LoggerFactory.getLogger(SDNCQueryTasks.class);
    private static final String NO_RESPONSE_FROM_SDNC = "Error did not receive a response from SDNC.";
    public static final String SDNCQUERY_RESPONSE = "SDNCQueryResponse_";
    @Autowired
    private SDNCVnfResources sdncVnfResources;
    @Autowired
    private SDNCVfModuleResources sdncVfModuleResources;
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    /**
     * BPMN access method to query the SDNC for fetching the vnf details.
     *
     * It will get the vnf details according to service instance id.
     *
     * @param execution
     * @throws Exception
     */
    public void queryVnf(BuildingBlockExecution execution) throws BBObjectNotFoundException {
        ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

        String selfLink =
                "rests/data/GENERIC-RESOURCE-API:services/service/" + serviceInstance.getServiceInstanceId()
                        + "/service-data/vnfs/vnf/" + genericVnf.getVnfId() + "/vnf-data/vnf-topology/";
        try {
            if (genericVnf.getSelflink() == null) {
                genericVnf.setSelflink(selfLink);
            }
            String response = sdncVnfResources.queryVnf(genericVnf);
            execution.setVariable(SDNCQUERY_RESPONSE + genericVnf.getVnfId(), response);
        } catch (BadResponseException ex) {
            logger.error("Exception occurred", ex);
            if (!ex.getMessage().equals(NO_RESPONSE_FROM_SDNC)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex, ONAPComponents.SDNC);
            } else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex, ONAPComponents.SO);
            }
        } catch (Exception ex) {
            logger.error("Exception occurred", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex, ONAPComponents.SO);
        }
    }


    /**
     * BPMN access method to query the SDNC for fetching the VfModule details.
     *
     * It will get the vnf details according to service instance id, vnf id & Vf module id.
     *
     * @param execution
     * @throws Exception
     */
    public void queryVfModule(BuildingBlockExecution execution) throws BBObjectNotFoundException {
        ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
        VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
        String selfLink = "rests/data/GENERIC-RESOURCE-API:services/service/"
                + serviceInstance.getServiceInstanceId() + "/service-data/vnfs/vnf/" + genericVnf.getVnfId()
                + "/vnf-data/vf-modules/vf-module/" + vfModule.getVfModuleId() + "/vf-module-data/vf-module-topology/";
        try {
            if (vfModule.getSelflink() == null
                    || (vfModule.getSelflink() != null && vfModule.getSelflink().isEmpty())) {
                vfModule.setSelflink(selfLink);
            }
            if (vfModule.getSelflink() != null && !vfModule.getSelflink().isEmpty()) {
                String response = sdncVfModuleResources.queryVfModule(vfModule);
                execution.setVariable(SDNCQUERY_RESPONSE + vfModule.getVfModuleId(), response);
            } else {
                throw new Exception("Vf Module " + vfModule.getVfModuleId()
                        + " exists in gBuildingBlock but does not have a selflink value");
            }
        } catch (BadResponseException ex) {
            logger.error("Exception occurred for BadResponse ", ex);
            if (!ex.getMessage().equals(NO_RESPONSE_FROM_SDNC)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex, ONAPComponents.SDNC);
            } else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex, ONAPComponents.SO);
            }
        } catch (Exception ex) {
            logger.error("Exception occurred", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to query the SDNC for fetching the VfModuleForVolumeGroup details.
     *
     * It will get the vnf details according to Vf module id.
     *
     * @param execution @throws
     */
    public void queryVfModuleForVolumeGroup(BuildingBlockExecution execution) {
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            if (vfModule.getSelflink() != null && !vfModule.getSelflink().isEmpty()) {
                String response = sdncVfModuleResources.queryVfModule(vfModule);
                execution.setVariable(SDNCQUERY_RESPONSE + vfModule.getVfModuleId(), response);
            } else {
                throw new Exception("Vf Module " + vfModule.getVfModuleId()
                        + " exists in gBuildingBlock but does not have a selflink value");
            }
        } catch (BBObjectNotFoundException bbException) {
            logger.error("Error occurred if bb object not found in SDNCQueryTasks queryVfModuleForVolumeGroup ",
                    bbException);
            // If there is not a vf module in the general building block, we will not call
            // SDNC and proceed as normal
            // without throwing an error
            // If we see a bb object not found exception for something that is not a vf
            // module id, then we should throw
            // the error as normal
            if (!ResourceKey.VF_MODULE_ID.equals(bbException.getResourceKey())) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, bbException, ONAPComponents.SO);
            }
        } catch (BadResponseException ex) {
            logger.error("Error occurred for BadResponseException in SDNCQueryTasks queryVfModuleForVolumeGroup ", ex);
            if (!ex.getMessage().equals(NO_RESPONSE_FROM_SDNC)) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex, ONAPComponents.SDNC);
            } else {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex, ONAPComponents.SO);
            }
        } catch (Exception ex) {
            logger.error("Exception occurred", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex, ONAPComponents.SO);
        }
    }
}
