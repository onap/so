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
package org.openecomp.mso.client.aai.objects;

import static org.junit.Assert.*;

import org.junit.Test;

public class AAIServiceInstanceTest {

	@Test
	public void test() {
		AAIServiceInstance aaiSI= new AAIServiceInstance();
		aaiSI.setEnvironmentContext("environmentContext");
		aaiSI.setModelInvariantUuid("modelInvariantUuid");
		aaiSI.setModelUuid("modelUuid");
		aaiSI.setoStatus("oStatus");
		aaiSI.setServiceInstanceId("serviceInstanceId");
		aaiSI.setServiceInstanceName("serviceInstanceName");
		aaiSI.setServiceRole("serviceRole");
		aaiSI.setServiceType("serviceType");
		aaiSI.setWorkloadContext("workloadContext");
		assertEquals(aaiSI.getEnvironmentContext(), "environmentContext");
		assertEquals(aaiSI.getModelInvariantUuid(), "modelInvariantUuid");
		assertEquals(aaiSI.getModelUuid(), "modelUuid");
		assertEquals(aaiSI.getoStatus(), "oStatus");
		assertEquals(aaiSI.getServiceInstanceId(), "serviceInstanceId");
		assertEquals(aaiSI.getServiceInstanceName(), "serviceInstanceName");
		assertEquals(aaiSI.getServiceRole(), "serviceRole");
		assertEquals(aaiSI.getServiceType(), "serviceType");
		assertEquals(aaiSI.getWorkloadContext(), "workloadContext");
		aaiSI.withServiceInstance("serviceInstanceId");
		assert(aaiSI.getUri()!=null);
	}

}
