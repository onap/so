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

package org.onap.so.bpmn.infrastructure.sdnc.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.SDNCVnfResources;
import org.onap.so.client.orchestration.SDNCVfModuleResources;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SDNCQueryTasks {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, SDNCQueryTasks.class);	
	@Autowired
	private SDNCVnfResources sdncVnfResources;
	@Autowired
	private SDNCVfModuleResources sdncVfModuleResources;
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	
	public void queryVnf(BuildingBlockExecution execution) throws Exception {		
		GenericVnf genericVnf =  extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
				
		try {
			String response = sdncVnfResources.queryVnf(genericVnf);		
			execution.setVariable("SDNCQueryResponse_" + genericVnf.getVnfId(), response);			
		} catch (Exception ex) {			
		    exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	
	public void queryVfModule(BuildingBlockExecution execution) throws Exception {		
		VfModule vfModule =  extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));		
		
		try {
			if(vfModule.getSelflink() != null && !vfModule.getSelflink().isEmpty()) {	
				String response = sdncVfModuleResources.queryVfModule(vfModule);		
			execution.setVariable("SDNCQueryResponse_" + vfModule.getVfModuleId(), response);			
			}
			else {
				throw new Exception("Vf Module " + vfModule.getVfModuleId() + " exists in gBuildingBlock but does not have a selflink value");
			}
		} catch (Exception ex) {			
		    exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void queryVfModuleForVolumeGroup(BuildingBlockExecution execution) {
		try {
			VfModule vfModule =  extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			if(vfModule.getSelflink() != null && !vfModule.getSelflink().isEmpty()) {
				String response = sdncVfModuleResources.queryVfModule(vfModule);
				execution.setVariable("SDNCQueryResponse_" + vfModule.getVfModuleId(), response);
			}
			else {
				throw new Exception("Vf Module " + vfModule.getVfModuleId() + " exists in gBuildingBlock but does not have a selflink value");
			}
		} catch(BBObjectNotFoundException bbException) {
			// If there is not a vf module in the general building block, we will not call SDNC and proceed as normal without throwing an error
			// If we see a bb object not found exception for something that is not a vf module id, then we should throw the error as normal
			if(!ResourceKey.VF_MODULE_ID.equals(bbException.getResourceKey())) {
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, bbException);
			}
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
