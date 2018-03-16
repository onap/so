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
package org.openecomp.mso.bpmn.common.recipe;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResourceInputTest {

	ResourceInput ri=new ResourceInput();
	@Test
	public void test() {
		ri.setResourceInstanceName("resourceInstanceName");
		ri.setResourceInstanceDes("resourceInstanceDes");
		ri.setGlobalSubscriberId("globalSubscriberId");
		ri.setServiceType("serviceType");
		ri.setServiceInstanceId("serviceId");
		ri.setOperationId("operationId");
		ri.setResourceInvariantUuid("resourceInvariantUuid");
		ri.setResourceUuid("resourceUuid");
		ri.setResourceCustomizationUuid("resourceCustomizationUuid");
		ri.setResourceInstancenUuid("resourceInstancenUuid");
		ri.setResourceParameters("resourceParameters");
		ri.setOperationType("operationType");
		assertEquals(ri.getResourceInstanceName(), "resourceInstanceName");
		assertEquals(ri.getResourceInstanceDes(), "resourceInstanceDes");
		assertEquals(ri.getGlobalSubscriberId(), "globalSubscriberId");
		assertEquals(ri.getServiceType(), "serviceType");
		assertEquals(ri.getServiceInstanceId(), "serviceId");
		assertEquals(ri.getOperationId(), "operationId");
		assertEquals(ri.getResourceInvariantUuid(), "resourceInvariantUuid");
		assertEquals(ri.getResourceUuid(), "resourceUuid");
		assertEquals(ri.getResourceCustomizationUuid(), "resourceCustomizationUuid");
		assertEquals(ri.getResourceInstancenUuid(), "resourceInstancenUuid");
		assertEquals(ri.getResourceParameters(), "resourceParameters");
		assertEquals(ri.getOperationType(), "operationType");
		}
	@Test
	public void testToString(){
		assert(ri.toString()!=null);
	}
}
