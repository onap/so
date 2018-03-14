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
package org.openecomp.mso.client.sdnc.beans;

public class SDNCRequest {
	private String requestId;
	private String svcInstanceId;
	private SDNCSvcAction svcAction;
	private SDNCSvcOperation svcOperation;
	private String callbackUrl;
	private String msoAction;
	private String requestData;
	
	public SDNCRequest(String requestId, String svcInstanceId, SDNCSvcAction svcAction, SDNCSvcOperation svcOperation,
			String callbackUrl, String msoAction, String requestData) {
		this.requestId = requestId;
		this.svcInstanceId = svcInstanceId;
		this.svcAction = svcAction;
		this.svcOperation = svcOperation;
		this.callbackUrl = callbackUrl;
		this.msoAction = msoAction;
		this.requestData = requestData;
	}
	public SDNCRequest(){
		
	}
	
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getSvcInstanceId() {
		return svcInstanceId;
	}
	public void setSvcInstanceId(String svcInstanceId) {
		this.svcInstanceId = svcInstanceId;
	}
	public SDNCSvcAction getSvcAction() {
		return svcAction;
	}
	public void setSvcAction(SDNCSvcAction svcAction) {
		this.svcAction = svcAction;
	}
	public SDNCSvcOperation getSvcOperation() {
		return svcOperation;
	}
	public void setSvcOperation(SDNCSvcOperation svcOperation) {
		this.svcOperation = svcOperation;
	}
	public String getCallbackUrl() {
		return callbackUrl;
	}
	public void setCallbackUrl(String callbackUrl) {
		this.callbackUrl = callbackUrl;
	}
	public String getMsoAction() {
		return msoAction;
	}
	public void setMsoAction(String msoAction) {
		this.msoAction = msoAction;
	}
	
	public String getRequestData() {
		return requestData;
	}
	public void setRequestData(String requestData) {
		this.requestData = requestData;
	}
	@Override
	public String toString() {
		return "SDNCRequest [requestId=" + requestId + ", svcInstanceId=" + svcInstanceId + ", svcAction=" + svcAction
				+ ", svcOperation=" + svcOperation + ", callbackUrl=" + callbackUrl + ", msoAction=" + msoAction
				+ ", requestData=" + requestData + "]";
	}
	
}
