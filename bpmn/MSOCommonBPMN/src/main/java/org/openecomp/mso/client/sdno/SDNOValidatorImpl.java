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

package org.openecomp.mso.client.sdno;

public class SDNOValidatorImpl implements SDNOValidator {

	private final static String aafUserName = "something";
	private final static String clientName = "MSO";
	private final static String healthDiagnosticPath = "body.output.response-healthdiagnostic";
	private final static String producerFilePath = "";
	private String uuid;
	private boolean continuePolling = true;
	@Override
	public void healthDiagnostic(String vnfName, String uuid) {
		//Query A&AI data
		// setup SDNO Entity
		//Call SDNO for Health Diagnostic
		//create producer file for MRClient https://wiki.web.att.com/display/MessageRouter/DMaaP_MR_JavaReferenceClient
		//  final MRBatchingPublisher pub = MRClientFactory.createBatchingPublisher(producerFilePath);
		//	pub.send("Mypartitionkey",JSON.toString(object));
		//create consumer file for MRClient https://wiki.web.att.com/display/MessageRouter/DMaaP_MR_JavaReferenceClient
		//check for error in subscription feed filter via jsonpath 
		//block and continue to poll waiting for response
	}

}
