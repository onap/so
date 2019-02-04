/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.audit;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.LInterfaces;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.heat.model.Resources;
import com.woorea.openstack.heat.model.Stack;


@RunWith(MockitoJUnitRunner.Silent.class)
public class HeatStackAuditTest extends HeatStackAudit {

	@InjectMocks
	private HeatStackAudit heatStackAudit = new HeatStackAudit();

	@Mock
	private MsoHeatUtils msoHeatUtilsMock;
	
	@Mock
	private AuditVServer auditVserver;

	private static final String cloudRegion = "cloudRegion";
	private static final String tenantId = "tenantId";
	
	private Resources resources = new Resources();
	
	private ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	private ObjectMapper stackObjectMapper = new ObjectMapper().configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);

	@Before
	public void setup() throws Exception{		
		resources= objectMapper.readValue(new File("src/test/resources/GetResources.json"), Resources.class);
		
	} 
	
	@Test
	public void extract_proper_path_Test(){
		Optional<String> actualResult = extractStackPathFromHref("https://orchestration.com:8004/v1/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81");
		assertEquals("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81", actualResult.get());
	}
	
	@Test
	public void extract_proper_resources_path_Test(){
		Optional<String> actualResult = extractResourcePathFromHref("https://orchestration.com:8004/v1/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81");
		assertEquals("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81/resources", actualResult.get());
	}
	
	@Test
	public void extract_invalid_uri_Test(){
		Optional<String> actualResult = extractStackPathFromHref("orchestrn.com:8004/v18b44d60a6f94bdcb2738f9e/stacks/test_stack/f711be16-2654-4a09-b89d-0511fda20e81");
		assertEquals(false, actualResult.isPresent());
	}

	@Test
	public void createVserverSet_Test() throws Exception{
		List<Resource> novaResources = resources.getList().stream()
				.filter(p -> "OS::Nova::Server".equals(p.getType())).collect(Collectors.toList());
		
		List<Resource> resourceGroups = resources.getList().stream()
				.filter(p -> "OS::Heat::ResourceGroup".equals(p.getType())).collect(Collectors.toList());
		
		Set<Vserver> expectedVservers = new HashSet<>();
		Vserver vServer1= new Vserver();
		vServer1.setVserverId("92272b67-d23f-42ca-87fa-7b06a9ec81f3");
		LInterfaces vServer1Linterfaces = new LInterfaces();
		vServer1.setLInterfaces(vServer1Linterfaces);
		
		LInterface ssc_1_trusted_port_0 = new LInterface();
		ssc_1_trusted_port_0.setInterfaceId("d2f51f82-0ec2-4581-bd1a-d2a82073e52b");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_trusted_port_0);
		

		
		LInterface ssc_1_mgmt_port_1 = new LInterface();
		ssc_1_mgmt_port_1.setInterfaceId("07f5b14c-147a-4d14-8c94-a9e94dbc097b");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_1);
		
		LInterface ssc_1_mgmt_port_0 = new LInterface();
		ssc_1_mgmt_port_0.setInterfaceId("8d93f63e-e972-48c7-ad98-b2122da47315");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_0);
		
		LInterface ssc_1_mis_port_0 = new LInterface();
		ssc_1_mis_port_0.setLInterfaces(new LInterfaces());
		ssc_1_mis_port_0.setInterfaceId("0594a2f2-7ea4-42eb-abc2-48ea49677fca");	
		vServer1.getLInterfaces().getLInterface().add(ssc_1_mis_port_0);
		
		LInterface mis_sub_interface_1 = new LInterface();
		mis_sub_interface_1.setInterfaceId("2bbfa345-33bb-495a-94b2-fb514ee1cffc");	
		ssc_1_mis_port_0.getLInterfaces().getLInterface().add(mis_sub_interface_1);
		
		LInterface ssc_1_int_ha_port_0 = new LInterface();
		ssc_1_int_ha_port_0.setInterfaceId("00bb8407-650e-48b5-b919-33b88d6f8fe3");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_int_ha_port_0);		
		
		
		LInterface ssc_1_avpn_port_0 = new LInterface();
		ssc_1_avpn_port_0.setInterfaceId("27391d94-33af-474a-927d-d409249e8fd3");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_avpn_port_0);		
		ssc_1_avpn_port_0.setLInterfaces(new LInterfaces());		
		
		LInterface avpn_sub_interface_0 = new LInterface();
		avpn_sub_interface_0.setInterfaceId("d54dfd09-75c6-4e04-b204-909455b8f933");
		ssc_1_avpn_port_0.getLInterfaces().getLInterface().add(avpn_sub_interface_0);
		
		LInterface avpn_sub_interface_1 = new LInterface();
		avpn_sub_interface_1.setInterfaceId("f7a998c0-8939-4b07-bf4a-0862e9c325e1");
		ssc_1_avpn_port_0.getLInterfaces().getLInterface().add(avpn_sub_interface_1);
		
		LInterface avpn_sub_interface_2 = new LInterface();
		avpn_sub_interface_2.setInterfaceId("621c1fea-60b8-44ee-aede-c01b8b1aaa70");
		ssc_1_avpn_port_0.getLInterfaces().getLInterface().add(avpn_sub_interface_2);

		
		expectedVservers.add(vServer1);
		
		Resources avpnQueryResponse = objectMapper.readValue(new File("src/test/resources/AVPNResourceGroupResponse.json"), Resources.class);
		doReturn(avpnQueryResponse).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_avpn_port_0_subinterfaces-dtmxjmny7yjz/31d0647a-6043-49a4-81b6-ccab29380672/resources", cloudRegion,	tenantId, Resources.class);
		
		Resources misQueryResponse =objectMapper.readValue(new File("src/test/resources/MISResourceGroupResponse.json"), Resources.class);
		doReturn(misQueryResponse).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_mis_port_0_subinterfaces-hlzdigtimzst/447a9b41-714e-434b-b1d0-6cce8d9f0f0c/resources", cloudRegion,	tenantId, Resources.class);
		
		
		Stack misStackQuerySubInt = stackObjectMapper.readValue(new File("src/test/resources/MISSubInterface0.json"), Stack.class);
		doReturn(misStackQuerySubInt).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_mis_port_0_subinterfaces-hlzdigtimzst-0-upfi5nhurk7y/f711be16-2654-4a09-b89d-0511fda20e81", cloudRegion,tenantId, Stack.class);
		Resources misResourceQuerySubInt = objectMapper.readValue(new File("src/test/resources/MISSubInterface1Resources.json"), Resources.class);
		doReturn(misResourceQuerySubInt).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_mis_port_0_subinterfaces-hlzdigtimzst-0-upfi5nhurk7y/f711be16-2654-4a09-b89d-0511fda20e81/resources", cloudRegion,tenantId, Resources.class);
		
		Stack avpnStackQuerySubInt1 =stackObjectMapper.readValue(new File("src/test/resources/AVPNSubInterface0.json"), Stack.class);
		doReturn(avpnStackQuerySubInt1).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_avpn_port_0_subinterfaces-dtmxjmny7yjz-1-fmn5laetg5cs/0d9cd813-2ae1-46c0-9ebb-48081f6cffbb", cloudRegion,tenantId, Stack.class);
		Resources avpnResourceQuerySubInt1 = objectMapper.readValue(new File("src/test/resources/AVPNSubInterface0Resources.json"), Resources.class);
		doReturn(avpnResourceQuerySubInt1).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_avpn_port_0_subinterfaces-dtmxjmny7yjz-1-fmn5laetg5cs/0d9cd813-2ae1-46c0-9ebb-48081f6cffbb/resources", cloudRegion,tenantId, Resources.class);

	
		Stack avpnStackQuerySubInt2 =stackObjectMapper.readValue(new File("src/test/resources/AVPNSubInterface1.json"), Stack.class);
		doReturn(avpnStackQuerySubInt2).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_avpn_port_0_subinterfaces-dtmxjmny7yjz-0-yghihziaf36m/b7019dd0-2ee9-4447-bdef-ac25676b205a", cloudRegion,tenantId, Stack.class);
		Resources avpnResourceQuerySubInt2 = objectMapper.readValue(new File("src/test/resources/AVPNSubInterface1Resources.json"), Resources.class);
		doReturn(avpnResourceQuerySubInt2).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_avpn_port_0_subinterfaces-dtmxjmny7yjz-0-yghihziaf36m/b7019dd0-2ee9-4447-bdef-ac25676b205a/resources", cloudRegion,tenantId, Resources.class);

		Stack avpnStackQuerySubInt3 =stackObjectMapper.readValue(new File("src/test/resources/AVPNSubInterface2.json"), Stack.class);
		doReturn(avpnStackQuerySubInt3).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_avpn_port_0_subinterfaces-dtmxjmny7yjz-2-y3ndsavmsymv/bd0fc728-cbde-4301-a581-db56f494675c", cloudRegion,tenantId, Stack.class);
		Resources avpnResourceQuerySubInt3 = objectMapper.readValue(new File("src/test/resources/AVPNSubInterface2Resources.json"), Resources.class);
		doReturn(avpnResourceQuerySubInt3).when(msoHeatUtilsMock).executeHeatClientRequest("/ea2d13cc98b44d60a6f94bdcb2738f9e/stacks/tsbc0005vm002ssc001-ssc_1_subint_avpn_port_0_subinterfaces-dtmxjmny7yjz-2-y3ndsavmsymv/bd0fc728-cbde-4301-a581-db56f494675c/resources", cloudRegion,tenantId, Resources.class);
	
		Set<Vserver> vServersToAudit = heatStackAudit.createVserverSet(resources, novaResources);
		Set<Vserver> vserversWithSubInterfaces = heatStackAudit.processSubInterfaces(cloudRegion,tenantId,resourceGroups, vServersToAudit);
		
		String actualValue = objectMapper.writeValueAsString(vserversWithSubInterfaces);
		String expectedValue = objectMapper.writeValueAsString(expectedVservers);
		
		JSONAssert.assertEquals(expectedValue, actualValue, false);
	}
	

	@Test
	public void findInterfaceInformation_Test(){
		List<Resource> novaResources = resources.getList().stream()
				.filter(p -> "OS::Nova::Server".equals(p.getType())).collect(Collectors.toList());
		Set<Vserver> expectedVservers = new HashSet<>();
		Vserver vServer1= new Vserver();
		vServer1.setVserverId("92272b67-d23f-42ca-87fa-7b06a9ec81f3");
		LInterfaces vServer1Linterfaces = new LInterfaces();
		vServer1.setLInterfaces(vServer1Linterfaces);
		
		LInterface ssc_1_trusted_port_0 = new LInterface();
		ssc_1_trusted_port_0.setInterfaceId("d2f51f82-0ec2-4581-bd1a-d2a82073e52b");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_trusted_port_0);
		
		LInterface ssc_1_avpn_port_0 = new LInterface();
		ssc_1_avpn_port_0.setInterfaceId("27391d94-33af-474a-927d-d409249e8fd3");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_avpn_port_0);
		
		LInterface ssc_1_mgmt_port_1 = new LInterface();
		ssc_1_mgmt_port_1.setInterfaceId("07f5b14c-147a-4d14-8c94-a9e94dbc097b");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_1);
		
		LInterface ssc_1_mgmt_port_0 = new LInterface();
		ssc_1_mgmt_port_0.setInterfaceId("8d93f63e-e972-48c7-ad98-b2122da47315");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_0);
		
		LInterface ssc_1_mis_port_0 = new LInterface();
		ssc_1_mis_port_0.setInterfaceId("0594a2f2-7ea4-42eb-abc2-48ea49677fca");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_mis_port_0);
		
		LInterface ssc_1_int_ha_port_0 = new LInterface();
		ssc_1_int_ha_port_0.setInterfaceId("00bb8407-650e-48b5-b919-33b88d6f8fe3");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_int_ha_port_0);		
		
		expectedVservers.add(vServer1);
	
		Set<Vserver> actualVservers = heatStackAudit.createVserverSet(resources, novaResources);
		
		assertThat(actualVservers, sameBeanAs(expectedVservers));
	}


}
