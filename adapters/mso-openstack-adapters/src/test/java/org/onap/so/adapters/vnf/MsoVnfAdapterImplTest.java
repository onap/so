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

package org.onap.so.adapters.vnf;


import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.onap.so.adapters.vnf.exceptions.VnfAlreadyExists;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.adapters.vnf.exceptions.VnfNotFound;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.HeatTemplateParam;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.data.repository.VFModuleCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VnfResourceRepository;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.HeatStatus;
import org.onap.so.openstack.beans.StackInfo;
import org.onap.so.openstack.beans.VnfRollback;
import org.onap.so.openstack.exceptions.MsoException;
import org.onap.so.openstack.utils.MsoHeatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.xml.ws.Holder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_404;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackPutStack;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenstackGetWithResponse;


public class MsoVnfAdapterImplTest extends BaseRestTestUtils {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Autowired
	private MsoHeatUtils heatUtils;

	@Autowired
	MsoVnfAdapterImpl instance;

	String vnfName = "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId";

	@Before
	public void before() throws Exception {
		MockitoAnnotations.initMocks(this);
		WireMock.reset();
		setUp();
	}

	@Test
	@Ignore
	public void healthCheckVNFTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		instance.healthCheck();
	}

	@Test
	public void createVnfTest() throws Exception {
		StackInfo info = new StackInfo();
		info.setStatus(HeatStatus.CREATED);

		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_200();

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("MTN13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_HeatStatusUpdating() throws Exception {
		expectedException.expect(VnfAlreadyExists.class);
		mockOpenStackResponseAccess(wireMockPort);

		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack_Updating_VfModule.json")
						.withStatus(HttpStatus.SC_OK)));

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_HeatStatusUpdated() throws Exception {
		expectedException.expect(VnfAlreadyExists.class);
		mockOpenStackResponseAccess(wireMockPort);

		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_StackId.json")
						.withStatus(HttpStatus.SC_OK)));

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_HeatStatusFailed() throws Exception {
		expectedException.expect(VnfAlreadyExists.class);
		mockOpenStackResponseAccess(wireMockPort);

		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack_Failed_VfModule.json")
						.withStatus(HttpStatus.SC_OK)));

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_HeatStatusCreated() throws Exception {
		expectedException.expect(VnfAlreadyExists.class);
		mockOpenStackResponseAccess(wireMockPort);

		mockOpenStackGetStackVfModule_200();

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}


	@Test
	public void createVnfTest_ExceptionInGettingHeat() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_NestedHeatStatusNotFound() throws Exception {
		expectedException.expect(VnfException.class);
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_404();

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_ExceptionInGettingNestedHeat() throws Exception {
		expectedException.expect(VnfException.class);
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_404();
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_NestedBaseHeatStatus_NotFound() throws Exception {
		expectedException.expect(VnfException.class);
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_404();
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withBodyFile("OpenstackResponse_Stack_Created_VfModule.json")
						.withStatus(HttpStatus.SC_OK)));

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_ExceptionInGettingBaseNestedHeat() throws Exception {
		expectedException.expect(VnfException.class);
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_404();
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId")).willReturn(aResponse().withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfHeatStackId")).willReturn(aResponse().withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_ExceptionInCreateStack() throws Exception {
		expectedException.expect(VnfException.class);
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_404();
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId")).willReturn(aResponse().withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfHeatStackId")).willReturn(aResponse().withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));

		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		VfModule vfModule = new VfModule();
		vfModule.setIsBase(false);

		HeatTemplate heatTemplate = new HeatTemplate();
		heatTemplate.setTemplateBody("");
		heatTemplate.setTimeoutMinutes(200);
		vfModule.setModuleHeatTemplate(heatTemplate);
		vfModuleCustomization.setVfModule(vfModule);

		HeatEnvironment heatEnvironment = new HeatEnvironment();
		heatEnvironment.setEnvironment("ist");
		vfModuleCustomization.setHeatEnvironment(heatEnvironment);

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_ModelCustUuidIsNull() throws Exception {
		expectedException.expect(VnfException.class);
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_404();
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId")).willReturn(aResponse().withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfHeatStackId")).willReturn(aResponse().withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));

		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		VfModule vfModule = new VfModule();
		vfModule.setIsBase(false);

		HeatTemplate heatTemplate = new HeatTemplate();
		heatTemplate.setTemplateBody("");
		heatTemplate.setTimeoutMinutes(200);
		vfModule.setModuleHeatTemplate(heatTemplate);
		vfModuleCustomization.setVfModule(vfModule);

		HeatEnvironment heatEnvironment = new HeatEnvironment();
		heatEnvironment.setEnvironment("ist");
		vfModuleCustomization.setHeatEnvironment(heatEnvironment);

		VnfResource vnfResource = new VnfResource();
		vnfResource.setAicVersionMin("1");
		vnfResource.setAicVersionMin("3");


		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "XVFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", null, map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void createVnfTest_HeatEnvironment_ContainsParameters() throws Exception {
		expectedException.expect(VnfException.class);
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_404();
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId")).willReturn(aResponse().withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/baseVfHeatStackId")).willReturn(aResponse().withBodyFile("OpenstackResponse_Stack_Created_VfModule.json").withStatus(HttpStatus.SC_OK)));

		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.createVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "", vnfName, "", "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
				Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	private MsoRequest getMsoRequest() {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		return msoRequest;
	}

	@Test
	public void updateVnfTest_CloudSiteIdNotFound() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = getMsoRequest();

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.updateVfModule("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
				"volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
				"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void updateVnfTest_HeatStackNotFound() throws Exception {
		expectedException.expect(VnfNotFound.class);
		MsoRequest msoRequest = getMsoRequest();
		mockOpenStackResponseAccess(wireMockPort);
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.updateVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId",
				"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void updateVnfTest_ExceptionInGettingNestedHeatStack() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = getMsoRequest();
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_200();
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.updateVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId",
				"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void updateVnfTest_NestedHeatStackNotFound() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = getMsoRequest();
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_200();
		stubFor(get(urlPathEqualTo("/mockPublicUrl/stacks/volumeGroupHeatStackId"))
				.willReturn(aResponse().withHeader("Content-Type", "application/json")
						.withStatus(HttpStatus.SC_NOT_FOUND)));
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.updateVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId",
				"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void updateVnfTest_ExceptionInGettingNestedBaseHeatStack() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = getMsoRequest();
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_200();
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/volumeGroupHeatStackId",HttpStatus.SC_OK,"OpenstackResponse_Stack_Created_VfModule.json");
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/baseVfHeatStackId",HttpStatus.SC_INTERNAL_SERVER_ERROR,"OpenstackResponse_Stack_Created_VfModule.json");
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.updateVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId",
				"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void updateVnfTest_NestedBaseHeatStackNotFound() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = getMsoRequest();
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_200();
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/volumeGroupHeatStackId",HttpStatus.SC_OK,"OpenstackResponse_Stack_Created_VfModule.json");
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/baseVfHeatStackId",HttpStatus.SC_NOT_FOUND,"OpenstackResponse_Stack_Created_VfModule.json");
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.updateVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId",
				"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void updateVnfTest_MissingParams() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = getMsoRequest();
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_200();
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/volumeGroupHeatStackId",HttpStatus.SC_OK,"OpenstackResponse_Stack_Created_VfModule.json");
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/baseVfHeatStackId",HttpStatus.SC_OK,"OpenstackResponse_Stack_Created_VfModule.json");
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.updateVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId",
				"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void updateVnfTest_UpdateStackException() throws Exception {
		expectedException.expect(VnfException.class);
		MsoRequest msoRequest = getMsoRequest();
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_200();
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/volumeGroupHeatStackId",HttpStatus.SC_OK,"OpenstackResponse_Stack_Created_VfModule.json");
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/baseVfHeatStackId",HttpStatus.SC_OK,"OpenstackResponse_Stack_Created_VfModule.json");

		VfModuleCustomization vfModuleCustomization = getVfModuleCustomization();
		vfModuleCustomization.getVfModule().getModuleHeatTemplate().setParameters(new HashSet<>());
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.updateVfModule("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId",
				"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	public void updateVnfTest() throws Exception {
		MsoRequest msoRequest = getMsoRequest();
		mockOpenStackResponseAccess(wireMockPort);
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/"+vnfName,HttpStatus.SC_OK,"OpenstackResponse_Stack_UpdateComplete.json");
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/volumeGroupHeatStackId",HttpStatus.SC_OK,"OpenstackResponse_Stack_Created_VfModule.json");
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/baseVfHeatStackId",HttpStatus.SC_OK,"OpenstackResponse_Stack_Created_VfModule.json");
		mockOpenStackPutStack("null/stackId", HttpStatus.SC_OK);
		mockOpenstackGetWithResponse("/mockPublicUrl/stacks/null/stackId",HttpStatus.SC_OK,"OpenstackResponse_Stack_UpdateComplete.json");

		VfModuleCustomization vfModuleCustomization = getVfModuleCustomization();
		vfModuleCustomization.getVfModule().getModuleHeatTemplate().setParameters(new HashSet<>());
		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		instance.updateVfModule("MTN13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
				"volumeGroupHeatStackId", "baseVfHeatStackId", "vfModuleStackId",
				"b4ea86b4-253f-11e7-93ae-92361f002671", map, msoRequest, new Holder<Map<String, String>>(),
				new Holder<VnfRollback>());
	}

	@Test
	@Ignore
	public void deleteVnfTest() throws MsoException {

		Map<String, Object> outputs = new HashMap<>();
		outputs.put("Key1", "value1");
		when(heatUtils.queryStackForOutputs("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12")).thenReturn(outputs);

		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = getMsoRequest();
		try {
			instance.deleteVfModule("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
					new Holder<Map<String, String>>());
		} catch (Exception e) {

		}
	}

	private VfModuleCustomization getVfModuleCustomization() {
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		VfModule vfModule = new VfModule();
		vfModule.setIsBase(false);

		HeatTemplate heatTemplate = new HeatTemplate();
		heatTemplate.setTemplateBody("");
		heatTemplate.setTimeoutMinutes(200);
		HeatTemplateParam heatTemplateParam = new HeatTemplateParam();
		heatTemplateParam.setParamAlias("ParamAlias");
		heatTemplateParam.setRequired(true);
		heatTemplateParam.setParamName("test");
		Set set = new HashSet();
		set.add(heatTemplateParam);
		heatTemplate.setParameters(set);
		vfModule.setModuleHeatTemplate(heatTemplate);
		vfModuleCustomization.setVfModule(vfModule);

		HeatEnvironment heatEnvironment = new HeatEnvironment();
		heatEnvironment.setEnvironment("parameters:ist");
		vfModuleCustomization.setHeatEnvironment(heatEnvironment);
		return vfModuleCustomization;
	}


}
