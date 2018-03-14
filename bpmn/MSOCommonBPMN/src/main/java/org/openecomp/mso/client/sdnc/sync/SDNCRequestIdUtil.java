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

package org.openecomp.mso.client.sdnc.sync;


public class SDNCRequestIdUtil {
	// Add private constructor to prevent instance creation.
	private SDNCRequestIdUtil () {}

    public static String getSDNCOriginalRequestId (String newRequestId) {
     	
    	// Camunda scripts will add postfix, such as -1, -2, on the original requestID, to make sure requestID is unique while sending request to SDNC 
     	// In order to use the unique requestID in logging, need to remove the postfix added by the Camunda scripts
     	// Verify whether the requestId is a valid UUID with postfix (-1, -2). If yes, it should contain 5 times char '-', since valid UUID contains 4 times '-'
    	// If the requestId is not a valid UUID with postfix, we do nothing
		if (newRequestId.split("-").length == 6) {
			return newRequestId.substring(0, newRequestId.lastIndexOf('-'));
		}
		return newRequestId;
    }
}
