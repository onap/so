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
import javax.xml.ws.Holder;
import org.junit.Test;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapterImpl;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.VnfRollback;

public class MsoVnfAdapterImplTest {

	@Test
	public void healthCheckVNFTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		instance.healthCheck();
	}

	@Test
	public void createVnfTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		try {
			instance.createVfModule("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
					Boolean.FALSE, Boolean.TRUE, msoRequest, new Holder<>(), new Holder<>(),
                new Holder<>());
		} catch (Exception e) {

		}
	}

	@Test
	public void updateVnfTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		try {
			instance.updateVfModule("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
					"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<>(),
                new Holder<>());
		} catch (Exception e) {

		}
	}

	@Test
	public void deleteVnfTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		try {
			instance.deleteVfModule("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
                new Holder<>());
		} catch (Exception e) {

		}
	}

}
