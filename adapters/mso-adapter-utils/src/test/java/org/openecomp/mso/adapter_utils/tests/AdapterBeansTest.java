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

package org.openecomp.mso.adapter_utils.tests;

import static org.junit.Assert.assertTrue;

import com.woorea.openstack.heat.model.Stack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.openecomp.mso.entity.MsoRequest;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.MsoTenant;
import org.openecomp.mso.openstack.beans.NetworkRollback;
import org.openecomp.mso.openstack.beans.Pool;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.beans.Subnet;
import org.openecomp.mso.openstack.beans.VnfRollback;

public class AdapterBeansTest {
	@Test
	public final void msoTenantTest() {
		MsoTenant tenant = new MsoTenant();
		tenant.setTenantId("1");
		assertTrue(tenant.getTenantId().equalsIgnoreCase("1"));
		tenant.setTenantName("TenantName");
		assertTrue(tenant.getTenantName().equalsIgnoreCase("TenantName"));
		Map<String, String> hm = new HashMap<>();
		hm.put("Key1", "value1");
		tenant.setMetadata(hm);
		assertTrue(tenant.getMetadata() != null);
		new MsoTenant("1", "TenantName", hm);
		// assertTrue(tenant.toString() != null);
	}

	@Test
	public final void networkRollbackTest() {
		NetworkRollback networkRollback = new NetworkRollback();
		networkRollback.setCloudId("cloudId");
		assertTrue(networkRollback.getCloudId().equalsIgnoreCase("cloudId"));
		networkRollback.setModelCustomizationUuid("modelCustomizationUuid");
		assertTrue(networkRollback.getModelCustomizationUuid().equalsIgnoreCase("modelCustomizationUuid"));
		MsoRequest msoRequest = new MsoRequest();
		networkRollback.setMsoRequest(msoRequest);
		networkRollback.getMsoRequest();
		// assertTrue(networkRollback.getMsoRequest() == null);
		networkRollback.setNetworkCreated(Boolean.TRUE);
		assertTrue(networkRollback.getNetworkCreated());
		networkRollback.setNetworkId("networkId");
		assertTrue(networkRollback.getNetworkId().equalsIgnoreCase("networkId"));
		networkRollback.setNetworkName("networkName");
		assertTrue(networkRollback.getNetworkName().equalsIgnoreCase("networkName"));
		networkRollback.setNetworkStackId("networkStackId");
		assertTrue(networkRollback.getNetworkStackId().equalsIgnoreCase("networkStackId"));
		networkRollback.setNetworkType("networkType");
		assertTrue(networkRollback.getNetworkType().equalsIgnoreCase("networkType"));
		networkRollback.setNeutronNetworkId("neutronNetworkId");
		assertTrue(networkRollback.getNeutronNetworkId().equalsIgnoreCase("neutronNetworkId"));
		networkRollback.setPhysicalNetwork("physicalNetwork");
		assertTrue(networkRollback.getPhysicalNetwork().equalsIgnoreCase("physicalNetwork"));
		networkRollback.setTenantId("tenantId");
		assertTrue(networkRollback.getTenantId().equalsIgnoreCase("tenantId"));
		List<Integer> al = new ArrayList<>();
		al.add(1);
		al.add(2);
		networkRollback.setVlans(al);
		assertTrue(networkRollback.getVlans() != null);
		assertTrue(networkRollback.toString() != null);
	}

	@Test
	public final void poolTest() {
		Pool p = new Pool();
		p.setStart("start");
		p.getStart();
		p.setEnd("end");
		p.getEnd();
		p.toString();
	}

	@Test
	public final void stackInfoTest() {
		StackInfo stackInfo = new StackInfo();
		new StackInfo(new Stack());
		new StackInfo("name", HeatStatus.CREATED, "statusmessage", new HashMap<>());
		new StackInfo("name", HeatStatus.CREATED);
		stackInfo.setCanonicalName("Canonicalname");
		stackInfo.getCanonicalName();
		stackInfo.setName("name");
		stackInfo.getName();
		stackInfo.setOutputs(new HashMap<>());
		stackInfo.getOutputs();
		stackInfo.setParameters(new HashMap<>());
		stackInfo.getParameters();
		stackInfo.setStatus(HeatStatus.CREATED);
		stackInfo.getStatus();
		stackInfo.setStatusMessage("statusMessage");
		stackInfo.getStatusMessage();
	}

	@Test
	public final void subnetTest() {
		Subnet subnet = new Subnet();
		subnet.setAllocationPools(new ArrayList<>());
		subnet.getAllocationPools();
		subnet.setCidr("cidr");
		subnet.getCidr();
		subnet.setDnsNameServers(new ArrayList<>());
		subnet.getDnsNameServers();
		subnet.setEnableDHCP(true);
		subnet.getEnableDHCP();
		subnet.setGatewayIp("gatewayIp");
		subnet.getGatewayIp();
		subnet.setHostRoutes(new ArrayList<>());
		subnet.getHostRoutes();
		subnet.setIpVersion("ipVersion");
		subnet.getIpVersion();
		subnet.setNeutronId("neutronId");
		subnet.getNeutronId();
		subnet.setSubnetId("subnetId");
		subnet.getSubnetId();
		subnet.setSubnetName("subnetName");
		subnet.getSubnetName();
		subnet.toString();
	}

	@Test
	public final void vnfRollbackTest() {
		VnfRollback vnfRollback = new VnfRollback();
		new VnfRollback("vnfId", "tenantId", "cloudSiteId", true, true, new MsoRequest(), "volumeGroupName",
				"volumeGroupId", "requestType", "modelCustomizationUuid");
		vnfRollback.setBaseGroupHeatStackId("baseGroupHeatStackId");
		vnfRollback.getBaseGroupHeatStackId();
		vnfRollback.setCloudSiteId("cloudId");
		vnfRollback.getCloudSiteId();
		vnfRollback.setIsBase(false);
		vnfRollback.isBase();
		vnfRollback.setModelCustomizationUuid("modelCustomizationUuid");
		vnfRollback.getModelCustomizationUuid();
		vnfRollback.setMsoRequest(new MsoRequest());
		vnfRollback.getMsoRequest();
		vnfRollback.setRequestType("requestType");
		vnfRollback.getRequestType();
		vnfRollback.setTenantCreated(true);
		vnfRollback.getTenantCreated();
		vnfRollback.setTenantId("tenantId");
		vnfRollback.getTenantId();
		vnfRollback.setVfModuleStackId("vfModuleStackId");
		vnfRollback.getVfModuleStackId();
		vnfRollback.setVnfCreated(true);
		vnfRollback.getVnfCreated();
		vnfRollback.setVnfId("vnfId");
		vnfRollback.getVnfId();
		vnfRollback.setVolumeGroupHeatStackId("volumeGroupHeatStackId");
		vnfRollback.getVolumeGroupHeatStackId();
		vnfRollback.setVolumeGroupId("volumeGroupId");
		vnfRollback.getVolumeGroupId();
		vnfRollback.setVolumeGroupName("volumeGroupName");
		vnfRollback.getVolumeGroupName();
		vnfRollback.toString();
	}
}
