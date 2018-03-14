/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.network;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.NetworkRollback;

public class MsoNetworkAdapterAsyncImplTest {

	@Test
	public void healthCheckATest() {
		MsoNetworkAdapterAsyncImpl mNAAimpl = new MsoNetworkAdapterAsyncImpl();
		mNAAimpl.healthCheckA();
	}
	
	@Test
	@Ignore // 1802 merge
	public void rollbackNetworkATest() {
		NetworkRollback nrb = new NetworkRollback();
		nrb.setCloudId("cloudId");
		nrb.setMsoRequest(new MsoRequest());
		nrb.setModelCustomizationUuid("modelCustomizationUuid");
		nrb.setNetworkCreated(true);
		nrb.setNetworkId("networkId");
		nrb.setNetworkName("networkName");
		nrb.setNetworkStackId("networkStackId");
		nrb.setNetworkType("networkType");
		nrb.setNeutronNetworkId("neutronNetworkId");
		nrb.setPhysicalNetwork("physicalNetwork");
		nrb.setTenantId("tenantId");
		nrb.setVlans(new ArrayList<>());

		MsoNetworkAdapterAsyncImpl impl = new MsoNetworkAdapterAsyncImpl();
		impl.rollbackNetworkA(nrb, "messageId", "/notificationUrl");
	}

	@Test
	@Ignore // 1802 merge
	public void deleteNetworkATest() {
		MsoNetworkAdapterAsyncImpl impl = new MsoNetworkAdapterAsyncImpl();
		impl.deleteNetworkA("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
				"messageId", new MsoRequest(), "/notificationUrl");
	}

	@Test
	@Ignore // 1802 merge
	public void updateNetworkATest() {
		MsoNetworkAdapterAsyncImpl impl = new MsoNetworkAdapterAsyncImpl();
		impl.updateNetworkA("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkId",
				"networkName", "physicalNetworkName", new ArrayList<>(), new ArrayList<>(), "messageId",
				new MsoRequest(), "/notificationUrl");
	}

	@Test
	@Ignore // 1802 merge
	public void queryNetworkATest() {
		MsoNetworkAdapterAsyncImpl impl = new MsoNetworkAdapterAsyncImpl();
		impl.queryNetworkA("cloudSiteId", "tenantId", "networkNameOrId", "messageId", new MsoRequest(),
				"/notificationUrl");
	}

	@Test
	@Ignore // 1802 merge
	public void createNetworkATest() {
		MsoNetworkAdapterAsyncImpl impl = new MsoNetworkAdapterAsyncImpl();
		impl.createNetworkA("cloudSiteId", "tenantId", "networkType", "modelCustomizationUuid", "networkName",
				"physicalNetworkName", new ArrayList<>(), false, false, new ArrayList<>(), "messageId",
				new MsoRequest(), "/notificationUrl");
	}
}
