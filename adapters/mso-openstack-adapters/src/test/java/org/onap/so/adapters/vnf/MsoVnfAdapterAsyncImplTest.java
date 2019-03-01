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


import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.so.adapters.vnf.exceptions.VnfException;
import org.onap.so.entity.MsoRequest;
import org.onap.so.openstack.beans.VnfRollback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackGetStackVfModule_200;
import static org.onap.so.bpmn.mock.StubOpenStack.mockOpenStackResponseAccess;

public class MsoVnfAdapterAsyncImplTest extends BaseRestTestUtils {

	@Autowired
	MsoVnfAdapterAsyncImpl instance;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void healthCheckVNFTest() {
		instance.healthCheckA();
	}

	@Test
	public void createVNFTest() throws Exception {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		mockOpenStackResponseAccess(wireMockPort);
		mockOpenStackGetStackVfModule_200();
		stubFor(post(urlPathEqualTo("/notify/adapterNotify/updateVnfNotificationRequest")).withRequestBody
				(containing("messageId"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)));

		String vnfName = "DEV-VF-1802-it3-pwt3-v6-vSAMP10a-addon2-Replace-1001/stackId";
		String notificationUrl = "http://localhost:"+wireMockPort+"/notify/adapterNotify/updateVnfNotificationRequest";
		instance.createVnfA("mtn13", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", vnfName, "VFMOD",
				"volumeGroupHeatStackId|1", new HashMap<String, Object>(), Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, "messageId",
				msoRequest, notificationUrl);

		verify(1,postRequestedFor(urlEqualTo("/notify/adapterNotify/updateVnfNotificationRequest")));
	}

	@Test
	public void createVNFTest_Exception() throws Exception {
		String notificationUrl = "http://localhost:"+wireMockPort+"/notify/adapterNotify/updateVnfNotificationRequest";
		instance.createVnfA("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
				"volumeGroupHeatStackId|1", new HashMap<String, Object>(), Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, "messageId",
				null, notificationUrl);

		verify(1,postRequestedFor(urlEqualTo("/notify/adapterNotify/updateVnfNotificationRequest")));

	}

	@Test
	public void updateVnfTest() throws Exception{
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		stubFor(post(urlPathEqualTo("/notify/adapterNotify/updateVnfNotificationRequest")).withRequestBody
				(containing("messageId"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)));
		String notificationUrl = "http://localhost:"+wireMockPort+"/notify/adapterNotify/updateVnfNotificationRequest";
		instance.updateVnfA("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
				"volumeGroupHeatStackId|1", map, "messageId", msoRequest,
				notificationUrl);
	}

	@Test
	public void updateVnfTest_Exception() throws Exception{
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, Object> map = new HashMap<>();
		map.put("key1", "value1");
		stubFor(post(urlPathEqualTo("/notify/adapterNotify/updateVnfNotificationRequest")).withRequestBody
				(containing("messageId"))
				.willReturn(aResponse().withStatus(HttpStatus.SC_OK)));
		String notificationUrl = "http://localhost:"+wireMockPort+"/notify/adapterNotify/updateVnfNotificationRequest";
		instance.updateVnfA("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
				"volumeGroupHeatStackId|1", map, "messageId", msoRequest,
				notificationUrl);
		verify(1,postRequestedFor(urlEqualTo("/notify/adapterNotify/updateVnfNotificationRequest")));
	}

	@Test
	public void queryVnfTest() {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
			instance.queryVnfA("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", "messageId", msoRequest,
					"http://org.onap.so/notify/adapterNotify/updateVnfNotificationRequest");
	}

	@Test
	public void deleteVnfTest() {
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
			instance.deleteVnfA("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", "messageId", msoRequest,
					"http://org.onap.so/notify/adapterNotify/updateVnfNotificationRequest");
	}

	@Test
	public void rollbackVnfTest() {
		VnfRollback vnfRollBack = new VnfRollback();
		vnfRollBack.setCloudSiteId("mdt1");
		vnfRollBack.setTenantId("88a6ca3ee0394ade9403f075db23167e");
		vnfRollBack.setVnfId("ff5256d1-5a33-55df-13ab-12abad84e7ff");
			instance.rollbackVnfA(vnfRollBack, "messageId",
					"http://org.onap.so/notify/adapterNotify/updateVnfNotificationRequest");
	}
}
