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

package org.openecomp.mso.cloudify.exceptions;

import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;

/**
 * OpenStack exception.
 */
public class MsoCloudifyException extends MsoException
{
	
	/**
     * Serialization id.
     */
    private static final long serialVersionUID = 3313636124141766495L;
    
    private int statusCode;
	private String statusMessage;
	private String errorDetail;
	private boolean pendingWorkflow;

	/**
	 * Constructor to create a new MsoOpenstackException instance
	 * @param code the error code
	 * @param message the error message
	 * @param detail error details
	 */
	public MsoCloudifyException (int code, String message, String detail) {
		// Set the detailed error as the Exception 'message'
		super(detail);
		super.category = MsoExceptionCategory.OPENSTACK;
		
		this.statusCode = code;
		this.statusMessage = message;
		this.errorDetail = detail;
		this.pendingWorkflow = false;
	}
	
	/**
	 * Constructor to propagate the caught exception (mostly for stack trace)
     * @param code the error code
     * @param message the error message
     * @param detail error details
	 * @param e the cause
	 */
	public MsoCloudifyException (int code, String message, String detail, Exception e) {
		// Set the detailed error as the Exception 'message'
		super(detail, e);
		super.category = MsoExceptionCategory.OPENSTACK;
		
		this.statusCode = code;
		this.statusMessage = message;
		this.errorDetail = detail;
		this.pendingWorkflow = false;
	}
	
	public void setPendingWorkflow (boolean pendingWorkflow) {
		this.pendingWorkflow = pendingWorkflow;
	}
	
	@Override
	public String toString () {
		String error = "" + statusCode + " " + statusMessage + ": " + errorDetail + (pendingWorkflow ? " [workflow pending]" : "");
		return error;
	}
}
