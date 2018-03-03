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

package org.openecomp.mso.bpmn.infrastructure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unit test for DoDeleteVfModule.bpmn.
 */
public class DeleteVfModuleInfraTest extends WorkflowTest {
	private final CallbackSet callbacks = new CallbackSet();
	
	private static final String EOL = "\n";

	private final String vnfAdapterDeleteCallback = 
			"<deleteVfModuleResponse>" + EOL +
			"    <vnfId>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnfId>" + EOL +
			"    <vfModuleId>973ed047-d251-4fb9-bf1a-65b8949e0a73</vfModuleId>" + EOL +
			"    <vfModuleDeleted>true</vfModuleDeleted>" + EOL +
			"    <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			" 	<vfModuleOutputs>" + EOL +  
			" 	 <entry>" + EOL +
			"	 <key>policyKey1_contrail_network_policy_fqdn</key>" + EOL +
			" <value>MSOTest:DefaultPolicyFQDN1</value>" + EOL +
			"</entry>" + EOL +
			"<entry>" + EOL +
			"<key>policyKey2_contrail_network_policy_fqdn</key>" + EOL +
			"<value>MSOTest:DefaultPolicyFQDN2</value>" + EOL +
			"</entry>" + EOL +
			" 	 <entry>" + EOL +
			"	 <key>oam_management_v4_address</key>" + EOL +
			" <value>1234</value>" + EOL +
			"</entry>" + EOL +
			" 	 <entry>" + EOL +
			"	 <key>oam_management_v6_address</key>" + EOL +
			" <value>1234</value>" + EOL +
			"</entry>" + EOL +
			"</vfModuleOutputs>" + EOL +
			"</deleteVfModuleResponse>" + EOL;
				
	private final String vnfAdapterDeleteCallbackFail = 
			"<vfModuleException>" + EOL +
			"    <message>Error processing request to VNF-Async. Not Found.</message>" + EOL +
			"    <category>INTERNAL</category>" + EOL +
			"    <rolledBack>false</rolledBack>" + EOL +
			"    <messageId>{{MESSAGE-ID}}</messageId>" + EOL +
			"</vfModuleException>" + EOL;
					
	private final String sdncAdapterDeleteCallback =
		"<output xmlns=\"org:onap:sdnctl:l3api\">" + EOL +
		"  <svc-request-id>{{REQUEST-ID}}</svc-request-id>" + EOL +
		"  <ack-final-indicator>Y</ack-final-indicator>" + EOL +
		"</output>" + EOL;
	
	public DeleteVfModuleInfraTest() throws IOException {
		callbacks.put("sdncChangeDelete", sdncAdapterDeleteCallback);
		callbacks.put("sdncDelete", sdncAdapterDeleteCallback);
		callbacks.put("vnfDelete", vnfAdapterDeleteCallback);
		callbacks.put("vnfDeleteFail", vnfAdapterDeleteCallbackFail);
	}

	@Test
	@Deployment(resources = {
			"process/Infrastructure/DeleteVfModuleInfra.bpmn",
			"subprocess/DoDeleteVfModule.bpmn",
			"subprocess/PrepareUpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIVfModule.bpmn",
			"subprocess/UpdateAAIGenericVnf.bpmn",
			"subprocess/DeleteAAIVfModule.bpmn",
			"subprocess/SDNCAdapterV1.bpmn",
			"subprocess/VnfAdapterRestV1.bpmn",
			"subprocess/CompleteMsoProcess.bpmn",
			"subprocess/FalloutHandler.bpmn"
		})
	@Ignore
	public void  TestDeleteVfModuleSuccess() throws Exception {
		// delete the Base Module
		// vnf-id=a27ce5a9-29c4-4c22-a017-6615ac73c721, vf-module-id=973ed047-d251-4fb9-bf1a-65b8949e0a73
		String request =
			"<vnf-request xmlns=\"http://openecomp.org/mso/infra/vnf-request/v1\">" + EOL +
			"  <request-info>" + EOL +
			"    <request-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</request-id>" + EOL +
			"    <action>DELETE_VF_MODULE</action>" + EOL +
			"    <source>PORTAL</source>" + EOL +
			"  </request-info>" + EOL +
			"  <vnf-inputs>" + EOL +
			"    <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"    <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"    <vnf-type>asc_heat-int</vnf-type>" + EOL +
			"    <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"    <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"    <service-id>00000000-0000-0000-0000-000000000000</service-id>" + EOL +
			"    <service-type>SDN-ETHERNET-INTERNET</service-type>" + EOL +
			"    <tenant-id>fba1bd1e195a404cacb9ce17a9b2b421</tenant-id>" + EOL +
			"    <orchestration-status>pending-delete</orchestration-status>" + EOL +
			"    <aic-cloud-region>RDM2WAGPLCP</aic-cloud-region>" + EOL +
			"  </vnf-inputs>" + EOL +
			"  <vnf-params xmlns:tns=\"http://openecomp.org/mso/infra/vnf-request/v1\"/>" + EOL +
			"</vnf-request>" + EOL;
		logStart();
		WireMock.reset();
		
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .withRequestBody(containing("SvcAction>changedelete"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
		stubFor(post(urlEqualTo("/SDNCAdapter"))
				  .withRequestBody(containing("SvcAction>delete"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("DeleteGenericVNFV1/sdncAdapterResponse.xml")));
		
		mockVNFDelete(".*", "/.*", 202);
//		MockAAIGenericVnfSearch();
//		MockAAIVfModulePUT(false);
//		MockAAIDeleteGenericVnf();
//		MockAAIDeleteVfModule();
		mockUpdateRequestDB(200, "VfModularity/DBUpdateResponse.xml");
		
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a73/[?]resource-version=0000073"))
				.willReturn(aResponse()
						.withStatus(200)));
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c720/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a75/[?]resource-version=0000075"))
				.willReturn(aResponse()
						.withStatus(200)));
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a78/[?]resource-version=0000078"))
				.willReturn(aResponse()
						.withStatus(200)));
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c719/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a77/[?]resource-version=0000077"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
		stubFor(get(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy\\?network-policy-fqdn=.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "text/xml")
				  .withBodyFile("VfModularity/QueryNetworkPolicy_AAIResponse_Success.xml")));

		stubFor(delete(urlMatching("/aai/v[0-9]+/network/network-policies/network-policy/.*"))
				.willReturn(aResponse()
						.withStatus(200)));
		
		
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/[?]resource-version=0000021"))
				.willReturn(aResponse()
						.withStatus(200)));
		stubFor(delete(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718/[?]resource-version=0000018"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
		
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*/vf-modules/vf-module/.*"))
				.withRequestBody(containing("MMSC"))
				.willReturn(aResponse()
						.withStatus(200)));
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/.*/vf-modules/vf-module/.*"))
				.withRequestBody(containing("PCRF"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
		stubFor(put(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721"))				
				.willReturn(aResponse()
					.withStatus(200)));
		
		String body;
		
		// The following stubs are for CreateAAIVfModule and UpdateAAIVfModule
	
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC23&depth=1"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
	
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC22&depth=1"))
				.willReturn(aResponse()
						.withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBody("Generic VNF Not Found")));
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/768073c7-f41f-4822-9323-b75962763d74[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBody("Generic VNF Not Found")));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>1508691</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>1508692</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC21&depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>2f6aee38-1e2a-11e6-82d1-ffc7d9ee8aa4</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC20</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>1508691</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC20-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>1508692</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a74</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC20-MMSC::module-1-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a74</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>false</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>1508692</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC20&depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/2f6aee38-1e2a-11e6-82d1-ffc7d9ee8aa4[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		// The following stubs are for DeleteAAIVfModule
	
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c723[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
	
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c722[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBody("Generic VNF Not Found")));
	
		body =
				"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
				"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
				"  <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
				"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
				"  <service-id>SDN-MOBILITY</service-id>" + EOL +
				"  <equipment-role>vMMSC</equipment-role>" + EOL +
				"  <orchestration-status>pending-create</orchestration-status>" + EOL +
				"  <in-maint>false</in-maint>" + EOL +
				"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
				"  <resource-version>0000021</resource-version>" + EOL +
				"  <vf-modules>" + EOL +
				"    <vf-module>" + EOL +
				"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
				"      <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
				"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</persona-model-id>" + EOL +
				"      <persona-model-version>1.0</persona-model-version>" + EOL +
				"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
				"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
				"      <orchestration-status>pending-create</orchestration-status>" + EOL +
				"      <resource-version>0000073</resource-version>" + EOL +
				"    </vf-module>" + EOL +
				"  </vf-modules>" + EOL +
				"  <relationship-list/>" + EOL +
				"  <l-interfaces/>" + EOL +
				"  <lag-interfaces/>" + EOL +
				"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c720</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC20</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>0000020</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a74</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC20-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a74</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000074</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a75</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC20-MMSC::module-1-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a75</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>false</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000075</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c720[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c719</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC19</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>0000019</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a76</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC19-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a76</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000076</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a77</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC19-MMSC::module-1-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a77</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>false</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000077</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c719[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c718</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC18</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>0000018</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a78</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC18-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a78</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000078</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		body =
			"<generic-vnf xmlns=\"http://org.openecomp.aai.inventory/v7\">" + EOL +
			"  <vnf-id>a27ce5a9-29c4-4c22-a017-6615ac73c721</vnf-id>" + EOL +
			"  <vnf-name>STMTN5MMSC21</vnf-name>" + EOL +
			"  <vnf-type>mmsc-capacity</vnf-type>" + EOL +
			"  <service-id>SDN-MOBILITY</service-id>" + EOL +
			"  <equipment-role>vMMSC</equipment-role>" + EOL +
			"  <orchestration-status>pending-create</orchestration-status>" + EOL +
			"  <in-maint>false</in-maint>" + EOL +
			"  <is-closed-loop-disabled>false</is-closed-loop-disabled>" + EOL +
			"  <resource-version>0000021</resource-version>" + EOL +
			"  <vf-modules>" + EOL +
			"    <vf-module>" + EOL +
			"      <vf-module-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</vf-module-id>" + EOL +
			"      <vf-module-name>STMTN5MMSC21-MMSC::module-0-0</vf-module-name>" + EOL +
			"      <persona-model-id>973ed047-d251-4fb9-bf1a-65b8949e0a73</persona-model-id>" + EOL +
			"      <persona-model-version>1.0</persona-model-version>" + EOL +
			"      <is-base-vf-module>true</is-base-vf-module>" + EOL +
			"      <heat-stack-id>FILLED-IN-BY-MSO</heat-stack-id>" + EOL +
			"      <orchestration-status>pending-create</orchestration-status>" + EOL +
			"      <resource-version>0000073</resource-version>" + EOL +
			"    </vf-module>" + EOL +
			"  </vf-modules>" + EOL +
			"  <relationship-list/>" + EOL +
			"  <l-interfaces/>" + EOL +
			"  <lag-interfaces/>" + EOL +
			"</generic-vnf>" + EOL;
		stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a73"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));

		String businessKey = UUID.randomUUID().toString();
		String deleteVfModuleRequest =
				FileUtil.readResourceFile("__files/DeleteVfModule_VID_request.json");
		//Map<String, Object> variables = new HashMap<String, Object>();	
		
		//variables.put("isDebugLogEnabled","true");
//		variables.put("mso-request-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
//		variables.put("mso-service-instance-id", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
		
		Map<String, Object> variables = setupVariablesSunnyDayVID();

		TestAsyncResponse asyncResponse = invokeAsyncProcess("DeleteVfModuleInfra",
				"v1", businessKey, deleteVfModuleRequest, variables);
		WorkflowResponse response = receiveResponse(businessKey, asyncResponse, 10000);
		String responseBody = response.getResponse();
		System.out.println("Workflow (Synch) Response:\n" + responseBody);

		// "changedelete" operation not required for deleting a Vf Module
//		injectSDNCCallbacks(callbacks, "sdncChangeDelete");
		injectVNFRestCallbacks(callbacks, "vnfDelete");
		waitForRunningProcessCount("vnfAdapterDeleteV1", 0, 120000);
		injectSDNCCallbacks(callbacks, "sdncDelete");

		waitForProcessEnd(businessKey, 10000);
		WorkflowException wfe = (WorkflowException) getVariableFromHistory(businessKey, "WorkflowException");
		checkVariable(businessKey, "DeleteVfModuleInfraSuccessIndicator", true);
		checkVariable(businessKey, "WorkflowException", null);
		if (wfe != null) {
			System.out.println("TestDeleteVfModuleInfraSuccess: ErrorCode=" + wfe.getErrorCode() +
					", ErrorMessage=" + wfe.getErrorMessage());
		}
		logEnd();
	}
	
	// Active Scenario
			private Map<String, Object> setupVariablesSunnyDayVID() {
				Map<String, Object> variables = new HashMap<>();
				//try {
				//	variables.put("bpmnRequest", FileUtil.readResourceFile("__files/CreateVfModule_VID_request.json"));
				//}
				//catch (Exception e) {
					
				//}
				//variables.put("mso-request-id", "testRequestId");
				variables.put("requestId", "testRequestId");		
				variables.put("isBaseVfModule", "true");
				variables.put("isDebugLogEnabled", "true");
				variables.put("recipeTimeout", "0");		
				variables.put("requestAction", "DELETE_VF_MODULE");
				variables.put("serviceInstanceId", "f70e927b-6087-4974-9ef8-c5e4d5847ca4");
				variables.put("vnfId", "a27ce5a9-29c4-4c22-a017-6615ac73c721");
				variables.put("vfModuleId", "973ed047-d251-4fb9-bf1a-65b8949e0a73");
				variables.put("volumeGroupId", "");			
				variables.put("serviceType", "MOG");	
				variables.put("vfModuleType", "");			
				return variables;
				
			}

	
}
