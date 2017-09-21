package org.openecomp.mso.bpmn.vcpe;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetCustomer;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetServiceInstance;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockNodeQueryServiceInstanceById;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutAllottedResource;
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutGenericVnf;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.MockGetServiceResourcesCatalogData;
import static org.openecomp.mso.bpmn.mock.StubResponseDatabase.mockUpdateRequestDB;
import static org.openecomp.mso.bpmn.mock.StubResponseSDNCAdapter.mockSDNCAdapter;
//		TODO: uncomment when Homing BB is merged
// import static org.openecomp.mso.bpmn.mock.StubResponseSNIRO.mockSNIRO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.test.Deployment;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.WorkflowTest;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowResponse;
import org.openecomp.mso.bpmn.mock.FileUtil;

public class CreateVcpeResCustServiceTest extends WorkflowTest {

	private final CallbackSet callbacks = new CallbackSet();
	private final String request;
	
	public CreateVcpeResCustServiceTest() throws IOException {
		callbacks.put("assign", FileUtil.readResourceFile("__files/VCPE/VfModularity/SDNCTopologyAssignCallback.xml"));
		callbacks.put("query", FileUtil.readResourceFile("__files/VCPE/SDNCTopologyQueryCallbackNetworkInstance.xml"));
		callbacks.put("activate", FileUtil.readResourceFile("__files/VCPE/VfModularity/SDNCTopologyActivateCallback.xml"));
		callbacks.put("vnfCreate", FileUtil.readResourceFile("__files/VCPE/VfModularity/VNFAdapterRestCreateCallback.xml"));
		callbacks.put("create", FileUtil.readResourceFile("__files/VCPE/VfModularity/SDNCTopologyCreateCallback.xml"));
		callbacks.put("queryTXC", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceTXC/SDNCTopologyQueryCallback.xml"));
		callbacks.put("queryBRG", FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceBRG/SDNCTopologyQueryCallback.xml"));
		
		callbacks.put("sniro", JSON, "SNIROResponse", FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallback2AR1Vnf"));
		callbacks.put("sniro2", JSON, "SNIROResponse", FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallback2AR1Vnf2Net"));
		callbacks.put("sniroNoSol", JSON, "SNIROResponse", FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallbackNoSolutionFound"));
		callbacks.put("sniroPolicyEx", JSON, "SNIROResponse", FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallbackPolicyException"));
		callbacks.put("sniroServiceEx", JSON, "SNIROResponse", FileUtil.readResourceFile("__files/BuildingBlocks/sniroCallbackServiceException"));
		
		request = FileUtil.readResourceFile("__files/VCPE/request.json");
	}


//	/**
//	 * End-to-End flow - Unit test for CreateVcpeResCustService.bpmn
//	 *  - String input & String response
//	 */
//
//	@Test
//	@Ignore
//	@Deployment(resources = {"process/CreateVcpeResCustService.bpmn",
//			                 "subprocess/DoCreateServiceInstance.bpmn",
//			                 "subprocess/DoCreateServiceInstanceRollback.bpmn",
//			                 "subprocess/DoCreateNetworkInstance.bpmn",
//			                 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
//			                 "subprocess/DoCreateVnfAndModules.bpmn",
//			                 "subprocess/DoCreateAllottedResourceTXC.bpmn",
//			                 "subprocess/DoCreateAllottedResourceTXCRollback.bpmn",
//			                 "subprocess/DoCreateAllottedResourceBRG.bpmn",
//			                 "subprocess/DoCreateAllottedResourceBRGRollback.bpmn",
//			                 "subprocess/BuildingBlock/DecomposeService.bpmn",
//			                 "subprocess/BuildingBlock/Homing.bpmn",
//			                 "subprocess/GenericGetService.bpmn",
//			                 "subprocess/GenericPutService.bpmn",
//			                 "subprocess/SDNCAdapterV1.bpmn",
//			                 "subprocess/DoCreateVnf.bpmn",
//			                 "subprocess/GenericGetVnf.bpmn",
//			                 "subprocess/GenericPutVnf.bpmn",
//			                 "subprocess/FalloutHandler.bpmn",
//			                 "subprocess/GenericDeleteService.bpmn",
//			                 "subprocess/ReceiveWorkflowMessage.bpmn",
//	                         "subprocess/CompleteMsoProcess.bpmn"})
//
//	public void invokeCreateServiceInstanceInfra_Success() throws Exception {
//
//		logStart();
//
//		// setup simulators
//		//MockGetCustomer_VCPE();
//		MockGetCustomer("MCBH-1610", "VCPE/getCustomer.xml");
//		//MockGetNetworkCatalogData_VCPE();
//		MockGetNetworkCatalogData("uuid-miu-svc-011-abcdef", "2", "VCPE/getCatalogNetworkData.json");
//		//MockGetVnfCatalogData_VCPE();
//		MockGetVnfCatalogData("uuid-miu-svc-011-abcdef", "2", "VCPE/getCatalogVnfData.json");
//		//MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "VCPE/getCatalogServiceResourcesData.json");
//		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "VCPE/getCatalogServiceResourcesDataNoNetworkVnf.json");
//		MockGetServiceInstanceById_VCPE();
//		MockPutServiceInstance_VCPE();
//
//		//SDNC Adapter Mocks
//		mockSDNCAdapterRest();
//		mockSDNCAdapter();
//
//		//from CreateNetworkInstanceTest
//		sdncAdapterNetworkTopologySimulator_CreateNetworkV2();
//		MockNetworkAdapterResponse_CreateSuccessV2();
//		MockAAIResponse_queryName_CreateNetwork_404V2();         // 'network-name' not in AAI , Ok to Create.
//		MockAAIResponse_cloudRegion25_Success();
//		MockAAIResponse_queryId_CreateNetwork_SuccessV2();
//		MockAAIResponse_queryVpnBinding_CreateNetwork_SuccessV2();
//		MockAAIResponse_queryVpnBinding2_CreateNetwork_SuccessV2();
//		MockAAIResponse_queryNetworkPolicy_CreateNetwork_SuccessV2();
//		MockAAIResponse_queryNetworkTableRef_CreateNetwork_SuccessV2();
//		MockAAIResponse_updateContrail_CreateNetwork_SuccessV2();
//		MockDBUpdate_Success();
//		MocksAAIResponse_queryNetworkInstance_CreateNetwork_Success();
//
//		//network AAI Mocks
//		MockGetNetworkById("680b7453-0ec4-4d96-b355-280d981d418f", "VCPE/getNetwork.xml");
//		MockGetNetworkById("444a701a-6419-45b2-aa52-b45a1b719cf6", "VCPE/getNetwork.xml");
//		MockGetNetworkById("cf82a73f-de7f-4f84-8dfc-16a487c63a36", "VCPE/getNetwork.xml");
//		MockPutNetwork("680b7453-0ec4-4d96-b355-280d981d418f");
//		MockPutNetwork("cf82a73f-de7f-4f84-8dfc-16a487c63a36");
//
//		MockPutGenericVnf("9c61db87-345c-49ae-ac80-472211df5deb");
//		
//		mockSNIRO();
//
//		String businessKey = UUID.randomUUID().toString();
////		String createVfModuleRequest =
////			FileUtil.readResourceFile("__files/VCPE/CreateVfModule_VID_request.json");
//
//		Map<String, Object> variables = setupVariablesObjectMap();
//
//		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVcpeResCustService",
//			"v1", businessKey, getRequest(), variables);
//
//		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
//
//		String responseBody = response.getResponse();
//		System.out.println("Workflow (Synch) Response:\n" + responseBody);
//
//		injectSDNCCallbacks(callbacks, "assign, query");
//		injectSDNCCallbacks(callbacks, "activate");
//
//		// TODO add appropriate assertions
//
//		waitForProcessEnd(businessKey, 10000);
//		checkVariable(businessKey, "CreateVcpeResCustServiceSuccessIndicator", true);
//
//		logEnd();
//	}
	
	
	/**
	 * TEST Decompose + Homing - Unit test for CreateVcpeResCustService.bpmn
	 *  - String input & String response
	 */

	@Test
//  TODO: run this test when Homing BB is merged
	@Ignore
	@Deployment(resources = {"process/CreateVcpeResCustService.bpmn",
			                 "subprocess/DoCreateServiceInstance.bpmn",
			                 "subprocess/DoCreateServiceInstanceRollback.bpmn",
			                 "subprocess/DoCreateNetworkInstance.bpmn",
			                 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
			                 "subprocess/BuildingBlock/DecomposeService.bpmn",
			                 "subprocess/BuildingBlock/Homing.bpmn",
			                 "subprocess/GenericGetService.bpmn",
			                 "subprocess/GenericPutService.bpmn",
			                 "subprocess/SDNCAdapterV1.bpmn",
			                 "subprocess/DoCreateVnf.bpmn",
			                 "subprocess/GenericGetVnf.bpmn",
			                 "subprocess/GenericPutVnf.bpmn",
			                 "subprocess/FalloutHandler.bpmn",
			                 "subprocess/GenericDeleteService.bpmn",
			                 "subprocess/DoCreateAllottedResourceBRG.bpmn",
			                 "subprocess/DoCreateAllottedResourceBRGRollback.bpmn",
			                 "subprocess/DoCreateAllottedResourceTXC.bpmn",
			                 "subprocess/DoCreateAllottedResourceTXCRollback.bpmn",
	                         "subprocess/CompleteMsoProcess.bpmn"})

	public void invokeDecompositionHomingCreateServiceInstanceNetwork() throws Exception {

		logStart();

		// setup simulators
		MockGetCustomer("MCBH-1610", "VCPE/getCustomer.xml");

		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "2", "VCPE/getCatalogVcpe.json");
		
//		MockPutServiceInstance_VCPE();
		
//		MockGetNetworkByIdWithDepth("680b7453-0ec4-4d96-b355-280d981d418f", "VCPE/CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "1");
//		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "VCPE/CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "1");

		MockPutGenericVnf("39ae1b77-4edd-4e94-846a-d087a35a2260");
		
		// stuff to satisfy TXC & BRG subflows
		MockNodeQueryServiceInstanceById("MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getServiceInstance.xml");
		MockNodeQueryServiceInstanceById("MIS%252F1604%252F0027%252FSW_INTERNET", "GenericFlows/getParentSIUrlById.xml");
		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0027%252FSW_INTERNET", "GenericFlows/getParentServiceInstance.xml");
		MockPutAllottedResource("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0027%252FSW_INTERNET", "arId-1");
		MockPatchAllottedResource("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0027%252FSW_INTERNET", "arId-1");
		
		mockSDNCAdapter(200);

		mockUpdateRequestDB(200, "Database/DBUpdateResponse.xml");

//		TODO: uncomment when Homing BB is merged
//		mockSNIRO();

		//Below works for Homing/Sniro
		
		Map<String, Object> variables = setupVariablesObjectMap();

		String businessKey = UUID.randomUUID().toString();
		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVcpeResCustService",
			"v1", businessKey, request, variables);
				
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
		
		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);

		injectWorkflowMessages(callbacks, "sniro");
		injectSDNCCallbacks(callbacks, "assign, create, activate, queryTXC");
		injectSDNCCallbacks(callbacks, "assign, create, activate, queryBRG");

		waitForProcessEnd(businessKey, 10000);
		checkVariable(businessKey, "CreateVcpeResCustServiceSuccessIndicator", true);

		logEnd();
	}
	
	
	/**
	 * TEST Decompose + Homing - Unit test for CreateVcpeResCustService.bpmn
	 *  - String input & String response
	 */

//	@Test
//	@Deployment(resources = {"process/CreateVcpeResCustService.bpmn",
//			                 "subprocess/DoCreateServiceInstance.bpmn",
//			                 "subprocess/DoCreateServiceInstanceRollback.bpmn",
//			                 "subprocess/DoCreateNetworkInstance.bpmn",
//			                 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
//			                 "subprocess/BuildingBlock/DecomposeService.bpmn",
//			                 "subprocess/BuildingBlock/Homing.bpmn",
//			                 "subprocess/DoCreateAllottedResourceTXC.bpmn",
//			                 "subprocess/DoCreateAllottedResourceTXCRollback.bpmn",
//			                 "subprocess/DoCreateAllottedResourceBRG.bpmn",
//			                 "subprocess/DoCreateAllottedResourceBRGRollback.bpmn",
//			                 "subprocess/GenericGetService.bpmn",
//			                 "subprocess/GenericPutService.bpmn",
//			                 "subprocess/SDNCAdapterV1.bpmn",
//			                 "subprocess/DoCreateVnf.bpmn",
//			                 "subprocess/GenericGetVnf.bpmn",
//			                 "subprocess/GenericPutVnf.bpmn",
//			                 "subprocess/FalloutHandler.bpmn",
//			                 "subprocess/GenericDeleteService.bpmn",
//			                 "subprocess/ReceiveWorkflowMessage.bpmn",
//	                         "subprocess/CompleteMsoProcess.bpmn"})
//
//	public void invokeDecompositionHomingCreateServiceInstanceARs() throws Exception {
//
//		logStart();
//
//		// setup simulators
//		MockGetCustomer("MCBH-1610", "VCPE/getCustomer.xml");
//
//		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "VCPE/getCatalogServiceResourcesDataServiceAllotted.json");
//		
//		MockPutServiceInstance_VCPE();
//		//network AAI Mocks
//		MockGetNetworkById("cf82a73f-de7f-4f84-8dfc-16a487c63a36", "VCPE/getNetwork.xml");
//		MockPutNetwork("cf82a73f-de7f-4f84-8dfc-16a487c63a36");
//		
//		MockNodeQueryServiceInstanceById("c763d462-dfe4-4577-9706-fa3a9db640be", "VCPE/getSIUrlById.xml");
//		
//		mockSDNCAdapter();
//
//		MockNodeQueryServiceInstanceById("MIS%252F1604%252F0026%252FSW_INTERNET", "GenericFlows/getSIUrlById.xml");
//		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET");
//		MockPutAllottedResource("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET", "arId-1");
//		MockPutAllottedResource("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET_in-use", "arId-1");
//		MockPatchAllottedResource("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET_in-use", "arId-1");
//		
//		
//		MockDBUpdateVfModule();
//		
//		mockSNIRO();
//
//		String businessKey = UUID.randomUUID().toString();
//
//		//Below works for Homing/Sniro
//		
//		Map<String, Object> variables = setupVariablesObjectMap();
//		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVcpeResCustService", "v1", businessKey, getRequest(), variables);
//		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
//		String responseBody = response.getResponse();
//		System.out.println("Workflow (Synch) Response:\n" + responseBody);
//		
//		//Below is from CreateVcpeResCustService
////		Map<String, String> variables = setupVariables();
////		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "CreateVcpeResCustService", variables);
////		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());
////		String workflowResp = BPMNUtil.getVariable(processEngineRule, "CreateVcpeResCustService", "WorkflowResponse");
//
//		injectWorkflowMessages(callbacks, "sniro");
//		// TODO add appropriate assertions
//		injectSDNCCallbacks(callbacks, "assign, query, create, activate, queryTXC, assign, create, activate, queryBRG");
//		waitForProcessEnd(businessKey, 10000);
//
////		checkVariable(businessKey, "CreateVcpeResCustServiceSuccessIndicator", true);
//
//		logEnd();
//	}
//
//	
//	/**
//	 * TEST Decompose + Homing - Unit test for CreateVcpeResCustService.bpmn
//	 *  - String input & String response
//	 */
//
//	@Test
//	//@Ignore
//	@Deployment(resources = {"process/CreateVcpeResCustService.bpmn",
//			                 "subprocess/DoCreateServiceInstance.bpmn",
//			                 "subprocess/DoCreateServiceInstanceRollback.bpmn",
//			                 "subprocess/DoCreateNetworkInstance.bpmn",
//			                 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
//			                 "subprocess/BuildingBlock/DecomposeService.bpmn",
//			                 "subprocess/BuildingBlock/Homing.bpmn",
//			                 "subprocess/DoCreateVnfAndModules.bpmn",
//			                 "subprocess/DoCreateVnfAndModulesRollback.bpmn",
//			                 "subprocess/DoCreateAllottedResourceTXC.bpmn",
//			                 "subprocess/DoCreateAllottedResourceTXCRollback.bpmn",
//			                 "subprocess/DoCreateAllottedResourceBRG.bpmn",
//			                 "subprocess/DoCreateAllottedResourceBRGRollback.bpmn",
//			                 "subprocess/GenericGetService.bpmn",
//			                 "subprocess/GenericPutService.bpmn",
//			                 "subprocess/SDNCAdapterV1.bpmn",
//			                 "subprocess/DoCreateVnf.bpmn",
//			                 "subprocess/GenericGetVnf.bpmn",
//			                 "subprocess/GenericPutVnf.bpmn",
//			                 "subprocess/FalloutHandler.bpmn",
//			                 "subprocess/GenericDeleteService.bpmn",
//			                 "subprocess/ReceiveWorkflowMessage.bpmn",
//	                         "subprocess/CompleteMsoProcess.bpmn"})
//
//	public void invokeDecompositionHomingCreateServiceVnf() throws Exception {
//
//		logStart();
//
//		// setup simulators
//		MockGetCustomer("MCBH-1610", "VCPE/getCustomer.xml");
//
//		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "VCPE/getCatalogServiceResourcesServiceVnf.json");
//		
//		MockPutServiceInstance_VCPE();
//		//network AAI Mocks
//		MockGetNetworkById("cf82a73f-de7f-4f84-8dfc-16a487c63a36", "VCPE/getNetwork.xml");
//		MockPutNetwork("cf82a73f-de7f-4f84-8dfc-16a487c63a36");
//		
//		MockNodeQueryServiceInstanceById("c763d462-dfe4-4577-9706-fa3a9db640be", "VCPE/getSIUrlById.xml");
//		
//		
//		MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlByIdVcpe.xml");
//		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET");
//		MockGetGenericVnfById_404("testVnfId");
//		MockPutGenericVnf(".*");
//		MockAAIVfModule();
//		MockPatchGenericVnf("skask");
//		MockPatchVfModuleId("skask", ".*");
//		MockSDNCAdapterVfModule();		
//		MockVNFAdapterRestVfModule();
//		MockDBUpdateVfModule();	
//		
//		
//		mockSDNCAdapter();
//		//mockSDNCAdapterRest();
//		
//		//MockSDNCAdapterServiceInstanceModule();
//		
//		//mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologyRsrcAssignResponse.xml", "SvcAction>assign");
//		
//		MockDBUpdateVfModule();
//		
//		mockSNIRO();
//
//		String businessKey = UUID.randomUUID().toString();
//
//		//Below works for Homing/Sniro
//		
//		Map<String, Object> variables = setupVariablesObjectMap();
//		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVcpeResCustService", "v1", businessKey, getRequest(), variables);
//		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
//		String responseBody = response.getResponse();
//		System.out.println("Workflow (Synch) Response:\n" + responseBody);
//		
//		//Below is from CreateVcpeResCustService
////		Map<String, String> variables = setupVariables();
////		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "CreateVcpeResCustService", variables);
////		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());
////		String workflowResp = BPMNUtil.getVariable(processEngineRule, "CreateVcpeResCustService", "WorkflowResponse");
//
//		injectWorkflowMessages(callbacks, "sniro");
//		// TODO add appropriate assertions
//		injectSDNCCallbacks(callbacks, "assign, query");
//		waitForProcessEnd(businessKey, 10000);
//
////		checkVariable(businessKey, "CreateVcpeResCustServiceSuccessIndicator", true);
//
//		logEnd();
//	}
//	
//	
//	/**
//	 * TEST Decompose + Homing - Unit test for CreateVcpeResCustService.bpmn
//	 *  - String input & String response
//	 */
//
//	@Test
//	//@Ignore
//	@Deployment(resources = {"process/CreateVcpeResCustService.bpmn",
//			                 "subprocess/DoCreateServiceInstance.bpmn",
//			                 "subprocess/DoCreateServiceInstanceRollback.bpmn",
//			                 "subprocess/DoCreateNetworkInstance.bpmn",
//			                 "subprocess/DoCreateNetworkInstanceRollback.bpmn",
//			                 "subprocess/BuildingBlock/DecomposeService.bpmn",
//			                 "subprocess/BuildingBlock/Homing.bpmn",
//			                 "subprocess/DoCreateVnfAndModules.bpmn",
//			                 "subprocess/DoCreateVnfAndModulesRollback.bpmn",
//			                 "subprocess/DoCreateAllottedResourceTXC.bpmn",
//			                 "subprocess/DoCreateAllottedResourceTXCRollback.bpmn",
//			                 "subprocess/DoCreateAllottedResourceBRG.bpmn",
//			                 "subprocess/DoCreateAllottedResourceBRGRollback.bpmn",
//			                 "subprocess/GenericGetService.bpmn",
//			                 "subprocess/GenericPutService.bpmn",
//			                 "subprocess/SDNCAdapterV1.bpmn",
//			                 "subprocess/DoCreateVnf.bpmn",
//			                 "subprocess/GenericGetVnf.bpmn",
//			                 "subprocess/GenericPutVnf.bpmn",
//			                 "subprocess/FalloutHandler.bpmn",
//			                 "subprocess/GenericDeleteService.bpmn",
//			                 "subprocess/ReceiveWorkflowMessage.bpmn",
//	                         "subprocess/CompleteMsoProcess.bpmn"})
//
//	public void invokeCreateAll() throws Exception {
//
//		logStart();
//
//		// setup simulators
//		MockGetCustomer("MCBH-1610", "VCPE/getCustomer.xml");
//
//		MockGetServiceResourcesCatalogData("uuid-miu-svc-011-abcdef", "VCPE/getCatalogServiceResourcesData.json");
//		
//		MockPutServiceInstance_VCPE();
//		//network AAI Mocks
//		MockGetNetworkById("cf82a73f-de7f-4f84-8dfc-16a487c63a36", "VCPE/getNetwork.xml");
//		MockPutNetwork("cf82a73f-de7f-4f84-8dfc-16a487c63a36");
//		
//		MockNodeQueryServiceInstanceById("c763d462-dfe4-4577-9706-fa3a9db640be", "VCPE/getSIUrlById.xml");
//		
//		MockGetNetworkByIdWithDepth("680b7453-0ec4-4d96-b355-280d981d418f", "VCPE/CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "1");
//		MockGetNetworkByIdWithDepth("49c86598-f766-46f8-84f8-8d1c1b10f9b4", "VCPE/CreateNetworkV2/createNetwork_queryNetworkId_AAIResponse_Success.xml", "1");
//		MockNetworkAdapterPost("CreateNetworkV2/createNetworkResponse_Success.xml", "VCPE/createNetworkRequest");
//		MockGetNetworkVpnBindingWithDepth("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "85f015d0-2e32-4c30-96d2-87a1a27f8017", "all");
//		MockGetNetworkVpnBindingWithDepth("CreateNetworkV2/createNetwork_queryVpnBinding_AAIResponse_Success.xml", "c980a6ef-3b88-49f0-9751-dbad8608d0a6", "all");
//		MockGetNetworkPolicyWithDepth("CreateNetworkV2/createNetwork_queryNetworkPolicy_AAIResponse_Success.xml", "cee6d136-e378-4678-a024-2cd15f0ee0cg", "all");
//		MockGetNetworkTableReferenceWithDepth("CreateNetworkV2/createNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN1", "all");
//		MockGetNetworkTableReferenceWithDepth("CreateNetworkV2/createNetwork_queryNetworkTableRef1_AAIResponse_Success.xml", "refFQDN2", "all");
//		MockPutNetworkIdWithDepth("CreateNetworkV2/createNetwork_updateContrail_AAIResponse_Success.xml", "680b7453-0ec4-4d96-b355-280d981d418f", "1");
//		MockPutNetworkIdWithDepth("CreateNetworkV2/createNetwork_updateContrail_AAIResponse_Success.xml", "49c86598-f766-46f8-84f8-8d1c1b10f9b4", "1");
//		
//		
//		MockNodeQueryServiceInstanceById("MIS%2F1604%2F0026%2FSW_INTERNET", "GenericFlows/getSIUrlByIdVcpe.xml");
//		MockGetServiceInstance("SDN-ETHERNET-INTERNET", "123456789", "MIS%252F1604%252F0026%252FSW_INTERNET");
//		MockGetGenericVnfById_404("testVnfId");
//		MockPutGenericVnf(".*");
//		MockAAIVfModule();
//		MockPatchGenericVnf("skask");
//		MockPatchVfModuleId("skask", ".*");
//		MockSDNCAdapterVfModule();		
//		MockVNFAdapterRestVfModule();
//		MockDBUpdateVfModule();	
//		
//		
//		mockSDNCAdapter();
//		//mockSDNCAdapterRest();
//		
//		//MockSDNCAdapterServiceInstanceModule();
//		
//		//mockSDNCAdapterTopology("CreateNetworkV2mock/sdncCreateNetworkTopologyRsrcAssignResponse.xml", "SvcAction>assign");
//		
//		MockDBUpdateVfModule();
//		
//		mockSNIRO();
//
//		String businessKey = UUID.randomUUID().toString();
//
//		//Below works for Homing/Sniro
//		
//		Map<String, Object> variables = setupVariablesObjectMap();
//		TestAsyncResponse asyncResponse = invokeAsyncProcess("CreateVcpeResCustService", "v1", businessKey, getRequest(), variables);
//		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
//		String responseBody = response.getResponse();
//		System.out.println("Workflow (Synch) Response:\n" + responseBody);
//		
//		//Below is from CreateVcpeResCustService
////		Map<String, String> variables = setupVariables();
////		WorkflowResponse workflowResponse = executeWorkFlow(processEngineRule, "CreateVcpeResCustService", variables);
////		waitForWorkflowToFinish(processEngineRule, workflowResponse.getProcessInstanceID());
////		String workflowResp = BPMNUtil.getVariable(processEngineRule, "CreateVcpeResCustService", "WorkflowResponse");
//
//		injectWorkflowMessages(callbacks, "sniro");
//		// TODO add appropriate assertions
//		injectSDNCCallbacks(callbacks, "assign, query");
//		waitForProcessEnd(businessKey, 10000);
//
////		checkVariable(businessKey, "CreateVcpeResCustServiceSuccessIndicator", true);
//
//		logEnd();
//	}
	
	// *****************
	// Utility Section
	// *****************

	// Success Scenario
	private Map<String, Object> setupVariablesObjectMap() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("requestId", "testRequestId");
		variables.put("request-id", "testRequestId");
		variables.put("CREVAS_testServiceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4"); // assigned for testing
		variables.put("serviceInstanceId", "c763d462-dfe4-4577-9706-fa3a9db640be");// unit test
		variables.put("sourceNetworkId", "c763d462-dfe4-4577-9706-fa3a9db640be");// unit test
		variables.put("networkId", "c763d462-dfe4-4577-9706-fa3a9db640be");// unit test
		variables.put("sourceNetworkRole", "whoknows");// unit test
		variables.put("allottedResourceId", "arId-1");
		variables.put("junitSleepMs", "5");
		return variables;

	}

//	private Map<String, String> setupVariables() {
//		Map<String, String> variables = new HashMap<String, String>();
//		variables.put("bpmnRequest", getRequest());
//		variables.put("mso-request-id", "testRequestId");
//		variables.put("CREVAS_testServiceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4"); // assigned for testing
//		variables.put("serviceInstanceId", "c763d462-dfe4-4577-9706-fa3a9db640be");// unit test
//		variables.put("sourceNetworkId", "c763d462-dfe4-4577-9706-fa3a9db640be");// unit test
//		variables.put("sourceNetworkRole", "whoknows");// unit test
//		variables.put("allottedResourceId", "arId-1");
//		variables.put("junitSleepMs", "5");
//		return variables;
//
//	}
	
	// start of mocks used locally and by other VF Module unit tests
	public static void MockSDNCAdapterVfModule() {
		// simplified the implementation to return "success" for all requests
		stubFor(post(urlEqualTo("/SDNCAdapter"))
//			.withRequestBody(containing("SvcInstanceId><"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/xml")
				.withBodyFile("VfModularity/StandardSDNCSynchResponse.xml")));
//		stubFor(post(urlEqualTo("/SDNCAdapter"))
//				.withRequestBody(containing("vnf-type>STMTN"))
//				.willReturn(aResponse()
//					.withStatus(200)
//					.withHeader("Content-Type", "text/xml")
//					.withBodyFile("VfModularity/StandardSDNCSynchResponse.xml")));
//		stubFor(post(urlEqualTo("/SDNCAdapter"))
//				.withRequestBody(containing("SvcAction>query"))
//				.willReturn(aResponse()
//					.withStatus(200)
//					.withHeader("Content-Type", "text/xml")
//					.withBodyFile("VfModularity/StandardSDNCSynchResponse.xml")));
	}
	

	
	
}
