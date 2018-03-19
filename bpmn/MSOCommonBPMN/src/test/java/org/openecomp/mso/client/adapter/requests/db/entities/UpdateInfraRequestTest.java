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

package org.openecomp.mso.client.adapter.requests.db.entities;

import org.junit.Test;

public class UpdateInfraRequestTest {

	@Test
	public void test() {
		UpdateInfraRequest uir=new UpdateInfraRequest();
		RequestStatusType requestStatus=RequestStatusType.COMPLETE;
		uir.setConfigurationId("configurationId");
		uir.setConfigurationName("configurationName");
		uir.setLastModifiedBy("lastModifiedBy");
		uir.setNetworkId("networkId");
		uir.setProgress("progress");
		uir.setRequestId("requestId");
		uir.setRequestStatus(requestStatus);
		uir.setResponseBody("responseBody");
		uir.setServiceInstanceId("serviceInstanceId");
		uir.setServiceInstanceName("serviceInstanceName");
		uir.setStatusMessage("statusMessage");
		uir.setVfModuleId("vfModuleId");
		uir.setVfModuleName("vfModuleName");
		uir.setVnfId("vnfId");
		uir.setVnfOutputs("vnfOutputs");
		uir.setVolumeGroupId("volumeGroupId");
		assert(uir.getConfigurationId().equals("configurationId"));
		assert(uir.getConfigurationName().equals("configurationName"));
		assert(uir.getLastModifiedBy().equals("lastModifiedBy"));
		assert(uir.getNetworkId().equals("networkId"));
		assert(uir.getProgress().equals("progress"));
        assert(uir.getRequestId().equals("requestId"));
        assert(uir.getRequestStatus().equals(requestStatus));
        assert(uir.getResponseBody().equals("responseBody"));
        assert(uir.getServiceInstanceId().equals("serviceInstanceId"));
        assert(uir.getServiceInstanceName().equals("serviceInstanceName"));
        assert(uir.getStatusMessage().equals("statusMessage"));
        assert(uir.getVfModuleId().equals("vfModuleId"));
        assert(uir.getVnfOutputs().equals("vnfOutputs"));
        assert(uir.getVolumeGroupId().equals("volumeGroupId"));
        assert(uir.getVfModuleName().equals("vfModuleName"));
        assert(uir.getVnfId().equals("vnfId"));
	}
}
