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

package org.openecomp.mso.apihandlerinfra.tenantisolationbeans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "tenantIsolationRequest")
@JsonInclude(Include.NON_DEFAULT)
public class TenantIsolationRequest implements Serializable {

	private static final long serialVersionUID = -210322298981798607L;
	@JsonProperty("requestId")
    protected String requestId;
	@JsonProperty("startTime")	
    protected String startTime;
	@JsonProperty("requestScope")	
    protected String requestScope;
	@JsonProperty("requestType")	
    protected String requestType;
	@JsonProperty("requestDetails")	
    protected RequestDetails requestDetails;
	@JsonProperty("requestStatus")
    protected RequestStatus requestStatus;
    
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getRequestScope() {
		return requestScope;
	}
	public void setRequestScope(String requestScope) {
		this.requestScope = requestScope;
	}
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public RequestStatus getRequestStatus() {
		return requestStatus;
	}
	public void setRequestStatus(RequestStatus requestStatus) {
		this.requestStatus = requestStatus;
	}

	public RequestDetails getRequestDetails() {
		return requestDetails;
	}
	public void setRequestDetails(RequestDetails requestDetails) {
		this.requestDetails = requestDetails;
	}

	@Override
	public String toString() {
		return "Request [requestId=" + requestId + 
				      ", startTime=" + startTime + 
				      ", requestType=" + requestType +
				      ", requestDetails=" + requestDetails.toString() +				      
				      ", requestStatus=" + requestStatus.toString() + "]";
	}	
	
}
