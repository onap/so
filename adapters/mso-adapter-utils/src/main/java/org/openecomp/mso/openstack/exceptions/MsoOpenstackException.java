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

package org.openecomp.mso.openstack.exceptions;


/**
 * OpenStack exception.
 */
public class MsoOpenstackException extends MsoException
{
	
	/**
     * Serialization id.
     */
    private static final long serialVersionUID = 3313636124141766495L;
    
    private int statusCode;
	private String statusMessage;
	private String errorDetail;

	/**
	 * Constructor to create a new MsoOpenstackException instance
	 * @param code the error code
	 * @param message the error message
	 * @param detail error details
	 */
	public MsoOpenstackException (int code, String message, String detail) {
		// Set the detailed error as the Exception 'message'
		super(detail);
		super.category = MsoExceptionCategory.OPENSTACK;
		
		this.statusCode = code;
		this.statusMessage = message;
		this.errorDetail = detail;
	}
	
	/**
	 * Constructor to propagate the caught exception (mostly for stack trace)
     * @param code the error code
     * @param message the error message
     * @param detail error details
	 * @param e the cause
	 */
	public MsoOpenstackException (int code, String message, String detail, Exception e) {
		// Set the detailed error as the Exception 'message'
		super(detail, e);
		super.category = MsoExceptionCategory.OPENSTACK;
		
		this.statusCode = code;
		this.statusMessage = message;
		this.errorDetail = detail;
	}
	
	@Override
	public String toString () {
		StringBuilder error = new StringBuilder();
		error.append("");
		error.append(statusCode);
		error.append(" ");
		error.append(statusMessage);
		error.append(": ");
		error.append(errorDetail);
		return error.toString();
	}
}
