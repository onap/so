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

import org.junit.Test;

public class ResourceOperationStatusTest {

	ResourceOperationStatus ros=new ResourceOperationStatus();
	ResourceOperationStatus ros1=new ResourceOperationStatus("serviceId", "operationId", "resourceTemplateUUID");
	Object obj=new Object();
	@Test
	public void test() {
	ros.setErrorCode("errorCode");
	ros.setJobId("jobId");
	ros.setOperationId("operationId");
	ros.setOperType("operType");
	ros.setProgress("progress");
	ros.setResourceInstanceID("resourceInstanceID");
	ros.setResourceTemplateUUID("resourceTemplateUUId");
	ros.setServiceId("serviceId");
	ros.setStatus("101");
	ros.setStatusDescription("statusDescription");
	
	assertEquals(ros.getErrorCode(), "errorCode");
	assertEquals(ros.getJobId(), "jobId");
	assertEquals(ros.getOperationId(), "operationId");
	assertEquals(ros.getOperType(), "operType");
	assertEquals(ros.getProgress(), "progress");
	assertEquals(ros.getResourceInstanceID(), "resourceInstanceID");
	assertEquals(ros.getResourceTemplateUUID(), "resourceTemplateUUId");
	assertEquals(ros.getServiceId(), "serviceId");
	assertEquals(ros.getStatus(), "101");
	assertEquals(ros.getStatusDescription(), "statusDescription");
	ros.equals(obj);
	ros.hashCode();
	}

}
