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

package org.onap.so.bpmn.common;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.WireMockServer;

public class MockAAIGenericVnfSearch {
	
	private static final String EOL = "\n";

	public MockAAIGenericVnfSearch(WireMockServer wireMockServer){
		String body;
		
		// The following stubs are for CreateAAIVfModule and UpdateAAIVfModule
	
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC23&depth=1"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
	
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC22&depth=1"))
				.willReturn(aResponse()
						.withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBody("Generic VNF Not Found")));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/768073c7-f41f-4822-9323-b75962763d74[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBody("Generic VNF Not Found")));
	
		body =
			"<generic-vnf xmlns=\"http://com.aai.inventory/v7\">" + EOL +
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
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC21&depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721[?]depth=1"))
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
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/[?]vnf-name=STMTN5MMSC20&depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/2f6aee38-1e2a-11e6-82d1-ffc7d9ee8aa4[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
		// The following stubs are for DeleteAAIVfModule
	
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c723[?]depth=1"))
				.willReturn(aResponse()
						.withStatus(500)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("aaiFault.xml")));
	
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c722[?]depth=1"))
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
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721[?]depth=1"))
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
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c720[?]depth=1"))
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
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c719[?]depth=1"))
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
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c718[?]depth=1"))
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
		wireMockServer.stubFor(get(urlMatching("/aai/v[0-9]+/network/generic-vnfs/generic-vnf/a27ce5a9-29c4-4c22-a017-6615ac73c721/vf-modules/vf-module/973ed047-d251-4fb9-bf1a-65b8949e0a73"))
				.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody(body)));
	
	}
}
