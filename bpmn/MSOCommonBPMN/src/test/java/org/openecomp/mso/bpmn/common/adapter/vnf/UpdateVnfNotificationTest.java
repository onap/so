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
import org.openecomp.mso.bpmn.common.adapter.vnf.UpdateVnfNotification.Outputs;
import org.openecomp.mso.bpmn.common.adapter.vnf.UpdateVnfNotification.Outputs.Entry;

public class UpdateVnfNotificationTest {
	private UpdateVnfNotification updatevnf = new UpdateVnfNotification();
	MsoExceptionCategory mso;
	Outputs value= new Outputs();
	VnfRollback roll = new VnfRollback();
	private Entry entry = new Entry();
	
	@Test
	public void testUpdateVnfNotification() {
		updatevnf.setMessageId("messageId");
		updatevnf.setCompleted(true);
		updatevnf.setException(mso);
		updatevnf.setErrorMessage("errorMessage");
		updatevnf.setOutputs(value);
		updatevnf.setRollback(roll);
		entry.setKey("key");
		entry.setValue("value");
		assertEquals(updatevnf.getMessageId(), "messageId");
		assertEquals(updatevnf.isCompleted(), true);
		assertEquals(updatevnf.getException(), mso);
		assertEquals(updatevnf.getErrorMessage(), "errorMessage");
		assertEquals(updatevnf.getOutputs(), value);
		assertEquals(updatevnf.getRollback(), roll);
		assertEquals(entry.getKey(), "key");
		assertEquals(entry.getValue(), "value");	
	}
	@Test
	public void testtoString() {
		assert(updatevnf.toString()!= null);
		assert(entry.toString()!= null);
	}
}
