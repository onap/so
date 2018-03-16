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

public class CreateVnfNotificationTest {

	@Test
	public void test() {
		CreateVnfNotification cvn=new CreateVnfNotification();
		 Entry ent = new Entry();
		MsoExceptionCategory exception = MsoExceptionCategory.OPENSTACK;
		Outputs value=new Outputs();
		VnfRollback vnf=new VnfRollback();
		vnf.setCloudSiteId("cloud");
		cvn.setCompleted(true);
		cvn.setErrorMessage("emsg");
		cvn.setException(exception);
		cvn.setMessageId("id");
		cvn.setOutputs(value);
		ent.setKey("key");
		ent.setValue("value");
		cvn.setRollback(vnf);
		cvn.setVnfId("vnf");
	assertTrue(cvn.isCompleted());
	assert(cvn.getErrorMessage().equals("emsg"));
	assert(cvn.getException()).equals(exception);
	assert(cvn.getMessageId()).equals("id");
	assert(cvn.getRollback()).equals(vnf);
	assert(cvn.getOutputs()).equals(value);
	assert(cvn.getVnfId()).equals("vnf");
	assert(ent.getKey()).equals("key");
	assert(ent.getValue()).equals("value");
	assert(ent.toString()!=null);
	assert(cvn.toString()!=null);
	assert(vnf.getCloudSiteId().equals("cloud"));
    assert(value.getEntry()!=null);	
	}
}
