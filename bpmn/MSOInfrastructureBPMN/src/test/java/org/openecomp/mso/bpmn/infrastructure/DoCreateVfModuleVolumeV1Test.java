/*- 
 * ============LICENSE_START======================================================= 
 * OPENECOMP - MSO 
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

package org.openecomp.mso.bpmn.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.WorkflowTest.CallbackSet;
import org.openecomp.mso.bpmn.mock.FileUtil;

public class DoCreateVfModuleVolumeV1Test extends WorkflowTest {
	
	public static final String _prefix = "CVFMODVOL2_";
	
	private final CallbackSet callbacks = new CallbackSet();

	public DoCreateVfModuleVolumeV1Test() throws IOException {
		callbacks.put("volumeGroupCreate", FileUtil.readResourceFile(
				"__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeCallbackResponse.xml"));
		callbacks.put("volumeGroupRollback", FileUtil.readResourceFile(
				"__files/DoCreateVfModuleVolumeV1/RollbackVfModuleVolumeCallbackResponse.xml"));
	}

	/**
	 * Happy Path
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestHappyPath() throws Exception {

		logStart();
		
		DoCreateVfModuleVolume_Success();
		
		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeRequest.xml");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("DCVFMODVOLV1_volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
		testVariables.put("vnf-id", "TEST-VNF-ID-0123");
		testVariables.put("volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		//testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
		testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
		invokeAsyncProcess("DoCreateVfModuleVolumeV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		
		injectVNFRestCallbacks(callbacks, "volumeGroupCreate");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "DCVFMODVOLV1_SuccessIndicator", true);
		
		logEnd();
	}
	
	@Test
	@Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestVolumeGroupExistError() throws Exception {

		logStart();
		
		DoCreateVfModuleVolume_VolumeGroupExistsFail();
		
		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeRequest.xml");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("DCVFMODVOLV1_volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
		testVariables.put("vnf-id", "TEST-VNF-ID-0123");
		testVariables.put("volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
		invokeAsyncProcess("DoCreateVfModuleVolumeV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		
		//injectVNFRestCallbacks(callbacks, "volumeGroupCreate");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "DCVFMODVOLV1_SuccessIndicator", false);
		
		logEnd();
	}
	
	/**
	 * Will trigger AAI create rollback
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestVnfVolumeGroupCreateError() throws Exception {

		logStart();
		
		DoCreateVfModuleVolume_VnfCreateVolumeGroupFail();
		
		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeRequest.xml");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("DCVFMODVOLV1_volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
		testVariables.put("vnf-id", "TEST-VNF-ID-0123");
		testVariables.put("volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
		testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
		invokeAsyncProcess("DoCreateVfModuleVolumeV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		
		//injectVNFRestCallbacks(callbacks, "volumeGroupCreate");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "DCVFMODVOLV1_SuccessIndicator", false);
		
		logEnd();
	}
	
	/**
	 * Will trigger AAI create rollback
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestUpdateAaiVolumeGroupError() throws Exception {

		logStart();
		
		DoCreateVfModuleVolume_AaiVolumeGroupUpdateFail();
		
		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeRequest.xml");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("DCVFMODVOLV1_volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
		testVariables.put("vnf-id", "TEST-VNF-ID-0123");
		testVariables.put("volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
		testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
		invokeAsyncProcess("DoCreateVfModuleVolumeV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		
		injectVNFRestCallbacks(callbacks, "volumeGroupCreate,volumeGroupRollback");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "DCVFMODVOLV1_SuccessIndicator", false);
		
		logEnd();
	}		

	/**
	 * Will trigger not trigger rollback
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = {"subprocess/DoCreateVfModuleVolumeV1.bpmn",
            "subprocess/FalloutHandler.bpmn",
            "subprocess/CompleteMsoProcess.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn",
            "subprocess/VnfAdapterRestV1.bpmn"})
	public void TestUpdateAaiVolumeGroupErrorNoRollback() throws Exception {

		logStart();
		
		DoCreateVfModuleVolume_AaiVolumeGroupUpdateFail();
		
		String businessKey = UUID.randomUUID().toString();
		String createVfModuleVolRequest = FileUtil.readResourceFile("__files/DoCreateVfModuleVolumeV1/CreateVfModuleVolumeNoRollbackRequest.xml");
		
		Map<String, Object> testVariables = new HashMap<String, Object>();
		testVariables.put("DCVFMODVOLV1_volumeGroupId", "TEST-VOLUME-VOLUME-GROUP-ID-0123");
		testVariables.put("vnf-id", "TEST-VNF-ID-0123");
		testVariables.put("volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
		testVariables.put("test-volume-group-name", "TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0");
		testVariables.put("test-volume-group-id", "TEST-VOLUME-GROUP-ID-0123");
		testVariables.put("DoCreateVfModuleVolumeV1Request", createVfModuleVolRequest);
		invokeAsyncProcess("DoCreateVfModuleVolumeV1", "v1", businessKey, createVfModuleVolRequest, testVariables);
		
		injectVNFRestCallbacks(callbacks, "volumeGroupCreate");
		
		waitForProcessEnd(businessKey, 100000);
		checkVariable(businessKey, "DCVFMODVOLV1_SuccessIndicator", false);
		
		logEnd();
	}

	public static void DoCreateVfModuleVolume_Success() {
		// Notes:
		// 1. initial aai volume group by name - /aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group?volume-group-name=TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0
		// 2. Create volume group - /aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group
		// 3. Requery Volume Group - /aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group?volume-group-name=MSOTESTVOL101a-vSAMP12_base_vol_module-0
		// 4. Update volume group (id from requery response - /aai/v8/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group/8424bb3c-c3e7-4553-9662-469649ed9379

		//generic vnf
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/TEST-VNF-ID-0123"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/GenericVnf.xml")));
		//create volume group
		stubFor(put(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group/TEST-VOLUME-GROUP-ID-0123"))
				  .willReturn(aResponse()
				  .withStatus(201)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_createVolumeName_AAIResponse_Success.xml")));
		//requery volume group
		stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups[?]volume-group-name=MSOTESTVOL101a-vSAMP12_base_vol_module-0"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_queryVolumeName_AAIResponse_Success.xml")));
		//update volume group
		stubFor(put(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group/8424bb3c-c3e7-4553-9662-469649ed9379"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_updateVolumeName_AAIResponse_Success.xml")));
		// VNF rest Adapter
 		stubFor(post(urlEqualTo("/vnfs/v1/volume-groups"))
 				.willReturn(aResponse()
 					.withStatus(202)
 					.withHeader("Content-Type", "application/xml")));
	}

	public static void DoCreateVfModuleVolume_VolumeGroupExistsFail() {
		//generic vnf
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/TEST-VNF-ID-0123"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/GenericVnf.xml")));
		//initial volume group query
		stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group[?]volume-group-name=TEST-MSOTESTVOL101a-vSAMP12_base_vol_module-0"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_queryVolumeName_AAIResponse_Success.xml")));
	}

	public static void DoCreateVfModuleVolume_VnfCreateVolumeGroupFail() {
		//generic vnf
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/TEST-VNF-ID-0123"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/GenericVnf.xml")));
		//create volume group
		stubFor(put(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group/TEST-VOLUME-GROUP-ID-0123"))
				  .willReturn(aResponse()
				  .withStatus(201)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_createVolumeName_AAIResponse_Success.xml")));
		//Query AAI volume group by name -- needed before delete
		stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups[?]volume-group-name=MSOTESTVOL101a-vSAMP12_base_vol_module-0"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_queryVolumeName_AAIResponse_Success.xml")));
		//delete volume group in aai
		stubFor(delete(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group/8424bb3c-c3e7-4553-9662-469649ed9379[?]resource-version=1460134360"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_deleteVolumeName_AAIResponse_Success.xml")));
 		stubFor(post(urlEqualTo("/vnfs/v1/volume-groups"))
 				.willReturn(aResponse()
 					.withStatus(404)
 					.withHeader("Content-Type", "application/xml")));
	}

	public static void DoCreateVfModuleVolume_AaiVolumeGroupUpdateFail() {
		//generic vnf
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/TEST-VNF-ID-0123"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/GenericVnf.xml")));
		//create volume group
		stubFor(put(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups/volume-group/TEST-VOLUME-GROUP-ID-0123"))
				  .willReturn(aResponse()
				  .withStatus(201)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_createVolumeName_AAIResponse_Success.xml")));
		//requery volume group
		stubFor(get(urlMatching("/aai/v[0-9]+/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25/volume-groups[?]volume-group-name=MSOTESTVOL101a-vSAMP12_base_vol_module-0"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_queryVolumeName_AAIResponse_Success.xml")));
		//delete volume group in aai
		stubFor(delete(urlMatching("/aai/v[0-9]+/cloud-infrastructure/volume-groups/volume-group/8424bb3c-c3e7-4553-9662-469649ed9379[?]resource-version=1460134360"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DoCreateVfModuleVolumeV1/createVfModuleVolume_deleteVolumeName_AAIResponse_Success.xml")));
		// VNF rest Adapter
 		stubFor(post(urlEqualTo("/vnfs/v1/volume-groups"))
 				.willReturn(aResponse()
 					.withStatus(202)
 					.withHeader("Content-Type", "application/xml")));
 		// VNF Rest Adapter rollback - vnfs/v1/volume-groups/STUB-TEST-8424bb3c-c3e7-4553-9662-469649ed9379/rollback
 		stubFor(delete(urlEqualTo("/vnfs/v1/volume-groups/TEST-VOLUME-GROUP-ID-0123/rollback"))
 				.willReturn(aResponse()
 				.withStatus(202)
 				.withHeader("Content-Type", "application/xml")));
	}
}
