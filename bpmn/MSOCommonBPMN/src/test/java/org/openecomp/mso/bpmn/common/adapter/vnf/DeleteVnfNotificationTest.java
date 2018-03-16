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

import static org.junit.Assert.*;

import org.junit.Test;

public class DeleteVnfNotificationTest {

	@Test
	public void test() {
		DeleteVnfNotification dvn=new DeleteVnfNotification();
		MsoExceptionCategory exception = MsoExceptionCategory.OPENSTACK;
		dvn.setCompleted(true);
		dvn.setErrorMessage("msg");
		dvn.setMessageId("id");
		dvn.setException(exception);
		assert(dvn.getErrorMessage().equals("msg"));
		assert(dvn.getMessageId().equals("id"));
		assert(dvn.getException().equals(exception));
		assert(dvn.toString()!=null);
		assertTrue(dvn.isCompleted());
	}
}
