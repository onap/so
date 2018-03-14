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

import org.openecomp.mso.cloudify.v3.model.Execution;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoExceptionCategory;

/**
 * MSO Exception when a Cloudify workflow execution times out waiting for completion.
 * Exception includes the last known state of the workflow execution.
 */
public class MsoCloudifyTimeout extends MsoException
{
	
	/**
     * Serialization id.
     */
    private static final long serialVersionUID = 3313636124141766495L;
    
	private Execution execution;

	/**
	 * Constructor to create a new MsoOpenstackException instance
	 * @param code the error code
	 * @param message the error message
	 * @param detail error details
	 */
	public MsoCloudifyTimeout (Execution execution) {
		// Set the detailed error as the Exception 'message'
		super("Cloudify Workflow Timeout for workflow " + execution.getWorkflowId() + " on deployment " + execution.getDeploymentId());
		super.category = MsoExceptionCategory.OPENSTACK;
		
		this.execution = execution;
	}
		
	public Execution getExecution() {
		return this.execution;
	}
	
	@Override
	public String toString () {
		String error = "Workflow timeout: workflow=" + execution.getWorkflowId() + ",deployment=" + execution.getDeploymentId();
		return error;
	}
}
