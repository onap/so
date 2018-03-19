/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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



import org.openecomp.mso.adapters.vnf.MsoVnfAdapter;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapterImpl;
import org.openecomp.mso.adapters.vnf.exceptions.VnfException;

public class VnfDeleteTest {
	public final static void main (String args[])
	{
		MsoVnfAdapter vnfAdapter = new MsoVnfAdapterImpl();
		log ("Got a VnfAdapter");
		
		// Web Service Inputs
		String cloudId = "MT";
		String tenantName = "MSO_Test";
		String vnfName = "AdapterTest";

		try {
			vnfAdapter.deleteVnf(cloudId, tenantName, vnfName, null);
		} catch (VnfException e) {
			log ("Got an Exception: " + e);
		}
		
		log ("Deleted VNF");
	}
	
	private static void log (String msg) {
		System.out.println (msg);
	}
}
