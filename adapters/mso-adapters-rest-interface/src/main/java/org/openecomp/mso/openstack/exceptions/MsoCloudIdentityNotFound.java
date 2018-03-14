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
 * Signals that an attempt to find a specific mso cloud identity has failed.
 */
public class MsoCloudIdentityNotFound extends MsoException {

    private static final long serialVersionUID = 2583769056266415665L;

  
    /**
     * Default constructor (needed for BPEL/JAXB)
     */
    public MsoCloudIdentityNotFound () {
        super("Cloud Identity not found");
        super.category=MsoExceptionCategory.USERDATA;
    }  
  
    public MsoCloudIdentityNotFound (String cloudIdentity) {
        // Set the detailed error as the Exception 'message'
        super("Cloud Identity [" + cloudIdentity + "] not found");
        super.category=MsoExceptionCategory.USERDATA;
    }

    @Override
    public String toString () {
        return getMessage();
    }
}
