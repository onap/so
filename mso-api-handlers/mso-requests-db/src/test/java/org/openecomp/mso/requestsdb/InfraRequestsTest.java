/*
* ============LICENSE_START=======================================================
* ONAP : SO
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.openecomp.mso.requestsdb;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.Test;

public class InfraRequestsTest {
    
    InfraRequests ir=new InfraRequests();
    InfraRequests ir1=new InfraRequests("requestId", "action");
	Timestamp time=new Timestamp(123);
	long progress=111;
	@Test
	public void test() {
		ir.setAaiServiceId("aaiServiceId");
		ir.setAction("action");
		ir.setAicCloudRegion("aicCloudRegion");
		ir.setAicNodeClli("aicNodeClli");
		ir.setCallBackUrl("callBackUrl");
		ir.setClientRequestId("clientRequestId");
		ir.setConfigurationId("configurationId");
		ir.setConfigurationName("configurationName");
		ir.setCorrelator("correlator");
		ir.setEndTime(time);
		ir.setLastModifiedBy("lastModifiedBy");
		ir.setModifyTime(time);
		ir.setNetworkId("networkId");
		ir.setNetworkName("networkName");
		ir.setNetworkType("networkType");
		ir.setOperationalEnvId("operationalEnvId");
		ir.setOperationalEnvName("operationalEnvName");
		ir.setProgress(progress);
		ir.setProvStatus("provStatus");
		ir.setRequestAction("requestAction");
		ir.setRequestBody("requestBody");
		ir.setRequestId("requestId");
		ir.setRequestorId("requestorId");
		ir.setRequestScope("requestScope");
		ir.setRequestStatus("requestStatus");
		ir.setRequestType("requestType");
		ir.setResponseBody("responseBody");
		ir.setServiceInstanceId("serviceInstanceId");
		ir.setServiceInstanceName("serviceInstanceName");
		ir.setServiceType("serviceType");
		ir.setSource("source");
		ir.setStartTime(time);
		ir.setStatusMessage("statusMessage");
		ir.setTenantId("tenantId");
		ir.setVfModuleId("vfModuleId");
		ir.setVfModuleModelName("vfModuleModelName");
		ir.setVfModuleName("vfModuleName");
		ir.setVnfId("vnfId");
		ir.setVnfName("vnfName");
		ir.setVnfOutputs("vnfOutputs");
		ir.setVnfParams("vnfParams");
		ir.setVnfType("vnfType");
		ir.setVolumeGroupId("volumeGroupId");
		ir.setVolumeGroupName("volumeGroupName");
		assertEquals(ir.getAaiServiceId(), "aaiServiceId");
		assertEquals(ir.getAction(),"action");
		assertEquals(ir.getAicCloudRegion(),"aicCloudRegion");
		assertEquals(ir.getAicNodeClli(),"aicNodeClli");
		assertEquals(ir.getCallBackUrl(),"callBackUrl");
		assertEquals(ir.getClientRequestId(),"clientRequestId");
		assertEquals(ir.getConfigurationId(),"configurationId");
		assertEquals(ir.getConfigurationName(),"configurationName");
		assertEquals(ir.getCorrelator(),"correlator");
		assertEquals(ir.getEndTime(),time);
		assertEquals(ir.getLastModifiedBy(),"lastModifiedBy");
		assertEquals(ir.getModifyTime(),time);
		assertEquals(ir.getNetworkId(),"networkId");
		assertEquals(ir.getNetworkName(),"networkName");
		assertEquals(ir.getNetworkType(),"networkType");
		assertEquals(ir.getOperationalEnvId(),"operationalEnvId");
		assertEquals(ir.getOperationalEnvName(),"operationalEnvName");
	    assert(ir.getProgress().equals(progress));
		assertEquals(ir.getProvStatus(),"provStatus");
		assertEquals(ir.getRequestAction(),"requestAction");
		assertEquals(ir.getRequestBody(),"requestBody");
		assertEquals(ir.getRequestId(),"requestId");
		assertEquals(ir.getRequestorId(),"requestorId");
		assertEquals(ir.getRequestScope(),"requestScope");
		assertEquals(ir.getRequestStatus(),"requestStatus");
		assertEquals(ir.getRequestType(),"requestType");
		assertEquals(ir.getResponseBody(),"responseBody");
		assertEquals(ir.getServiceInstanceId(),"serviceInstanceId");
		assertEquals(ir.getServiceInstanceName(),"serviceInstanceName");
		assertEquals(ir.getServiceType(),"serviceType");
		assertEquals(ir.getSource(),"source");
		assertEquals(ir.getStartTime(),time);
		assertEquals(ir.getStatusMessage(),"statusMessage");
		assertEquals(ir.getTenantId(),"tenantId");
		assertEquals(ir.getVfModuleId(),"vfModuleId");
		assertEquals(ir.getVfModuleModelName(),"vfModuleModelName");
		assertEquals(ir.getVfModuleName(),"vfModuleName");
		assertEquals(ir.getVnfId(),"vnfId");
		assertEquals(ir.getVnfName(),"vnfName");
		assertEquals(ir.getVnfOutputs(),"vnfOutputs");
		assertEquals(ir.getVnfParams(),"vnfParams");
		assertEquals(ir.getVnfType(),"vnfType");
		assertEquals(ir.getVolumeGroupId(),"volumeGroupId");
		assertEquals(ir.getVolumeGroupName(),"volumeGroupName");
		
	}
	

}
