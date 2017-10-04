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

package org.openecomp.mso.adapters.vnf.test;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Holder;

import org.junit.Test;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapterImpl;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.utils.MsoHeatUtils;

import mockit.Mock;
import mockit.MockUp;

public class MsoVnfAdapterImplTest {

	@Test
	public void healthCheckVNFTest() {
		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		instance.healthCheck();
	}

	@Test
	public void createVnfTest() {

		new MockUp<MsoHeatUtils>() {
			@Mock
			public StackInfo queryStack(String cloudSiteId, String tenantId, String stackName) throws MsoException {
				StackInfo info = new StackInfo();
				info.setStatus(HeatStatus.CREATED);
				return info;
			}
		};

		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		try {
			instance.createVfModule("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", "baseVfHeatStackId", "88a6ca3ee0394ade9403f075db23167e", map,
					Boolean.FALSE, Boolean.TRUE, msoRequest, new Holder<>(), new Holder<Map<String, String>>(),
					new Holder<VnfRollback>());
		} catch (Exception e) {

		}
	}

	@Test
	public void updateVnfTest() {

		new MockUp<MsoHeatUtils>() {
			@Mock
			public StackInfo queryStack(String cloudSiteId, String tenantId, String stackName) throws MsoException {
				StackInfo info = new StackInfo();
				info.setStatus(HeatStatus.CREATED);
				return info;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public VfModuleCustomization getVfModuleCustomizationByModelCustomizationId(String modelCustomizationUuid) {
				VfModuleCustomization vfcModule = new VfModuleCustomization();
				VfModule vfm = new VfModule();
				vfm.setVnfResourceModelUUId("88a6ca3ee0394ade9403f075db23167e");
				vfcModule.setVfModule(vfm);
				return vfcModule;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public VnfResource getVnfResourceByModelUuid(String modelUuid) {
				VnfResource vnfResource = new VnfResource();
				vnfResource.setAicVersionMin("1");
				vnfResource.setAicVersionMax("2");
				return vnfResource;
			}
		};

		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");

		Map<String, String> map = new HashMap<>();
		map.put("key1", "value1");
		try {
			instance.updateVfModule("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vnf", "1", "vSAMP12", "VFMOD",
					"volumeGroupHeatStackId|1", "baseVfHeatStackId", "vfModuleStackId",
					"88a6ca3ee0394ade9403f075db23167e", map, msoRequest, new Holder<Map<String, String>>(),
					new Holder<VnfRollback>());
		} catch (Exception e) {

		}
	}

	@Test
	public void deleteVnfTest() {
		new MockUp<MsoHeatUtils>() {
			@Mock
			public Map<String, Object> queryStackForOutputs(String cloudSiteId, String tenantId, String stackName)
					throws MsoException {
				
				Map<String, Object> outputs = new HashMap<>();
				outputs.put("Key1", "value1");
				return outputs;
			}
		};

		MsoVnfAdapterImpl instance = new MsoVnfAdapterImpl();
		MsoRequest msoRequest = new MsoRequest();
		msoRequest.setRequestId("12345");
		msoRequest.setServiceInstanceId("12345");
		try {
			instance.deleteVfModule("mdt1", "88a6ca3ee0394ade9403f075db23167e", "vSAMP12", msoRequest,
					new Holder<Map<String, String>>());
		} catch (Exception e) {

		}
	}

}
