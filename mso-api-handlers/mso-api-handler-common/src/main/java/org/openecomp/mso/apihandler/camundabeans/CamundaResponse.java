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

package org.openecomp.mso.apihandler.camundabeans;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JavaBean JSON class for a "variables" which contains the xml payload that
 * will be passed to the Camunda process
 * 
 */

public class CamundaResponse {
	
	@JsonProperty("response")
	private String response;
	@JsonProperty("messageCode")
	private int messageCode;
	@JsonProperty("message")
	private String message;
	@JsonProperty("processInstanceID")
	private String processInstanceID;
	@JsonProperty("variables")
	private String variables;

	public String getProcessInstanceID() {
		return processInstanceID;
	}

	public void setProcessInstanceID(String processInstanceID) {
		this.processInstanceID = processInstanceID;
	}

	public String getVariables() {
		return variables;
	}

	public void setVariables(String variables) {
		this.variables = variables;
	}

	public CamundaResponse() {
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
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

	@Override
	public String toString() {
		return "CamundaResponse [response=" + response + ", messageCode="
				+ messageCode + ", message=" + message + "]";
	}

	
	
	
}
