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
package com.att.ecomp.mso.bpmn.validations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.so.BuildingBlockTestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoVfModule;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

@RunWith(MockitoJUnitRunner.class)
public class VerifyVfModuleActionAllowedTest extends BuildingBlockTestDataSetup {

	@Spy
	private ExtractPojosForBB extractPojosForBB;
	
	@Spy
	private ExceptionBuilder exceptionUtil;

	@InjectMocks
	private VerifyVfModuleActionAllowed verifyVfModuleActionAllowed = new VerifyVfModuleActionAllowed();
	
	private GenericVnf genericVnf;
	private VfModule vfModule;	
	
	@Before
	public void before() {			
		genericVnf = setGenericVnf();		
		vfModule = setVfModule();		
	}	
	
	
	@Test
	public void flowsToBeCalledTest() {
		
		Set<String> flows = new HashSet<>();
		
		flows.add("UnassignVfModuleBB");
		flows.add("DeleteVfModuleBB");
		flows.add("DeactivateVfModuleBB");
		
		assertEquals(flows, verifyVfModuleActionAllowed.forBuildingBlock());
	}
	@Test
	public void checkIfVfModuleDeactivateAllowed_NotBaseModule_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "DeactivateVfModuleBB");
		vfModule.getModelInfoVfModule().setIsBaseBoolean(false);		
		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertTrue(result);
	}
	
	@Test
	public void checkIfVfModuleDeactivateAllowed_CorrectStatus_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "DeactivateVfModuleBB");
		dataSetup(true, false, Optional.of(OrchestrationStatus.ASSIGNED));
		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertTrue(result);
	}
	
	@Test
	public void checkIfVfModuleDeactivateAllowed_IncorrectStatus_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "DeactivateVfModuleBB");		
		dataSetup(true, false, Optional.of(OrchestrationStatus.ACTIVE));
		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertFalse(result);
	}
	
	@Test
	public void checkIfVfModuleDeleteAllowed_NotBaseModule_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "DeleteVfModuleBB");
		vfModule.getModelInfoVfModule().setIsBaseBoolean(false);		
		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertTrue(result);
	}
	
	@Test
	public void checkIfVfModuleDeleteAllowed_CorrectStatus_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "DeleteVfModuleBB");
		dataSetup(true, false, Optional.of(OrchestrationStatus.ASSIGNED));
		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertTrue(result);
	}
	
	@Test
	public void checkIfVfModuleDeleteAllowed_IncorrectStatus_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "DeleteVfModuleBB");
		dataSetup(true, false, Optional.of(OrchestrationStatus.ACTIVE));

		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertFalse(result);
	}
	
	@Test
	public void checkIfVfModuleDeleteAllowed_IncorrectStatusCreated_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "DeleteVfModuleBB");
		dataSetup(true, false, Optional.of(OrchestrationStatus.CREATED));
		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertFalse(result);
	}
	
	@Test
	public void checkIfVfModuleUnassignAllowed_NotBaseModule_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "UnassignVfModuleBB");
		vfModule.getModelInfoVfModule().setIsBaseBoolean(false);		
		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertTrue(result);
	}
	
	@Test
	public void checkIfVfModuleUnassignAllowed_NoMoreModules_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "UnassignVfModuleBB");
		vfModule.getModelInfoVfModule().setIsBaseBoolean(true);		
		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertTrue(result);
	}
	
	@Test
	public void checkIfVfModuleUnassignAllowed_AddonExists_Test() throws Exception {
		execution.setVariable("flowToBeCalled", "UnassignVfModuleBB");				
		dataSetup(true, false, Optional.empty());
		boolean result = verifyVfModuleActionAllowed.validate(execution);
		assertFalse(result);
	}
	
	private void dataSetup(boolean vfmoduleIsBaseBoolean, boolean vfmoduleModelInfoIsBaseBoolean, Optional<OrchestrationStatus> orchestrationStatus) {
		vfModule.getModelInfoVfModule().setIsBaseBoolean(vfmoduleIsBaseBoolean);
		VfModule vfModule1 = new VfModule();
		ModelInfoVfModule modelInfoVfModule1 = new ModelInfoVfModule();
		modelInfoVfModule1.setIsBaseBoolean(vfmoduleModelInfoIsBaseBoolean);
		vfModule1.setModelInfoVfModule(modelInfoVfModule1);
		if (orchestrationStatus.isPresent()) {
			vfModule1.setOrchestrationStatus(orchestrationStatus.get());
		}
		genericVnf.getVfModules().add(vfModule1);
	}
}
