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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A synchronous response from a workflow.
 */
public class WorkflowResponse {

	@JsonProperty("processInstanceId")
	private String processInstanceId;

	@JsonProperty("messageCode")
	private int messageCode;

	@JsonProperty("message")
	private String message;
	
	@JsonProperty("variables")
	private Map<String,String> variables;

	@JsonProperty("content")
	private String content;

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public int getMessageCode() {
		return messageCode;
	}

	public void setMessageCode(int messageCode) {
		this.messageCode = messageCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String,String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String,String> variables) {
		this.variables = variables;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
			+ "processInstanceId=" + processInstanceId
			+ ",messageCode=" + messageCode
			+ ",message=" + message
			+ ",variables=" + variables
			+ ",content=" + content
			+ "]";
	}
}