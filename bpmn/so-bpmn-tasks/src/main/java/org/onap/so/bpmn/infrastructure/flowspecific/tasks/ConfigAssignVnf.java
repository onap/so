/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.Map;
import java.util.UUID;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.beans.ConfigAssignPropertiesForVnf;
import org.onap.so.client.cds.beans.ConfigAssignRequestVnf;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Get vnf related data and config assign
 *
 */
@Component
public class ConfigAssignVnf {

    private static final Logger logger = LoggerFactory.getLogger(ConfigAssignVnf.class);
    private static final String ORIGINATOR_ID = "SO";
    private static final String ACTION_NAME = "config-assign";
    private static final String MODE = "sync";

    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    /**
     * Getting the vnf data, blueprint name, blueprint version etc and setting them
     * in execution object and calling the subprocess.
     * 
     * @param execution
     */
    public void preProcessAbstractCDSProcessing(BuildingBlockExecution execution) {
        logger.info("Start preProcessAbstractCDSProcessing ");
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);

            Map<String, Object> userParams = execution.getGeneralBuildingBlock().getRequestContext().getUserParams();

            ConfigAssignPropertiesForVnf configAssignPropertiesForVnf = new ConfigAssignPropertiesForVnf();
            configAssignPropertiesForVnf.setServiceInstanceId(serviceInstance.getServiceInstanceId());
            configAssignPropertiesForVnf
                    .setServiceModelUuid(serviceInstance.getModelInfoServiceInstance().getModelUuid());
            configAssignPropertiesForVnf
                    .setVnfCustomizationUuid(vnf.getModelInfoGenericVnf().getModelCustomizationUuid());
            configAssignPropertiesForVnf.setVnfId(vnf.getVnfId());
            configAssignPropertiesForVnf.setVnfName(vnf.getVnfName());

            for (Map.Entry<String, Object> entry : userParams.entrySet()) {
                configAssignPropertiesForVnf.setUserParam(entry.getKey(), entry.getValue());
            }

            ConfigAssignRequestVnf configAssignRequestVnf = new ConfigAssignRequestVnf();
            configAssignRequestVnf.setResolutionKey(vnf.getVnfName());
            configAssignRequestVnf.setConfigAssignPropertiesForVnf(configAssignPropertiesForVnf);

            String blueprintName = vnf.getBlueprintName();
            String blueprintVersion = vnf.getBlueprintVersion();

            AbstractCDSPropertiesBean abstractCDSPropertiesBean = new AbstractCDSPropertiesBean();

            abstractCDSPropertiesBean.setBlueprintName(blueprintName);
            abstractCDSPropertiesBean.setBlueprintVersion(blueprintVersion);
            abstractCDSPropertiesBean.setRequestObject(configAssignRequestVnf.toString());

            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            abstractCDSPropertiesBean.setOriginatorId(ORIGINATOR_ID);
            abstractCDSPropertiesBean.setRequestId(gBBInput.getRequestContext().getMsoRequestId());
            abstractCDSPropertiesBean.setSubRequestId(UUID.randomUUID().toString());
            abstractCDSPropertiesBean.setActionName(ACTION_NAME);
            abstractCDSPropertiesBean.setMode(MODE);
            execution.setVariable("executionObject", abstractCDSPropertiesBean);

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

}
