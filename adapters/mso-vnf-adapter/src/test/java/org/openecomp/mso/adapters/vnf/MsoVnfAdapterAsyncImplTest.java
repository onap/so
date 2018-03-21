/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.adapters.vnf;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapterAsyncImpl;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.VnfRollback;

public class MsoVnfAdapterAsyncImplTest {

	@Test
	public void healthCheckVNFTest() {
		MsoVnfAdapterAsyncImpl instance = new MsoVnfAdapterAsyncImpl();
		instance.healthCheckA();
	}

	@Test
	public void createVNFTest() {
		MsoVnfAdapterAsyncImpl instance = new MsoVnfAdapterAsyncImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		try {

			instance.createVnfA("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", new HashMap<>(), Boolean.FALSE, Boolean.TRUE, "messageId",
					null, "http://org.openecomp.mso/notify/adapterNotify/updateVnfNotificationRequest");
		} catch (Exception e) {

		}
	}

	@Test
	public void updateVnfTest() {
		MsoVnfAdapterAsyncImpl instance = new MsoVnfAdapterAsyncImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		try {
			instance.updateVnfA("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", map, "messageId", msoRequest,
					"http://org.openecomp.mso/notify/adapterNotify/updateVnfNotificationRequest");
		} catch (Exception e) {

		}
	}

	@Test
	public void queryVnfTest() {
		MsoVnfAdapterAsyncImpl instance = new MsoVnfAdapterAsyncImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		try {
			instance.queryVnfA("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", "messageId", msoRequest,
					"http://org.openecomp.mso/notify/adapterNotify/updateVnfNotificationRequest");
		} catch (Exception e) {

		}
	}

	@Test
	public void deleteVnfTest() {
		MsoVnfAdapterAsyncImpl instance = new MsoVnfAdapterAsyncImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		try {
			instance.deleteVnfA("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", "messageId", msoRequest,
					"http://org.openecomp.mso/notify/adapterNotify/updateVnfNotificationRequest");
		} catch (Exception e) {

		}
	}

	@Test
	public void rollbackVnfTest() {
		MsoVnfAdapterAsyncImpl instance = new MsoVnfAdapterAsyncImpl();
		VnfRollback vnfRollBack = new VnfRollback();
		vnfRollBack.setCloudSiteId("mdt1");
		vnfRollBack.setTenantId("88a6ca3ee0394ade9403f075db23167e");
		vnfRollBack.setVnfId("ff5256d1-5a33-55df-13ab-12abad84e7ff");
		try {
			instance.rollbackVnfA(vnfRollBack, "messageId",
					"http://org.openecomp.mso/notify/adapterNotify/updateVnfNotificationRequest");
		} catch (Exception e) {

		}
	}
}
