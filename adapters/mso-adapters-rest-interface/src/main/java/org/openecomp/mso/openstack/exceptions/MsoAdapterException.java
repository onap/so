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
 * General MSO Exception class for any non-specific errors.
 * 
 *
 */
public class MsoAdapterException extends MsoException
{
	private static final long serialVersionUID = 1L;
	
	// Constructor to create a new MsoException instance
	public MsoAdapterException (String message) {
		super(message);
		super.category = MsoExceptionCategory.INTERNAL;
	}

	// Constructor to wrap a nested exception
	public MsoAdapterException (String message, Throwable t) {
		super(message, t);
		super.category = MsoExceptionCategory.INTERNAL;
	}
}
