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

/**
 * Reports an error with a Cloudify Workflow execution.
 * @author JC1348
 *
 */
public class MsoCloudifyWorkflowException extends MsoCloudifyException {

	private static final long serialVersionUID = 1L;

	private String workflowStatus;
	private boolean workflowStillRunning = false;
	
	// Constructor to create a new MsoCloudifyException instance
	public MsoCloudifyWorkflowException (String message, String deploymentId, String workflowId, String workflowStatus)
	{
		super(0, "Workflow Exception", "Workflow " + workflowId + " failed on deployment " + deploymentId + ": " + message);
		this.workflowStatus = workflowStatus;
		if (workflowStatus.equals("pending") || workflowStatus.equals("started") ||
			workflowStatus.equals("cancelling") || workflowStatus.equals("force_cancelling"))
		{
			workflowStillRunning = true;
		}
	}
		
	public String getWorkflowStatus() {
		return workflowStatus;
	}
	
	public boolean isWorkflowStillRunning () {
		return workflowStillRunning;
	}
}
