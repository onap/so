/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.adapters.vdu;

import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;

/**
 * OpenStack exception.
 */
public class VduException extends MsoException
{
	
	/**
     * Serialization id.
     */
    private static final long serialVersionUID = 3313636124141766495L;
    
	/**
	 * Constructor to create a new VduException instance
	 * @param detail error details
	 */
	public VduException (String detail) {
		// Set the detailed error as the Exception 'message'
		super(detail);
		// TODO:  Need a more generic category than OPENSTACK
		super.category = MsoExceptionCategory.OPENSTACK;
	}
	
	/**
	 * Constructor to create a new VduException instance
	 * @param detail error details
	 * @param e the cause
	 */
	public VduException (String detail, Exception e) {
		// Set the detailed error as the Exception 'message'
		super(detail, e);
		// TODO:  Need a more generic category than OPENSTACK
		super.category = MsoExceptionCategory.OPENSTACK;
	}

}