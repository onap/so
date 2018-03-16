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

package org.openecomp.mso.bpmn.common.adapter.vnf;

import static org.mockito.Mockito.mock;
import org.junit.Test;
import org.openecomp.mso.bpmn.common.adapter.vnf.CreateVnfNotification.Outputs;

public class ObjectFactoryTest {
	private ObjectFactory ofa=new ObjectFactory();
	
	@Test
	public void test() {		
		CreateVnfNotification cvn=mock( CreateVnfNotification.class);
		UpdateVnfNotification uvn=mock (UpdateVnfNotification.class);
		 QueryVnfNotification qn=mock(QueryVnfNotification.class);
		 DeleteVnfNotification dvn=mock( DeleteVnfNotification.class);
		 RollbackVnfNotification rvn=mock( RollbackVnfNotification.class);
		 MsoRequest mr=mock( MsoRequest.class);
		 Outputs opt=mock(Outputs.class);
		ofa.createCreateVnfNotification();
		ofa.createCreateVnfNotificationOutputs();
		ofa.createDeleteVnfNotification();
		ofa.createQueryVnfNotification();
		ofa.createUpdateVnfNotification();
		ofa.createMsoRequest();
		ofa.createRollbackVnfNotification();
		ofa.createUpdateVnfNotificationOutputs();
		ofa.createQueryVnfNotificationOutputs();
		ofa.createVnfRollback();
		ofa.createUpdateVnfNotificationOutputsEntry();
		ofa.createQueryVnfNotificationOutputsEntry();
		ofa.createCreateVnfNotificationOutputsEntry();
		ofa.createCreateVnfNotification(cvn);
		ofa.createDeleteVnfNotification(dvn);
		ofa.createQueryVnfNotification(qn);
		ofa.createRollbackVnfNotification(rvn);
		ofa.createUpdateVnfNotification(uvn); 
	}
}
