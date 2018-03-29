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

public class OperationStatusTest {
	
	OperationStatus os=new OperationStatus();
	Timestamp time=new Timestamp(10);
	Object obj=new Object();
	@Test
	public void test() {
		os.setFinishedAt(time);
		os.setOperateAt(time);
		os.setOperation("operation");
		os.setOperationContent("operationContent");
		os.setOperationId("operationId");
		os.setProgress("progress");
		os.setReason("reason");
		os.setResult("result");
		os.setServiceId("serviceId");
		os.setServiceName("serviceName");
		os.setUserId("userId");
		assertEquals(os.getFinishedAt(), time);
		assertEquals(os.getOperateAt(), time);
		assertEquals(os.getOperation(), "operation");
		assertEquals(os.getOperationContent(), "operationContent");
		assertEquals(os.getOperationId(), "operationId");
		assertEquals(os.getProgress(), "progress");
		assertEquals(os.getReason(), "reason");
		assertEquals(os.getResult(), "result");
		assertEquals(os.getServiceId(), "serviceId");
		assertEquals(os.getServiceName(), "serviceName");
		assertEquals(os.getUserId(), "userId");
		
		os.equals(obj);
		os.hashCode();
	}

}
