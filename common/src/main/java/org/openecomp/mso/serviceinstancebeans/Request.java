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

package org.openecomp.mso.serviceinstancebeans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.commons.lang3.builder.ToStringBuilder;

//@JsonRootName(value = "request")
@JsonInclude(Include.NON_DEFAULT)
public class Request {

    protected String requestId;
    protected String startTime;
    protected String requestScope;
    protected String requestType;
    //protected String requestDetails;
    protected RequestDetails requestDetails;
    protected InstanceReferences instanceReferences;
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
	public InstanceReferences getInstanceReferences() {
		return instanceReferences;
	}
	public void setInstanceReferences(InstanceReferences instanceReferences) {
		this.instanceReferences = instanceReferences;
	}
	public RequestDetails getRequestDetails() {
		return requestDetails;
	}
	public void setRequestDetails(RequestDetails requestDetails) {
		this.requestDetails = requestDetails;
	}
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("requestId", requestId).append("startTime", startTime)
				.append("requestScope", requestScope).append("requestType", requestType)
				.append("requestDetails", requestDetails).append("instanceReferences", instanceReferences)
				.append("requestStatus", requestStatus).toString();
	}

}
