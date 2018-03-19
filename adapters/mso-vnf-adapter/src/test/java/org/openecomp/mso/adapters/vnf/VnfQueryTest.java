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

package org.openecomp.mso.adapters.vnf.test;


import java.util.Map;

import javax.xml.ws.Holder;

import org.openecomp.mso.adapters.vnf.MsoVnfAdapter;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapterImpl;
import org.openecomp.mso.openstack.beans.VnfStatus;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;

public class VnfQueryTest {
	public final static void main (String args[])
	{
		MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl();
		log ("Got a VnfAdapter");
		
		String cloudId = "MT";
		String tenantId = "MSO_Test";
		String vnfName = "VNF_TEST1";
		Holder<Boolean> vnfExists = new Holder<>();
		Holder<String> vnfId = new Holder<>();
		Holder<VnfStatus> status = new Holder<>();
		Holder<Map<String,String>> outputs = new Holder<>();
		
		try {
			vnfAdapter.queryVnf(cloudId, tenantId, vnfName, null,
						vnfExists, vnfId, status, outputs);
		} catch (VnfException e) {
			log ("Got an Exception: " + e);
		}
		
		if (! vnfExists.value){
			log ("VNF Not Found");
		} else {
			log ("Found VNF, ID = " + vnfId.value + ", status=" + status.value);
		}
	}
	
	private static void log (String msg) {
		System.out.println (msg);
	}
}
