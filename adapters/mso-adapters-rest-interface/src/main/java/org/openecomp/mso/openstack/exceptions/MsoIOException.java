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
 * I/O exception
 */
public class MsoIOException extends MsoException
{

	/**
     * Serialization id.
     */
    private static final long serialVersionUID = 6752445132721635760L;

    /**
     * Basic constructor with message
     * @param message the error message
     */
	public MsoIOException (String message) {
		super(message);
		super.category = MsoExceptionCategory.IO;
	}
	
	/**
	 * Constructor to wrap a nested exception
	 * @param message the error message
	 * @param t the cause
	 */
	public MsoIOException (String message, Throwable t) {
		super (message, t);
		super.category = MsoExceptionCategory.IO;
	}
}
