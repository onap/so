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

package org.openecomp.mso.bpmn.common.workflow.service;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * Workflow context object used to send timeout response, if workflow instance does not write the response in time
 */
public class WorkflowContext implements Delayed {
	private final String processKey;
	private final String requestId;
	private final AsynchronousResponse asynchronousResponse;
	private final long startTime;
	private final long timeout;
	
	public WorkflowContext(String processKey, String requestId,
			AsynchronousResponse asynchronousResponse, long timeout) {
		this.processKey = processKey;
		this.requestId = requestId;
		this.asynchronousResponse = asynchronousResponse;
		this.timeout = timeout;
		this.startTime = System.currentTimeMillis();
	}
	
	public String getRequestId() {
		return requestId;
	}

	public String getProcessKey() {
		return processKey;
	}

	public AsynchronousResponse getAsynchronousResponse() {
		return asynchronousResponse;
	}

	public long getTimeout() {
		return timeout;
	}

	public long getStartTime() {
		return startTime;
	}

	/**
	 * Required implementation by Delay queue
	 * Returns the elapsed time for this context
	 */
	@Override
	public long getDelay(TimeUnit unit) {
		// 0 or negative means this object is considered to be expired
		return unit.convert(startTime + timeout - System.currentTimeMillis(), unit);
	}

	/**
	 * Required implementation by Delay queue
	 * Compares the object to determine whether the object can be marked as expired
	 */
	@Override
	public int compareTo(Delayed object) {
		WorkflowContext that = (WorkflowContext) object;
		long thisEndTime = startTime + timeout;
		long thatEndTime = that.startTime + that.timeout;

		if (thisEndTime < thatEndTime) {
			return -1;
		} else if (thisEndTime > thatEndTime) {
			return 1;
		} else {
			return 0;
		}
	}
}
