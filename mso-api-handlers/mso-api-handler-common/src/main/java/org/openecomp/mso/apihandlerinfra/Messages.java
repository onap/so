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

package org.openecomp.mso.apihandlerinfra;


import java.util.HashMap;
import java.util.Map;

import org.openecomp.mso.apihandler.common.ErrorNumbers;

public class Messages {

	protected static final Map<String,String> errors = new HashMap<>();
	static {
		errors.put(ErrorNumbers.REQUEST_FAILED_SCHEMA_VALIDATION + "_service", "Service request FAILED schema validation. %s");
		errors.put(ErrorNumbers.REQUEST_FAILED_SCHEMA_VALIDATION + "_feature", "Feature request FAILED schema validation. %s");
		errors.put(ErrorNumbers.RECIPE_DOES_NOT_EXIST, "Recipe for %s-type and action specified does not exist in catalog %s");
		errors.put(ErrorNumbers.SERVICE_PARAMETERS_FAILED_SCHEMA_VALIDATION, "Service specific parameters passed in request FAILED schema validation. %s");
		
		errors.put(ErrorNumbers.LOCKED_CREATE_ON_THE_SAME_VNF_NAME_IN_PROGRESS, "%s-name (%s) is locked (status = %s) because already working on a CREATE request with same %s-name.");
		errors.put(ErrorNumbers.LOCKED_SAME_ACTION_AND_VNF_ID, "%s-id (%s) is locked (status = %s) because already working on a request with same action (%s) for this %s-id.");
		errors.put(ErrorNumbers.REQUEST_TIMED_OUT, "Service request timed out before completing");
		errors.put(ErrorNumbers.ERROR_FROM_BPEL, "BPEL returned an error: %s");
		errors.put(ErrorNumbers.NO_COMMUNICATION_TO_BPEL, "Could not communicate with BPEL %s");
		errors.put(ErrorNumbers.NO_RESPONSE_FROM_BPEL, "No response from BPEL %s");
		errors.put(ErrorNumbers.COULD_NOT_WRITE_TO_REQUESTS_DB, "Could not insert or update record in MSO_REQUESTS DB %s");
		errors.put(ErrorNumbers.NO_COMMUNICATION_TO_REQUESTS_DB, "Could not communicate with MSO_REQUESTS DB %s");
		errors.put(ErrorNumbers.NO_COMMUNICATION_TO_CATALOG_DB, "Could not communicate with MSO_CATALOG DB %s");
		errors.put(ErrorNumbers.ERROR_FROM_CATALOG_DB, "Received error from MSO_CATALOG DB %s");		
	}
	
	private Messages(){
	}	
	
	public static Map<String,String> getErrors() {
		return errors;
	}
}
