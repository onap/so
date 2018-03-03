/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Intellectual Property. All rights reserved.
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

import org.junit.Test;
import org.openecomp.mso.openstack.beans.Pool;
import org.openecomp.mso.openstack.beans.Subnet;

public class NetworkBeansTest {

	/**
	 * Test case for coverage
	 */
	@Test()
	public final void bpelRestClientPOJOTest() {
		try {
			BpelRestClient bpelRestClient = new BpelRestClient();
			bpelRestClient.setConnectTimeout(180);
			bpelRestClient.setCredentials("credentials");
			bpelRestClient.setRetryCount(-1);
			bpelRestClient.setRetryInterval(2);
			bpelRestClient.setSocketTimeout(10);
			bpelRestClient.equals(bpelRestClient);
			bpelRestClient.bpelPost("toBpelStr", "bpelUrl", true);
			bpelRestClient.getConnectTimeout();
			bpelRestClient.getCredentials();
			bpelRestClient.getLastResponse();
			bpelRestClient.getLastResponseCode();
			bpelRestClient.getRetryCount();
			bpelRestClient.getRetryInterval();
			bpelRestClient.getRetryList();
			bpelRestClient.getSocketTimeout();
			bpelRestClient.hashCode();
			bpelRestClient.toString();

			ContrailPolicyRef contrailPolicyRef = new ContrailPolicyRef();
			contrailPolicyRef.populate("major", "minor");
			contrailPolicyRef.toJsonNode();
			contrailPolicyRef.toJsonString();
			contrailPolicyRef.toString();

			ContrailPolicyRefSeq contrailPolicyRefSeq = new ContrailPolicyRefSeq();
			ContrailPolicyRefSeq contrailPolicyRefSeq2 = new ContrailPolicyRefSeq("", "");
			contrailPolicyRefSeq.setMajor("major");
			contrailPolicyRefSeq.setMinor("minor");
			contrailPolicyRefSeq.getMajor();
			contrailPolicyRefSeq.getMinor();
			contrailPolicyRefSeq.toString();

			ContrailSubnet contrailSubnet = new ContrailSubnet();
			contrailSubnet.setAddrFromStart(true);
			contrailSubnet.setDefaultGateway("defaultGateway");
			contrailSubnet.setEnableDhcp(true);
			contrailSubnet.setPools(new ArrayList<>());
			contrailSubnet.setSubnet(new ContrailSubnetIp());
			contrailSubnet.setSubnetName("subnetName");
			contrailSubnet.getAllocationPools();
			contrailSubnet.getDefaultGateway();
			contrailSubnet.getSubnet();
			contrailSubnet.getSubnetName();
			contrailSubnet.isAddrFromStart();
			contrailSubnet.isEnableDhcp();
			contrailSubnet.populateWith(new Subnet());
			contrailSubnet.toJsonNode();
			contrailSubnet.toJsonString();
			contrailSubnet.toString();

			ContrailSubnetIp contrailSubnetIp = new ContrailSubnetIp();
			contrailSubnetIp.setIpPrefix("ipPrefix");
			contrailSubnetIp.setIpPrefixLen("ipPrefixLen");
			contrailSubnetIp.getIpPrefix();
			contrailSubnetIp.getIpPrefixLen();
			contrailSubnetIp.toString();

			ContrailSubnetPool contrailSubnetPool = new ContrailSubnetPool();
			contrailSubnetPool.setEnd("end");
			contrailSubnetPool.setStart("start");
			contrailSubnetPool.getEnd();
			contrailSubnetPool.getStart();
			contrailSubnetPool.populateWith(new Pool());
			contrailSubnetPool.toString();

			// HealthCheckHandler healthCheckHandler = new HealthCheckHandler();
			// healthCheckHandler.healthcheck("60c3e96e-0970-4871-b6e0-3b6de7561519");
			// healthCheckHandler.healthcheck("requestId");
		} catch (Exception e) {
			assert (false);

		}
	}
}
