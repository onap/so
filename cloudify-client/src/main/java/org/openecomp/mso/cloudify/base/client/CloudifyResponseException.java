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

package org.openecomp.mso.cloudify.base.client;

import org.openecomp.mso.cloudify.v3.model.CloudifyError;

public class CloudifyResponseException extends CloudifyBaseException {

	private static final long serialVersionUID = 7294957362769575271L;

	protected String message;
	protected int status;
	
	// Make the response available for exception handling (includes body)
	protected CloudifyResponse response;

	public CloudifyResponseException(String message, int status) {
		this.message = message;
		this.status = status;
		this.response = null;
	}

	// Include the response message itself.  The body is a CloudifyError JSON structure.
	public CloudifyResponseException(String message, int status, CloudifyResponse response) {
		CloudifyError error = response.getErrorEntity(CloudifyError.class);
		this.message = message + ": " + error.getErrorCode();
		this.status = status;
		this.response = response;
	}

	public String getMessage() {
		return message;
	}

	public int getStatus() {
		return status;
	}

	public CloudifyResponse getResponse() {
		return response;
	}

}
