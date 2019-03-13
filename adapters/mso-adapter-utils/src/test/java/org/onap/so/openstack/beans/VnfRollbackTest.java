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

package org.onap.so.openstack.beans;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.so.BaseTest;
import org.onap.so.entity.MsoRequest;
import org.springframework.beans.factory.annotation.Autowired;

public class VnfRollbackTest extends BaseTest {
	@Autowired
	private VnfRollback vnfRollback;
	
	private String vnfId = "testVnfId";
	private String tenantId = "testTenantId";
	private String cloudOwner = "testCloudOwner";
	private String cloudSiteId = "testCloudSiteId";
	private boolean tenantCreated = true;
	private boolean vnfCreated = true;
	private MsoRequest msoRequest = new MsoRequest();
	private String volumeGroupName = "testVolumeGroupName";
	private String volumeGroupId = "testVolumeGroupId";
	private String requestType = "testRequestType";
	private String modelCustomizationUuid = "testModelCustimizationUuid";
	private String orchestrationMode = "testOrchestrationMode";
	private static final String VNF_ROLLBACK_STRING = "VnfRollback: cloud=testCloudSiteId, cloudOwner=testCloudOwner, tenant=testTenantId, vnf=testVnfId, "
			+ "tenantCreated=true, vnfCreated=true, requestType = testRequestType, modelCustomizationUuid=testModelCustimizationUuid, mode=testOrchestrationMode";
	
	@Test
	public void VnfRollbackInstantiationTest() {
		vnfRollback = new VnfRollback(vnfId, tenantId, cloudOwner, cloudSiteId, tenantCreated, vnfCreated,
				msoRequest, volumeGroupName, volumeGroupId, requestType, modelCustomizationUuid);
		
		assertEquals(vnfId, vnfRollback.getVnfId());
		assertEquals(tenantId, vnfRollback.getTenantId());
		assertEquals(cloudOwner, vnfRollback.getCloudOwner());
		assertEquals(cloudSiteId, vnfRollback.getCloudSiteId());
		assertEquals(tenantCreated, vnfRollback.getTenantCreated());
		assertEquals(vnfCreated, vnfRollback.getVnfCreated());
		assertEquals(msoRequest, vnfRollback.getMsoRequest());
		assertEquals(volumeGroupName, vnfRollback.getVolumeGroupName());
		assertEquals(volumeGroupId, vnfRollback.getVolumeGroupId());
		assertEquals(requestType, vnfRollback.getRequestType());
		assertEquals(modelCustomizationUuid, vnfRollback.getModelCustomizationUuid());
	}
	
	@Test
	public void VnfRollbackInstantiationOrchestrationModeTest() {
		vnfRollback = new VnfRollback(vnfId, tenantId, cloudOwner, cloudSiteId, tenantCreated, vnfCreated,
				msoRequest, volumeGroupName, volumeGroupId, requestType, modelCustomizationUuid, orchestrationMode);
		
		assertEquals(vnfId, vnfRollback.getVnfId());
		assertEquals(tenantId, vnfRollback.getTenantId());
		assertEquals(cloudOwner, vnfRollback.getCloudOwner());
		assertEquals(cloudSiteId, vnfRollback.getCloudSiteId());
		assertEquals(tenantCreated, vnfRollback.getTenantCreated());
		assertEquals(vnfCreated, vnfRollback.getVnfCreated());
		assertEquals(msoRequest, vnfRollback.getMsoRequest());
		assertEquals(volumeGroupName, vnfRollback.getVolumeGroupName());
		assertEquals(volumeGroupId, vnfRollback.getVolumeGroupId());
		assertEquals(requestType, vnfRollback.getRequestType());
		assertEquals(modelCustomizationUuid, vnfRollback.getModelCustomizationUuid());
		assertEquals(orchestrationMode, vnfRollback.getMode());
	}
	
	@Test
	public void toStringTest() {
		vnfRollback = new VnfRollback(vnfId, tenantId, cloudOwner, cloudSiteId, tenantCreated, vnfCreated,
				msoRequest, volumeGroupName, volumeGroupId, requestType, modelCustomizationUuid, orchestrationMode);
		
		assertEquals(VNF_ROLLBACK_STRING, vnfRollback.toString());
	}
}
