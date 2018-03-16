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
import org.openecomp.mso.bpmn.common.adapter.vnf.CreateVnfNotification.Outputs;
import org.openecomp.mso.bpmn.common.adapter.vnf.CreateVnfNotification.Outputs.Entry;

public class QueryVnfNotificationTest {
	private QueryVnfNotification qvn=new QueryVnfNotification();
	
	@Test
	public void test() {
		 Entry ent = new Entry();
		MsoExceptionCategory exception = MsoExceptionCategory.OPENSTACK;
		Outputs opt=new Outputs();
		VnfStatus vnf=VnfStatus.ACTIVE;
		qvn.setCompleted(true);
		qvn.setErrorMessage("error");
		qvn.setException(exception);
		qvn.setMessageId("id");
		qvn.setStatus(vnf);
		qvn.setVnfId("id");
		qvn.setVnfExists(true);
		ent.setKey("key");
		ent.setValue("value");
		assert(qvn.getErrorMessage().equals("error"));
		assert(qvn.getException()).equals(exception);
		assert(qvn.getMessageId()).equals("id");
		assert(qvn.getStatus()).equals(vnf);
		assert(qvn.getVnfId()).equals("id");
		assertTrue(qvn.isVnfExists());
		assertTrue(qvn.isCompleted());
		assert(opt.getEntry()!=null);
		assert(opt.toString()!=null);
		assert(ent.getValue()).equals("value");
		assert(ent.getKey()).equals("key");
		assert(ent.toString()!=null);
		assert(qvn.toString()!=null);		
	}
}
