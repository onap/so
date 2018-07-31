/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.adapter.vnf.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.FileUtil;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

public class VnfAdapterImplTest extends BaseTaskTest {
	@Autowired
	private VnfAdapterImpl vnfAdapterImpl;

	private RequestContext requestContext;
	private ServiceInstance serviceInstance;
	private VfModule vfModule;
    private VolumeGroup volumeGroup;
	
	private static final String VNF_ADAPTER_REST_DELETE_RESPONSE = FileUtil.readResourceFile("__files/VfModularity/VNFAdapterRestDeleteResponse.xml");
	private static final String VNF_ADAPTER_REST_CREATE_RESPONSE =  FileUtil.readResourceFile("__files/VfModularity/VNFAdapterRestCreateCallback.xml");
    private static final String VNF_ADAPTER_VOLUME_CREATE_RESPONSE =  FileUtil.readResourceFile("__files/VfModularity/CreateVfModuleVolumeCallbackResponse.xml");
	private static final String TEST_VFMODULE_HEATSTACK_ID = "slowburn";
    private static final String TEST_VOLUME_HEATSTACK_ID = "testHeatStackId1";

	@Before
	public void before() {
		requestContext = setRequestContext();
		serviceInstance = setServiceInstance();
		vfModule = setVfModule();
        volumeGroup = setVolumeGroup();
		vfModule.setHeatStackId(null);
	}

	@Test
	public void preProcessVnfAdapterTest() {
		vnfAdapterImpl.preProcessVnfAdapter(execution);

		assertEquals("true", execution.getVariable("isDebugLogEnabled"));
		assertEquals(requestContext.getMsoRequestId(), execution.getVariable("mso-request-id"));
		assertEquals(serviceInstance.getServiceInstanceId(), execution.getVariable("mso-service-instance-id"));
	}

	@Test
	public void postProcessVnfAdapter_CreateResponseTest() {
		execution.setVariable("vnfAdapterRestV1Response", VNF_ADAPTER_REST_CREATE_RESPONSE);
		vnfAdapterImpl.postProcessVnfAdapter(execution);
		assertEquals(TEST_VFMODULE_HEATSTACK_ID, vfModule.getHeatStackId());
	}
	

	@Test
	public void postProcessVnfAdapter_CreateResponseTest_EmptyCreateVfModuleResponseTag() {
        expectedException.expect(BpmnError.class);
		execution.setVariable("vnfAdapterRestV1Response", "<vfModuleStackId></vfModuleStackId>");
		vnfAdapterImpl.postProcessVnfAdapter(execution);
	}
	
	@Test
	public void postProcessVnfAdapter_CreateResponseTest_EmptyVfModuleStackIdTag() {
		execution.setVariable("vnfAdapterRestV1Response", "<createVfModuleResponse></createVfModuleResponse>");
		vnfAdapterImpl.postProcessVnfAdapter(execution);
		assertNull(vfModule.getHeatStackId());
	}
	
	@Test
	public void postProcessVnfAdapter_CreateResponseTest_EmptyHeatStackId() {
		execution.setVariable("vnfAdapterRestV1Response", "<createVfModuleResponse><vfModuleStackId></vfModuleStackId></createVfModuleResponse>");
		vnfAdapterImpl.postProcessVnfAdapter(execution);
		assertNull(vfModule.getHeatStackId());
	}

	@Test
	public void postProcessVnfAdapter_DeleteResponseTest() {
		execution.setVariable("vnfAdapterRestV1Response", VNF_ADAPTER_REST_DELETE_RESPONSE);
		vnfAdapterImpl.postProcessVnfAdapter(execution);
		assertNull(vfModule.getHeatStackId());
	}
	
	@Test
	public void postProcessVnfAdapter_ResponseNullTest() {
		execution.setVariable("vnfAdapterRestV1Response", null);
		vnfAdapterImpl.postProcessVnfAdapter(execution);
		assertNull(vfModule.getHeatStackId());
	}
	
	@Test
	public void postProcessVnfAdapter_ResponseEmptyTest() {
        execution.setVariable("vnfAdapterRestV1Response", "");
		vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertNull(vfModule.getHeatStackId());
	}
	
	@Test
	public void postProcessVnfAdapter_DeleteResponseTest_VfModuleDeletedFalse() {
		execution.setVariable("vnfAdapterRestV1Response", "<deleteVfModuleResponse><vfModuleDeleted>false</vfModuleDeleted></deleteVfModuleResponse>");
		vnfAdapterImpl.postProcessVnfAdapter(execution);
		assertNull(vfModule.getHeatStackId());
	}
	
	@Test
	public void postProcessVnfAdapter_DeleteResponseTest_EmptyDeleteVfModuleResponseTag() {
        expectedException.expect(BpmnError.class);
		execution.setVariable("vnfAdapterRestV1Response", "<vfModuleDeleted></vfModuleDeleted>");
		vnfAdapterImpl.postProcessVnfAdapter(execution);
	}
	
	@Test
	public void postProcessVnfAdapter_DeleteResponseTest_EmptyVfModuleDeletedTag() {
		execution.setVariable("vnfAdapterRestV1Response", "<deleteVfModuleResponse></deleteVfModuleResponse>");
		vnfAdapterImpl.postProcessVnfAdapter(execution);
		assertNull(vfModule.getHeatStackId());
	}

	@Test
	public void preProcessVnfAdapterExceptionTest() {
		expectedException.expect(BpmnError.class);
		lookupKeyMap.clear();
		vnfAdapterImpl.preProcessVnfAdapter(execution);
	}

    @Test
    public void postProcessVnfAdapter_CreateVolumeResponseTest() {
        execution.setVariable("vnfAdapterRestV1Response", VNF_ADAPTER_VOLUME_CREATE_RESPONSE);
        vnfAdapterImpl.postProcessVnfAdapter(execution);
        assertEquals(TEST_VOLUME_HEATSTACK_ID, volumeGroup.getHeatStackId());
    }

    @Test
    public void postProcessVnfAdapter_CreateVolumeEmptyResponseTest()  {
        expectedException.expect(BpmnError.class);
        execution.setVariable("vnfAdapterRestV1Response", "<createVolumeGroupResponse></createVolumeGroupResponse>");
        vnfAdapterImpl.postProcessVnfAdapter(execution);
    }

	@Test
	public void postProcessVnfAdapterExceptionTest() {
		execution.setVariable("vnfAdapterRestV1Response", VNF_ADAPTER_REST_CREATE_RESPONSE);
		expectedException.expect(BpmnError.class);
		lookupKeyMap.clear();
		vnfAdapterImpl.postProcessVnfAdapter(execution);
	}
}
