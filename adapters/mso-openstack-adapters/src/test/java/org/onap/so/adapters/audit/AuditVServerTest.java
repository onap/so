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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.domain.yang.LInterface;
import org.onap.aai.domain.yang.LInterfaces;
import org.onap.aai.domain.yang.Vserver;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.AAIResourcesClient;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woorea.openstack.heat.model.Resource;
import com.woorea.openstack.heat.model.Resources;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AuditVServerTest extends AuditVServer {

	@InjectMocks
	private AuditVServer auditNova = new AuditVServer();

	@Mock
	private AAIResourcesClient aaiResourcesMock;

	private String cloudOwner = "cloudOwner";
	private String cloudRegion = "cloudRegion";
	private String tenantId = "tenantId";

	private AAIResourceUri vserverURI = AAIUriFactory.createResourceUri(AAIObjectType.VSERVER,cloudOwner, cloudRegion,
			tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db");
	
	private AAIResourceUri vserverURI2 = AAIUriFactory.createResourceUri(AAIObjectType.VSERVER, cloudOwner, cloudRegion,
			tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4dz");

	private AAIResourceUri ssc_1_trusted_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db").queryParam("interface-id", "dec8bdc7-5718-41dc-bfbb-561ff6eeb81c");

	private AAIResourceUri ssc_1_service1_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db").queryParam("interface-id", "1c56a24b-5f03-435a-850d-31cd4252de56");

	private AAIResourceUri ssc_1_mgmt_port_1_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db").queryParam("interface-id", "12afcd28-929f-4d80-8a5a-0833bfd5e20b");

	private AAIResourceUri ssc_1_mgmt_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db").queryParam("interface-id", "80baec42-ffae-425f-ad8c-3f7b2c24bfff");

	private AAIResourceUri ssc_1_service2_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db").queryParam("interface-id", "13eddf95-4cf3-45f2-823a-2d890a6549b4");

	private AAIResourceUri ssc_1_int_ha_port_0_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db").queryParam("interface-id", "9cab2903-70f7-44fd-b681-491d6ae2adb8");

	private AAIResourceUri test_port_1_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4dz").queryParam("interface-id", "9cab2903-70f7-44fd-b681-491d6ae2adz1");


	private AAIResourceUri test_port_2_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4dz").queryParam("interface-id", "9cab2903-70f7-44fd-b681-491d6ae2adz2");

	
	
	private AAIResourceUri service2_sub_1_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.SUB_L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db","interface-name").queryParam("interface-id", "f711be16-2654-4a09-b89d-0511fda20e81");

	private AAIResourceUri service1_sub_0_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.SUB_L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db","interface-name").queryParam("interface-id", "0d9cd813-2ae1-46c0-9ebb-48081f6cffbb");

	private AAIResourceUri service1_sub_1_uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.SUB_L_INTERFACE,
			cloudOwner, cloudRegion, tenantId, "3a4c2ca5-27b3-4ecc-98c5-06804867c4db","interface-name").queryParam("interface-id", "b7019dd0-2ee9-4447-bdef-ac25676b205a");

	

	private Set<Vserver> vserversToAudit = new HashSet<>();
	
	
	LInterface test_port_1 = new LInterface();
	LInterface test_port_2 = new LInterface();
	LInterface ssc_1_int_ha_port_0 = new LInterface();
	LInterface service2_sub_interface_1 = new LInterface();
	LInterface ssc_1_service2_port_0 = new LInterface();
	LInterface ssc_1_mgmt_port_0 = new LInterface();
	LInterface ssc_1_mgmt_port_1 = new LInterface();
	LInterface service1_sub_interface_2 = new LInterface();
	LInterface service1_sub_interface_1 = new LInterface();
	LInterface ssc_1_service1_port_0 = new LInterface();
	LInterface ssc_1_trusted_port_0 = new LInterface();
	
	LInterfaces test_port_1_plural = new LInterfaces();	
	LInterfaces test_port_2_plural = new LInterfaces();
	LInterfaces ssc_1_int_ha_port_0_plural = new LInterfaces();
	LInterfaces service2_sub_interface_1_plural = new LInterfaces();
	LInterfaces ssc_1_service2_port_0_plural = new LInterfaces();
	LInterfaces ssc_1_mgmt_port_0_plural = new LInterfaces();
	LInterfaces ssc_1_mgmt_port_1_plural = new LInterfaces();
	LInterfaces service1_sub_interface_2_plural = new LInterfaces();
	LInterfaces service1_sub_interface_1_plural = new LInterfaces();
	LInterfaces ssc_1_service1_port_0_plural = new LInterfaces();
	LInterfaces ssc_1_trusted_port_0_plural = new LInterfaces();
	
	
	@Before
	public void setup() {
		auditNova.setAaiClient(aaiResourcesMock);

		Vserver vServer1= new Vserver();
		vServer1.setVserverId("3a4c2ca5-27b3-4ecc-98c5-06804867c4db");
		LInterfaces vServer1Linterfaces = new LInterfaces();
		vServer1.setLInterfaces(vServer1Linterfaces);
		
		ssc_1_trusted_port_0.setInterfaceId("dec8bdc7-5718-41dc-bfbb-561ff6eeb81c");
		ssc_1_trusted_port_0.setInterfaceName("interface-name");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_trusted_port_0);
		
		
		ssc_1_service1_port_0.setInterfaceId("1c56a24b-5f03-435a-850d-31cd4252de56");
		ssc_1_service1_port_0.setInterfaceName("interface-name");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_service1_port_0);
		ssc_1_service1_port_0.setLInterfaces(new LInterfaces());	
		
		
		service1_sub_interface_1.setInterfaceId("0d9cd813-2ae1-46c0-9ebb-48081f6cffbb");
		ssc_1_service1_port_0.getLInterfaces().getLInterface().add(service1_sub_interface_1);
		
		
		service1_sub_interface_2.setInterfaceId("b7019dd0-2ee9-4447-bdef-ac25676b205a");
		ssc_1_service1_port_0.getLInterfaces().getLInterface().add(service1_sub_interface_2);
		
		
		ssc_1_mgmt_port_1.setInterfaceId("12afcd28-929f-4d80-8a5a-0833bfd5e20b");
		ssc_1_mgmt_port_1.setInterfaceName("interface-name");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_1);

		ssc_1_mgmt_port_0.setInterfaceId("80baec42-ffae-425f-ad8c-3f7b2c24bfff");
		ssc_1_mgmt_port_0.setInterfaceName("interface-name");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_mgmt_port_0);
		
	
		ssc_1_service2_port_0.setLInterfaces(new LInterfaces());
		ssc_1_service2_port_0.setInterfaceId("13eddf95-4cf3-45f2-823a-2d890a6549b4");
		ssc_1_service2_port_0.setInterfaceName("interface-name");
		vServer1.getLInterfaces().getLInterface().add(ssc_1_service2_port_0);
		

		service2_sub_interface_1.setInterfaceId("f711be16-2654-4a09-b89d-0511fda20e81");	
		ssc_1_service2_port_0.getLInterfaces().getLInterface().add(service2_sub_interface_1);
		

		ssc_1_int_ha_port_0.setInterfaceId("9cab2903-70f7-44fd-b681-491d6ae2adb8");	
		vServer1.getLInterfaces().getLInterface().add(ssc_1_int_ha_port_0);
		
		
		Vserver vServer2= new Vserver();
		vServer2.setVserverId("3a4c2ca5-27b3-4ecc-98c5-06804867c4dz");
		LInterfaces vServer2Linterfaces = new LInterfaces();
		vServer2.setLInterfaces(vServer2Linterfaces);
	
		test_port_1.setInterfaceId("9cab2903-70f7-44fd-b681-491d6ae2adz1");
		test_port_1.setInterfaceName("interface-name");
		

		test_port_2.setInterfaceId("9cab2903-70f7-44fd-b681-491d6ae2adz2");
		test_port_2.setInterfaceName("interface-name");
		
		vServer2.getLInterfaces().getLInterface().add(test_port_1);
		vServer2.getLInterfaces().getLInterface().add(test_port_2);
		
		vserversToAudit.add(vServer1);
		vserversToAudit.add(vServer2);
		
		
		test_port_1_plural.getLInterface().add(test_port_1);
		test_port_2_plural.getLInterface().add(test_port_2);
		ssc_1_int_ha_port_0_plural.getLInterface().add(ssc_1_int_ha_port_0);
		ssc_1_service2_port_0_plural.getLInterface().add(ssc_1_service2_port_0);
		ssc_1_mgmt_port_0_plural.getLInterface().add(ssc_1_mgmt_port_0);
		ssc_1_mgmt_port_1_plural.getLInterface().add(ssc_1_mgmt_port_1);
		ssc_1_service1_port_0_plural.getLInterface().add(ssc_1_service1_port_0);
		ssc_1_trusted_port_0_plural.getLInterface().add(ssc_1_trusted_port_0);

	} 
	
	@Test
	public void audit_Vserver_Empty_HashSet() throws JsonParseException, JsonMappingException, IOException {
		boolean exists = auditNova.auditAllVserversDoExist(new HashSet<Vserver>(), tenantId, cloudOwner, cloudRegion);
		assertEquals(false, exists);
		
		boolean doNotExist = auditNova.auditAllVserversDoNotExist(new HashSet<Vserver>(), tenantId, cloudOwner, cloudRegion);
		assertEquals(true, doNotExist);
	}
	

	@Test
	public void audit_Vserver_Found_Test() throws JsonParseException, JsonMappingException, IOException {
		doReturn(true).when(aaiResourcesMock).exists(vserverURI);
		doReturn(true).when(aaiResourcesMock).exists(vserverURI2);
		doReturn(Optional.of(ssc_1_trusted_port_0_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_trusted_port_0_uri);
		doReturn(Optional.of(ssc_1_service1_port_0_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_service1_port_0_uri);
		doReturn(Optional.of(ssc_1_mgmt_port_1_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_mgmt_port_1_uri);
		doReturn(Optional.of(ssc_1_mgmt_port_0_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_mgmt_port_0_uri);
		doReturn(Optional.of(ssc_1_service2_port_0_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_service2_port_0_uri);
		doReturn(Optional.of(ssc_1_int_ha_port_0_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_int_ha_port_0_uri);
		doReturn(Optional.of(test_port_1_plural)).when(aaiResourcesMock).get(LInterfaces.class,test_port_1_uri);
		doReturn(Optional.of(test_port_2_plural)).when(aaiResourcesMock).get(LInterfaces.class,test_port_2_uri);
		
		doReturn(true).when(aaiResourcesMock).exists(service2_sub_1_uri);
		doReturn(true).when(aaiResourcesMock).exists(service1_sub_0_uri);
		doReturn(true).when(aaiResourcesMock).exists(service1_sub_1_uri);

		boolean exists = auditNova.auditAllVserversDoExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(true, exists);
		
		boolean doNotExist = auditNova.auditAllVserversDoNotExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(false, doNotExist);
	}

	@Test
	public void audit_Vserver_Found_Test_Network_Not_Found()
			throws JsonParseException, JsonMappingException, IOException {
		doReturn(true).when(aaiResourcesMock).exists(vserverURI);
		doReturn(true).when(aaiResourcesMock).exists(vserverURI2);
		doReturn(Optional.of(ssc_1_trusted_port_0_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_trusted_port_0_uri);
		doReturn(Optional.of(ssc_1_service1_port_0_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_service1_port_0_uri);
		doReturn(Optional.of(ssc_1_mgmt_port_1_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_mgmt_port_1_uri);
		doReturn(Optional.empty()).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_mgmt_port_0_uri);
		doReturn(Optional.of(ssc_1_service2_port_0_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_service2_port_0_uri);
		doReturn(Optional.of(ssc_1_int_ha_port_0_plural)).when(aaiResourcesMock).get(LInterfaces.class,ssc_1_int_ha_port_0_uri);
		doReturn(Optional.of(test_port_1_plural)).when(aaiResourcesMock).get(LInterfaces.class,test_port_1_uri);
		doReturn(Optional.of(test_port_2_plural)).when(aaiResourcesMock).get(LInterfaces.class,test_port_2_uri);
		
		doReturn(true).when(aaiResourcesMock).exists(service2_sub_1_uri);
		doReturn(true).when(aaiResourcesMock).exists(service1_sub_0_uri);
		doReturn(true).when(aaiResourcesMock).exists(service1_sub_1_uri);

		boolean exists = auditNova.auditAllVserversDoExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(false, exists);
		
		boolean doNotExist = auditNova.auditAllVserversDoNotExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(false, doNotExist);
	}

	@Test
	public void audit_Vserver_Found_Test_Network_Not_Found_Second_Server()
			throws JsonParseException, JsonMappingException, IOException {
		doReturn(true).when(aaiResourcesMock).exists(vserverURI);
		doReturn(true).when(aaiResourcesMock).exists(vserverURI2);
		doReturn(Optional.of(ssc_1_trusted_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_trusted_port_0_uri);
		doReturn(Optional.of(ssc_1_service1_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_service1_port_0_uri);
		doReturn(Optional.of(ssc_1_mgmt_port_1_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_mgmt_port_1_uri);
		doReturn(Optional.of(ssc_1_mgmt_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_mgmt_port_0_uri);
		doReturn(Optional.of(ssc_1_service2_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_service2_port_0_uri);
		doReturn(Optional.of(ssc_1_int_ha_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_int_ha_port_0_uri);
		doReturn(Optional.of(test_port_1_plural)).when(aaiResourcesMock).get(LInterface.class,test_port_1_uri);
		doReturn(Optional.empty()).when(aaiResourcesMock).get(LInterface.class,test_port_2_uri);		
		doReturn(true).when(aaiResourcesMock).exists(service2_sub_1_uri);
		doReturn(true).when(aaiResourcesMock).exists(service1_sub_0_uri);
		doReturn(true).when(aaiResourcesMock).exists(service1_sub_1_uri);
		boolean exists = auditNova.auditAllVserversDoExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);
		assertEquals(false, exists);
		
		boolean doNotExist = auditNova.auditAllVserversDoNotExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(false, doNotExist);
	}

	@Test
	public void audit_Vservers_Not_Found_Test() throws JsonParseException, JsonMappingException, IOException {
		doReturn(false).when(aaiResourcesMock).exists(vserverURI);
		doReturn(false).when(aaiResourcesMock).exists(vserverURI2);
		
		boolean exists = auditNova.auditAllVserversDoExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(false, exists);
		
		boolean doNotExist = auditNova.auditAllVserversDoNotExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(true, doNotExist);
	}

	@Test
	public void audit_Vserver_first_Not_Found_Test() throws JsonParseException, JsonMappingException, IOException {
		doReturn(false).when(aaiResourcesMock).exists(vserverURI);
		doReturn(true).when(aaiResourcesMock).exists(vserverURI2);
		doReturn(Optional.of(test_port_1_plural)).when(aaiResourcesMock).get(LInterface.class,test_port_1_uri);
		doReturn(Optional.of(test_port_2_plural)).when(aaiResourcesMock).get(LInterface.class,test_port_2_uri);
		boolean exists = auditNova.auditAllVserversDoExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(false, exists);
		
		boolean doNotExist = auditNova.auditAllVserversDoNotExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(false, doNotExist);
	}

	@Test
	public void audit_Vserver_Second_Not_Found_Test() throws JsonParseException, JsonMappingException, IOException {
		doReturn(true).when(aaiResourcesMock).exists(vserverURI);
		doReturn(Optional.of(ssc_1_trusted_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_trusted_port_0_uri);
		doReturn(Optional.of(ssc_1_service1_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_service1_port_0_uri);
		doReturn(Optional.of(ssc_1_mgmt_port_1_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_mgmt_port_1_uri);
		doReturn(Optional.of(ssc_1_mgmt_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_mgmt_port_0_uri);
		doReturn(Optional.of(ssc_1_service2_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_service2_port_0_uri);
		doReturn(Optional.of(ssc_1_int_ha_port_0_plural)).when(aaiResourcesMock).get(LInterface.class,ssc_1_int_ha_port_0_uri);
		doReturn(Optional.of(test_port_1_plural)).when(aaiResourcesMock).get(LInterface.class,test_port_1_uri);
		doReturn(Optional.of(test_port_2_plural)).when(aaiResourcesMock).get(LInterface.class,test_port_2_uri);
		doReturn(true).when(aaiResourcesMock).exists(service2_sub_1_uri);
		doReturn(true).when(aaiResourcesMock).exists(service1_sub_0_uri);
		doReturn(true).when(aaiResourcesMock).exists(service1_sub_1_uri);
		doReturn(false).when(aaiResourcesMock).exists(vserverURI2);
		
		boolean exists = auditNova.auditAllVserversDoExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);
		assertEquals(false, exists);
		
		boolean doNotExist = auditNova.auditAllVserversDoNotExist(vserversToAudit, tenantId, cloudOwner, cloudRegion);		
		assertEquals(false, doNotExist);
	}

}
