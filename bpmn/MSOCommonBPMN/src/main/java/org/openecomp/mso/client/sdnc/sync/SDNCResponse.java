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

package org.openecomp.mso.client.sdnc.sync;


import java.io.Serializable;

public class SDNCResponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private String reqId = null;
	private int respCode = 0;
	private String respMsg = null;
	private String sdncResp = null;

	public SDNCResponse(String reqId) {
		this.reqId = reqId;
	}
	public SDNCResponse(String reqId, int respCode, String respMsg) {
		this.reqId = reqId;
		this.respCode = respCode;
		this.respMsg = respMsg;
	}

	public String getReqId() {
		return reqId;
	}
	public void setReqId(String reqId) {
		this.reqId = reqId;
	}
	public int getRespCode() {
		return respCode;
	}
	public void setRespCode(int respCode) {
		this.respCode = respCode;
	}
	public String getRespMsg() {
		return respMsg;
	}
	public void setRespMsg(String respMsg) {
		this.respMsg = respMsg;
	}
	public String getSdncResp() {
		return sdncResp;
	}
	public void setSdncResp(String sdncResp) {
		this.sdncResp = sdncResp;
	}

	@Override
	public String toString() {
		return "SDNCResponse [reqId=" + reqId + ", respCode=" + respCode
				+ ", respMsg=" + respMsg + ", sdncResp=" + sdncResp + "]";
	}
}
