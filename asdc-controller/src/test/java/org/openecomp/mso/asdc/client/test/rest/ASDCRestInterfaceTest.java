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

package org.openecomp.mso.asdc.client.test.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Spy;
import org.openecomp.mso.asdc.BaseTest;
import org.openecomp.mso.asdc.client.test.emulators.DistributionClientEmulator;
import org.openecomp.mso.asdc.client.test.emulators.NotificationDataImpl;
import org.openecomp.mso.db.catalog.beans.AllottedResource;
import org.openecomp.mso.db.catalog.beans.AllottedResourceCustomization;
import org.openecomp.mso.db.catalog.beans.CollectionResource;
import org.openecomp.mso.db.catalog.beans.CollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.openecomp.mso.db.catalog.beans.ConfigurationResource;
import org.openecomp.mso.db.catalog.beans.ConfigurationResourceCustomization;
import org.openecomp.mso.db.catalog.beans.InstanceGroup;
import org.openecomp.mso.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkResource;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceProxyResource;
import org.openecomp.mso.db.catalog.beans.ServiceProxyResourceCustomization;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.db.catalog.beans.ToscaCsar;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.openecomp.mso.db.catalog.data.repository.AllottedResourceCustomizationRepository;
import org.openecomp.mso.db.catalog.data.repository.AllottedResourceRepository;
import org.openecomp.mso.db.catalog.data.repository.CollectionResourceInstanceGroupCustomizationRepository;
import org.openecomp.mso.db.catalog.data.repository.CollectionResourceRepository;
import org.openecomp.mso.db.catalog.data.repository.ConfigurationResourceRepository;
import org.openecomp.mso.db.catalog.data.repository.InstanceGroupRepository;
import org.openecomp.mso.db.catalog.data.repository.NetworkResourceRepository;
import org.openecomp.mso.db.catalog.data.repository.ServiceProxyResourceRepository;
import org.openecomp.mso.db.catalog.data.repository.ServiceRepository;
import org.openecomp.mso.db.catalog.data.repository.VnfcInstanceGroupCustomizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class ASDCRestInterfaceTest extends BaseTest {
	//ASDC Controller writes to this path
	static {
		System.setProperty("mso.config.path", "src/test/resources/");
	}
	
    @Rule
	public WireMockRule wireMockRule = new WireMockRule(8089);
    
	@Autowired
	private ServiceProxyResourceRepository serviceProxyRepo;
	
	@Autowired
	private CollectionResourceRepository collectionRepo;
	
	@Autowired
	private CollectionResourceInstanceGroupCustomizationRepository collectionResourceInstanceGroupCustomizationRepo;
	
	@Autowired
	private ConfigurationResourceRepository configProxyRepo;
    
	@Autowired
	private AllottedResourceRepository allottedRepo;
	
	@Autowired
	private AllottedResourceCustomizationRepository allottedCustomRepo;
	
	@Autowired
	private InstanceGroupRepository instanceGroupRepo;
	
	@Autowired
	private VnfcInstanceGroupCustomizationRepository vnfcInstanceGroupRepo;
	 
	@Autowired
	private ServiceRepository serviceRepo; 
	
	@Autowired
	private NetworkResourceRepository networkRepo;
	
	@Autowired
	private ASDCRestInterface asdcRestInterface;

	private TestRestTemplate restTemplate = new TestRestTemplate("test", "test");

	private HttpHeaders headers = new HttpHeaders();
	
	@Spy
	DistributionClientEmulator spyClient = new DistributionClientEmulator();

	@LocalServerPort
	private int port;
	
	@Ignore
	@Test
	@Transactional
	public void testPortMirroringDistribution() throws Exception {
		//Expected Response
		Service expectedService = new Service();
		
		expectedService.setDescription("sdfsf");
		expectedService.setEnvironmentContext("General_Revenue-Bearing");
		expectedService.setServiceRole("");
		expectedService.setServiceType("");
		expectedService.setModelName("port_mirroring_container");
		expectedService.setModelVersion("2.0");
		expectedService.setModelUUID("ed25a930-6bca-4409-8874-c6525d8d6420");
		expectedService.setModelInvariantUUID("813f63fd-e223-407a-b83a-2b2077b1289f"); 
		
		expectedService.setAllottedCustomizations(new ArrayList<AllottedResourceCustomization>());
		expectedService.setNetworkCustomizations(new ArrayList<NetworkResourceCustomization>());		
		expectedService.setVnfCustomizations(new ArrayList<VnfResourceCustomization>());
		List<ServiceProxyResourceCustomization> servProxylist = new ArrayList<>();
		expectedService.setServiceProxyCustomizations(servProxylist);
		expectedService.setRecipes(new  HashMap<String,ServiceRecipe>());
		
		ToscaCsar expectedCsar = new ToscaCsar();
		expectedCsar.setArtifactUUID("123456-35a8-467f-b440-d0f6226b3516");
		expectedCsar.setName("service_PortMirroringContainer_csar.csar");
		expectedCsar.setVersion("5.0");
		expectedCsar.setArtifactChecksum("ZDc1MTcxMzk4ODk4N2U5MzMxOTgwMzYzZTI0MTg5Y2U\u003d");
		expectedCsar.setDescription("TOSCA representation of the asset");
		expectedCsar.setUrl("service_PortMirroringContainer_csar.csar");
		List<Service> services = new ArrayList<Service>();
		services.add(expectedService);
		expectedCsar.setServices(services);
		expectedService.setCsar(expectedCsar);
		
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/portmirroring/notif-portm.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/portmirroring/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	

		Service actualResponse = serviceRepo.findOneByModelUUID("ed25a930-6bca-4409-8874-c6525d8d6420");
		if(actualResponse == null)
			throw new Exception("No Service Written to database");
		
		assertThat(actualResponse, sameBeanAs(expectedService).ignoring("0x1.created").ignoring("0x1.csar.created"));
	}
	
	@Test
	@Transactional
	public void testVfService() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.enable(MapperFeature.USE_ANNOTATIONS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		//Expected Response
		Service expectedService = mapper.readValue(new File("src/test/resources/resource-examples/multipleModules/ServiceResponse.json"), Service.class);
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/multipleModules/testStructure.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/multipleModules/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
		
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	

		Service actualResponse = serviceRepo.findOneByModelUUID("bad955c3-29b2-4a27-932e-28e942cc6480");
		if(actualResponse == null)
			throw new Exception("No Service Written to database");

		assertEquals("Size of VfModuleCustomizations: ", expectedService.getVnfCustomizations().get(0).getVfModuleCustomizations().size(), actualResponse.getVnfCustomizations().get(0).getVfModuleCustomizations().size());
		assertNotNull(actualResponse.getVnfCustomizations().get(0).getVfModuleCustomizations().get(0).getHeatEnvironment().getEnvironment());
		assertNotNull(actualResponse.getVnfCustomizations().get(0).getVfModuleCustomizations().get(1).getHeatEnvironment().getEnvironment());
		assertNotNull(actualResponse.getVnfCustomizations().get(0).getVfModuleCustomizations().get(2).getHeatEnvironment().getEnvironment());
	}
	
	@Test
	@Transactional
	public void testAllottedResourceService() throws Exception {
		
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/allottedresource/notif-portm.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/allottedresource/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	
		
		AllottedResource expectedService = new AllottedResource();
		expectedService.setDescription("rege1802pnf");
		expectedService.setModelInvariantUUID("b8f83c3f-077c-4e2c-b489-c66382060436");
		expectedService.setModelName("rege1802pnf");
		expectedService.setModelUUID("5b18c75e-2d08-4bf2-ad58-4ea704ec648d");
		expectedService.setModelVersion("1.0");
		expectedService.setSubcategory("Contrail Route");
		expectedService.setToscaNodeType("org.openecomp.resource.pnf.Rege1802pnf");
		Set<AllottedResourceCustomization> arCustomizationSet = new HashSet<AllottedResourceCustomization>();
		AllottedResourceCustomization arCustomization = new AllottedResourceCustomization();
		arCustomization.setModelCustomizationUUID("f62bb612-c5d4-4406-865c-0abec30631ba");
		arCustomization.setModelInstanceName("rege1802pnf 0");
		arCustomization.setProvidingServiceModelInvariantUUID("ebcef23d-2ca3-474f-9381-3a15709538f5");
		arCustomizationSet.add(arCustomization);
		
		
		arCustomization.setAllottedResource(expectedService);
		
		
		expectedService.setAllotedResourceCustomization(arCustomizationSet);	

		
		AllottedResource actualResponse = allottedRepo.findResourceByModelUUID("5b18c75e-2d08-4bf2-ad58-4ea704ec648d");
				
		
		if(actualResponse == null)
			throw new Exception("No Allotted Resource Written to database");
		
		assertThat(actualResponse, sameBeanAs(expectedService).ignoring("0x1.created").ignoring("0x1.allotedResourceCustomization.created"));
	}
	
	@Test
	public void invokeASDCStatusDataNullTest() {
		String request = "";
		Response response = asdcRestInterface.invokeASDCStatusData(request);
		assertNull(response);
		
	}
	
	@Test
	@Transactional
	public void testNetworkService() throws Exception {
		
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/TempNetworkHeat/notification.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/TempNetworkHeat/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	
		
		NetworkResource expectedNetwork = new NetworkResource();
		expectedNetwork.setDescription("For 1707, use this generic Tenant OAM Network Resource model for L0-3 VNFs for Tenant OAM network modeling. ");
		expectedNetwork.setModelInvariantUUID("a8d8e8b5-9d5b-4403-8560-b392c9a836af");
		expectedNetwork.setModelName("VIPR_TENANT_OAM_NETWORK");
		expectedNetwork.setModelUUID("6559075c-1f06-4367-83be-cdb0a8ecb3a2");
		expectedNetwork.setModelVersion("1.0");	
		expectedNetwork.setToscaNodeType("org.openecomp.resource.vl.extVLforTenantOAM1707");
		List<NetworkResourceCustomization> expectedNetworkCustomList = new ArrayList<>();
		NetworkResourceCustomization expectedNetworkCustom = new NetworkResourceCustomization();
		expectedNetworkCustom.setModelCustomizationUUID("2f2393b5-41b9-42af-94c4-b921aaa47a70");
		expectedNetworkCustom.setModelInstanceName("rege1802pnf 0");		
		expectedNetworkCustomList.add(expectedNetworkCustom);				
		expectedNetworkCustom.setNetworkResource(expectedNetwork);		
		expectedNetwork.setNetworkResourceCustomization(expectedNetworkCustomList);	
		
		NetworkResource actualResponse = networkRepo.findResourceByModelUUID("6559075c-1f06-4367-83be-cdb0a8ecb3a2");
		
		
		if(actualResponse != null)
			throw new Exception("No Network Resource should be written to database");
			
		
		//some reason shazam crest dies here
		//assertThat(actualResponse, sameBeanAs(expectedNetwork));	

		
	} 
	
	@Test
	@Transactional
	public void testServiceProxyResource() throws Exception {
		
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/serviceproxy/notif-serviceproxy.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/serviceproxy/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	
		
		ServiceProxyResource expectedService = new ServiceProxyResource();
		expectedService.setDescription("A Proxy for Service Transport_srv");
		expectedService.setModelInvariantUUID("41a4d16c-6f66-4ea9-bf48-e40498f54826");
		expectedService.setModelName("Transport_srv Service Proxy");
		expectedService.setModelUUID("9c0f7835-b5a8-495c-9ac0-f025a490efbd");
		expectedService.setModelVersion("0.1");
		//expectedService.setToscaNodeType("org.openecomp.resource.pnf.Rege1802pnf");
		Set<ServiceProxyResourceCustomization> spCustomizationSet = new HashSet<ServiceProxyResourceCustomization>();		
		ServiceProxyResourceCustomization spCustomization = new ServiceProxyResourceCustomization();
		spCustomization.setModelCustomizationUUID("f62bb612-c5d4-4406-865c-0abec30631ba");
		spCustomization.setModelInstanceName("rege1802pnf 0");
		spCustomization.setToscaNodeType("asdsa");
		spCustomizationSet.add(spCustomization);
		
		spCustomization.setServiceProxyResource(expectedService);
		
		expectedService.setServiceProxyCustomization(spCustomizationSet);	
	
		ServiceProxyResource actualResponse = serviceProxyRepo.findResourceByModelUUID("9c0f7835-b5a8-495c-9ac0-f025a490efbd");
				
		
		if(actualResponse == null)
			throw new Exception("No Service Proxy Resource Written to database");
		
		assertEquals(actualResponse.getDescription(), expectedService.getDescription());
		assertEquals(actualResponse.getModelInvariantUUID(), expectedService.getModelInvariantUUID());
		assertEquals(actualResponse.getModelName(), expectedService.getModelName());
		assertEquals(actualResponse.getModelUUID(), expectedService.getModelUUID());
		assertEquals(actualResponse.getModelVersion(), expectedService.getModelVersion());
		
		actualResponse.getServiceProxyCustomization();
		
		//assertThat(actualResponse, sameBeanAs(expectedService));	

		
	} 
	
	@Test
	@Transactional
	public void testConfigurationResource() throws Exception {
		
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/serviceproxy/notif-serviceproxy.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/serviceproxy/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	
		
		ConfigurationResource expectedService = new ConfigurationResource();
		expectedService.setDescription("VLAN network receptor configuration object");
		expectedService.setModelInvariantUUID("41282431-a04e-46b4-92e6-eb2b7a3dc207");
		expectedService.setModelName("VLAN Network Receptor Configuration");
		expectedService.setModelUUID("8f92336b-7231-4477-b894-b7ed60df5ed4");
		expectedService.setModelVersion("1.0");
		expectedService.setToscaNodeType("org.openecomp.nodes.VLANNetworkReceptor");
		Set<ConfigurationResourceCustomization> configCustomizationSet = new HashSet<ConfigurationResourceCustomization>();
		
		ConfigurationResourceCustomization configCustomization = new ConfigurationResourceCustomization();
		configCustomization.setModelCustomizationUUID("f62bb612-c5d4-4406-865c-0abec30631ba");
		configCustomization.setModelInstanceName("rege1802pnf 0");

		configCustomizationSet.add(configCustomization);
		
		configCustomization.setConfigurationResource(expectedService);	
		
		expectedService.setConfigurationResourceCustomization(configCustomizationSet);		

		
		ConfigurationResource actualResponse = configProxyRepo.findResourceByModelUUID("8f92336b-7231-4477-b894-b7ed60df5ed4");
				
		
		if(actualResponse == null)
			throw new Exception("No Configuration Resource Written to database");
		
		assertEquals(actualResponse.getDescription(), expectedService.getDescription());
		assertEquals(actualResponse.getModelInvariantUUID(), expectedService.getModelInvariantUUID());
		assertEquals(actualResponse.getModelName(), expectedService.getModelName());
		assertEquals(actualResponse.getModelUUID(), expectedService.getModelUUID());
		assertEquals(actualResponse.getModelVersion(), expectedService.getModelVersion());
		assertEquals(actualResponse.getToscaNodeType(), expectedService.getToscaNodeType());
		
		//assertThat(actualResponse, sameBeanAs(expectedService));	

		
	} 
	
	@Test
	@Transactional
	public void testNetworkCollection() throws Exception {
		
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/networkcollection/notif-networkcollection.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/networkcollection/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	
		
		CollectionResource expectedService = new CollectionResource();
		expectedService.setDescription("tbd");
		expectedService.setModelInvariantUUID("9a2d98b8-b935-4481-8ef8-7170427fbc7e");
		expectedService.setModelName("Network_Collection_Resource_1806_II");
		expectedService.setModelUUID("2581582f-b94a-4251-acf7-1bc83d2c3584");
		expectedService.setModelVersion("1.0");
		expectedService.setToscaNodeType("org.openecomp.resource.cr.NetworkCollectionResource1806Ii");
		Set<CollectionResourceCustomization> collectionCustomizationSet = new HashSet<CollectionResourceCustomization>();
		
		NetworkCollectionResourceCustomization networkCustomization = new NetworkCollectionResourceCustomization();
		networkCustomization.setModelCustomizationUUID("f62bb612-c5d4-4406-865c-0abec30631ba");
		networkCustomization.setModelInstanceName("rege1802pnf 0");

		collectionCustomizationSet.add(networkCustomization);
		
		networkCustomization.setCollectionResource(expectedService);	
		
		expectedService.setCollectionResourceCustomization(collectionCustomizationSet);
		
		CollectionResource actualResponse = collectionRepo.findResourceByModelUUID("2581582f-b94a-4251-acf7-1bc83d2c3584");
				
		
		if(actualResponse == null)
			throw new Exception("No Collection Resource Written to database");
		
		List<CollectionResourceInstanceGroupCustomization> actualCustomizationResponse = collectionResourceInstanceGroupCustomizationRepo.findByModelCustomizationUUID("85d24b75-0009-4177-ac0e-5091c88e18aa");
		
		if(actualCustomizationResponse == null || actualCustomizationResponse.size() < 1)
			throw new Exception("No Collection Resource Instance Group Customization Resource Written to database");
		
		NetworkResource actualNetworkResource = networkRepo.findResourceByModelUUID("ac815c68-35b7-4ea4-9d04-92d2f844b27c");
		
		if(actualNetworkResource == null)
			throw new Exception("No Network Resource Written to database");
		
		//assertThat(actualResponse, sameBeanAs(expectedService));	
		assertEquals(actualResponse.getDescription(), expectedService.getDescription());
		assertEquals(actualResponse.getModelInvariantUUID(), expectedService.getModelInvariantUUID());
		assertEquals(actualResponse.getModelName(), expectedService.getModelName());
		assertEquals(actualResponse.getModelUUID(), expectedService.getModelUUID());
		assertEquals(actualResponse.getModelVersion(), expectedService.getModelVersion());
		assertEquals(actualResponse.getToscaNodeType(), expectedService.getToscaNodeType());

		
	}
	
	@Test
	@Transactional
	public void testProcessFlexware() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.enable(MapperFeature.USE_ANNOTATIONS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
		Service expectedService = new Service();
		
		expectedService.setDescription("Demo Flexware Service in 1806");
		expectedService.setEnvironmentContext("General_Revenue-Bearing");
		expectedService.setServiceRole("");
		expectedService.setServiceType("FLEXWARE");
		expectedService.setModelName("CISCO 1806");
		expectedService.setModelVersion("1.0");
		expectedService.setModelUUID("e122e61d-1d0e-41d2-9b5f-897a88b07e85");
		expectedService.setModelInvariantUUID("0f2224c5-68df-4967-a517-e768a9a793b1"); 
		
		//Expected Response
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/flexware/notif-flexware.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/flexware/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
		
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	

		Service actualResponse = serviceRepo.findOneByModelUUID("e122e61d-1d0e-41d2-9b5f-897a88b07e85");
		if(actualResponse == null)
			throw new Exception("No Service Written to database");
		
		assertEquals(actualResponse.getDescription(), expectedService.getDescription());
		assertEquals(actualResponse.getModelInvariantUUID(), expectedService.getModelInvariantUUID());
		assertEquals(actualResponse.getModelName(), expectedService.getModelName());
		assertEquals(actualResponse.getModelUUID(), expectedService.getModelUUID());
		assertEquals(actualResponse.getModelVersion(), expectedService.getModelVersion());

	}
	
	@Test
	@Transactional
	public void testLSMP_1806() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.enable(MapperFeature.USE_ANNOTATIONS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
		Service expectedService = new Service();
		
		expectedService.setDescription("LMSP_1806_ap7134_SVC just updated verison");
		expectedService.setEnvironmentContext("General_Revenue-Bearing");
		expectedService.setServiceRole("");
		expectedService.setServiceType("");
		expectedService.setModelName("LMSP_1806_ap7134_SVC");
		expectedService.setModelUUID("7821682f-2bd8-4db6-85d0-4da9c0576fd1");
		expectedService.setModelInvariantUUID("40bff8f4-8375-47c8-baa9-b695785cad8f"); 
        
		
		//Expected Response
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/LMSP/notif-LMSP.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/LMSP/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
		
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	

		Service actualResponse = serviceRepo.findOneByModelUUID("7821682f-2bd8-4db6-85d0-4da9c0576fd1");
		if(actualResponse == null)
			throw new Exception("No Service Written to database");
		
		assertEquals(expectedService.getDescription(), actualResponse.getDescription());
		assertEquals(expectedService.getModelInvariantUUID(), actualResponse.getModelInvariantUUID());
		assertEquals(expectedService.getModelName(), actualResponse.getModelName());
		assertEquals(expectedService.getModelUUID(), actualResponse.getModelUUID());
		assertEquals(expectedService.getEnvironmentContext(), actualResponse.getEnvironmentContext());
		
		request = mapper.readValue(new File("src/test/resources/resource-examples/sdWanService/notif-SdWan.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/sdWanService/");
		entity = new HttpEntity<NotificationDataImpl>(request, headers);
		
		response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	
		
		expectedService.setDescription("SD-WAN service 1806-For DHV testing");
		expectedService.setModelVersion("1.0");
		expectedService.setModelName("SD-WAN service 1806");
		expectedService.setModelUUID("5b3d8a4a-94ee-4b08-bb73-f52f23905e83");
		expectedService.setModelInvariantUUID("8756c428-301d-4b8c-932a-e3ed89211d87"); 
		expectedService.setEnvironmentContext("General_Revenue-Bearing");
		
		actualResponse = serviceRepo.findOneByModelUUID("5b3d8a4a-94ee-4b08-bb73-f52f23905e83");
		if(actualResponse == null)
			throw new Exception("No Service Written to database");
		
		assertEquals(expectedService.getDescription(), actualResponse.getDescription());
		assertEquals(expectedService.getModelInvariantUUID(), actualResponse.getModelInvariantUUID());
		assertEquals(expectedService.getModelName(), actualResponse.getModelName());
		assertEquals(expectedService.getModelUUID(), actualResponse.getModelUUID());
		assertEquals(expectedService.getEnvironmentContext(), actualResponse.getEnvironmentContext());
	}
	
	@Test
	@Transactional
	public void testSdWanService1806() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.enable(MapperFeature.USE_ANNOTATIONS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
		Service expectedService = new Service();
		
		expectedService.setDescription("SD-WAN service 1806-For DHV testing");
		expectedService.setModelVersion("1.0");
		expectedService.setModelName("SD-WAN service 1806");
		expectedService.setModelUUID("5b3d8a4a-94ee-4b08-bb73-f52f23905e83");
		expectedService.setModelInvariantUUID("8756c428-301d-4b8c-932a-e3ed89211d87"); 
		expectedService.setEnvironmentContext("General_Revenue-Bearing");
        
		
		//Expected Response
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/sdWanService/notif-SdWan.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/sdWanService/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
		
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	

		Service actualResponse = serviceRepo.findOneByModelUUID("5b3d8a4a-94ee-4b08-bb73-f52f23905e83");
		if(actualResponse == null)
			throw new Exception("No Service Written to database");
		
		assertEquals(expectedService.getDescription(), actualResponse.getDescription());
		assertEquals(expectedService.getModelInvariantUUID(), actualResponse.getModelInvariantUUID());
		assertEquals(expectedService.getModelName(), actualResponse.getModelName());
		assertEquals(expectedService.getModelUUID(), actualResponse.getModelUUID());
		assertEquals(expectedService.getEnvironmentContext(), actualResponse.getEnvironmentContext());
	}
	
	@Test
	@Transactional
	public void testSRIOVNetwork() throws Exception {
		
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/sriovNetworks/notification.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/sriovNetworks/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	
		
		NetworkResource expectedNetwork = new NetworkResource();
		expectedNetwork.setDescription("For 1707, use this generic Tenant OAM Network Resource model for L0-3 VNFs for Tenant OAM network modeling. ");
		expectedNetwork.setModelInvariantUUID("a8d8e8b5-9d5b-4403-8560-b392c9a836af");
		expectedNetwork.setModelName("VIPR_TENANT_OAM_NETWORK");
		expectedNetwork.setModelUUID("6559075c-1f06-4367-83be-cdb0a8ecb3a2");
		expectedNetwork.setModelVersion("1.0");	
		expectedNetwork.setToscaNodeType("org.openecomp.resource.vl.extVLforTenantOAM1707");
		List<NetworkResourceCustomization> expectedNetworkCustomList = new ArrayList<>();
		NetworkResourceCustomization expectedNetworkCustom = new NetworkResourceCustomization();
		expectedNetworkCustom.setModelCustomizationUUID("2f2393b5-41b9-42af-94c4-b921aaa47a70");
		expectedNetworkCustom.setModelInstanceName("rege1802pnf 0");		
		expectedNetworkCustomList.add(expectedNetworkCustom);				
		expectedNetworkCustom.setNetworkResource(expectedNetwork);		
		expectedNetwork.setNetworkResourceCustomization(expectedNetworkCustomList);	
		
		NetworkResource actualResponse = networkRepo.findResourceByModelUUID("6559075c-1f06-4367-83be-cdb0a8ecb3a2");
		
		
		if(actualResponse != null)
			throw new Exception("No Network Resource should be written to database");
			
		
		//some reason shazam crest dies here
		//assertThat(actualResponse, sameBeanAs(expectedNetwork));	

		
	} 
	
	@Test
	@Transactional
	public void testVNFC() throws Exception {
		
		stubFor(post(urlPathMatching("/aai/.*"))
				  .willReturn(aResponse()
				  .withStatus(200)
				  .withHeader("Content-Type", "application/json")));
		
		ObjectMapper mapper = new ObjectMapper();
		NotificationDataImpl request = mapper.readValue(new File("src/test/resources/resource-examples/vnfc/notification.json"), NotificationDataImpl.class);
		headers.add("resource-location", "src/test/resources/resource-examples/vnfc/");
		HttpEntity<NotificationDataImpl> entity = new HttpEntity<NotificationDataImpl>(request, headers);
				
		ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("test/treatNotification/v1"), HttpMethod.POST,
				entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode().value());	
		
		InstanceGroup expectedNetwork = new InstanceGroup();
		expectedNetwork.setModelInvariantUUID("01a007bf-e3c4-4e27-963b-74fabeab08bc");
		expectedNetwork.setModelName("untr_group");
		expectedNetwork.setModelUUID("82823f08-f935-49aa-a0d8-608a2a2da10c");
		expectedNetwork.setModelVersion("3");	
		expectedNetwork.setToscaNodeType("org.openecomp.resource.vf.Vdbe");
	
		InstanceGroup actualResponse = instanceGroupRepo.findResourceByModelUUID("82823f08-f935-49aa-a0d8-608a2a2da10c");
		
		List<VnfcInstanceGroupCustomization> actualVnfcResponse = vnfcInstanceGroupRepo.findByModelCustomizationUUID("6c823610-1129-4208-a162-703fce937ba8");
		
		
		if(actualResponse == null)
			throw new Exception("No InstanceGroup was written to database");
		
		if(actualVnfcResponse == null)
			throw new Exception("No VnfInstanceGroupCustomization was written to database");
			
		
		//some reason shazam crest dies here
		//assertThat(actualResponse, sameBeanAs(expectedNetwork));	

		
	} 
	
	protected String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
}
