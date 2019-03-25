/*
* ============LICENSE_START=======================================================
* ONAP : DCAE
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.UUID;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.aai.tasks.AAIUpdateTasks;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import org.onap.so.client.cds.beans.ConfigDeployPropertiesForVnf;
import org.onap.so.client.cds.beans.ConfigDeployRequestVnf;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Get vnf related data and config Deploy
 *
 */
@Component
public class ConfigDeployVnf {
	private static final Logger logger = LoggerFactory.getLogger(ConfigDeployVnf.class);
	private final static String ORIGINATOR_ID = "SO";
	private final static String ACTION_NAME = "config-deploy";
	private final static String MODE = "async";

	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private AAIUpdateTasks aaiUpdateTask;
	
	/**
	 * Update vnf orch status to configure in AAI
	 * 
	 * @param execution
	 */
	public void updateAAIConfigure(BuildingBlockExecution execution) {
		aaiUpdateTask.updateOrchestrationStausConfigDeployConfigureVnf(execution);

	}
	/**
	 * Getting the vnf object and set in execution object
	 * 
	 * @param execution
	 * 
	 * 
	 */
	public void preProcessAbstractCDSProcessing(BuildingBlockExecution execution) {
		
		
		logger.info("Start preProcessAbstractCDSProcessing");
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);

			ConfigDeployPropertiesForVnf configDeployPropertiesForVnf = new ConfigDeployPropertiesForVnf();
			configDeployPropertiesForVnf.setServiceInstanceId(serviceInstance.getServiceInstanceId());
			configDeployPropertiesForVnf.setServiceModelUuid(serviceInstance.getModelInfoServiceInstance().getModelUuid());
			configDeployPropertiesForVnf.setVnfCustomizationUuid(vnf.getModelInfoGenericVnf().getModelCustomizationUuid());
			configDeployPropertiesForVnf.setVnfId(vnf.getVnfId());
			configDeployPropertiesForVnf.setVnfName(vnf.getVnfName());

			ConfigDeployRequestVnf configDeployRequestVnf = new ConfigDeployRequestVnf();

			configDeployRequestVnf.setResolutionKey(vnf.getVnfName());
			configDeployRequestVnf.setConfigDeployPropertiesForVnf(configDeployPropertiesForVnf);

			String blueprintName = vnf.getBlueprintName();
			String blueprintVersion = vnf.getBlueprintVersion();
			AbstractCDSPropertiesBean abstractCDSPropertiesBean = new AbstractCDSPropertiesBean();
			
			abstractCDSPropertiesBean.setBlueprintName(blueprintName);
			abstractCDSPropertiesBean.setBlueprintVersion(blueprintVersion);
			abstractCDSPropertiesBean.setRequestObject(configDeployRequestVnf.toString());
			

			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

			abstractCDSPropertiesBean.setOriginatorId( ORIGINATOR_ID);
			abstractCDSPropertiesBean.setRequestId(gBBInput.getRequestContext().getMsoRequestId());
			abstractCDSPropertiesBean.setSubRequestId(UUID.randomUUID().toString());
			abstractCDSPropertiesBean.setActionName(ACTION_NAME);
			abstractCDSPropertiesBean.setMode(MODE);
			
			execution.setVariable("executionObject", abstractCDSPropertiesBean);
			
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	/**
	 * Update vnf orch status to configured in AAI
	 * 
	 * @param execution
	 */
	public void updateAAIConfigured(BuildingBlockExecution execution) {
		aaiUpdateTask.updateOrchestrationStausConfigDeployConfiguredVnf(execution);

	}
}