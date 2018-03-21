/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.adapters.vnf;


import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.Holder;

import org.openecomp.mso.adapters.vnf.MsoVnfAdapter;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapterImpl;
import org.openecomp.mso.openstack.beans.VnfRollback;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;

public class VnfCreateTest {
	public final static void main (String args[])
	{
		MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl();
		log ("Got a VnfAdapter");
		
		// Web Service Inputs
		String cloudId = "MT";
		String tenantName = "John_Test";
		String vnfType = "ApacheDemo";
		String vnfName = "AdapterTest";
		Map<String,String> inputs = new HashMap<>();
		inputs.put("vnf_id", "abc");
		inputs.put("extra", "whocares");
		inputs.put("private_subnet_gateway", "10.4.1.1");
		inputs.put("private_subnet_cidr", "10.4.1.0/29");
		
		// Web Service Outputs
		Holder<String> vnfId = new Holder<>();
		Holder<Map<String,String>> outputs = new Holder<>();
		Holder<VnfRollback> vnfRollback = new Holder<>();

		try {
			vnfAdapter.createVnf(cloudId, tenantName, vnfType,null, vnfName, null, null, inputs, false, true, null,
					vnfId, outputs, vnfRollback);
		} catch (VnfException e) {
			log ("Got a Create Exception: " + e);
			System.exit(1);
		}
		
		log ("Created VNF, ID = " + vnfId.value);
		for (String key : outputs.value.keySet()) {
			log ("   " + key + " = " + outputs.value.get(key));
		}
		if (vnfRollback.value != null)
			log (vnfRollback.value.toString());
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {}
		
		log ("Rolling Back VNF");
		try {
			vnfAdapter.rollbackVnf(vnfRollback.value);
		} catch (VnfException e) {
			log ("Got a Rollback Exception: " + e);
		}
		log ("VNF Rolled Back");
	}
	
	private static void log (String msg) {
		System.out.println (msg);
	}
}
