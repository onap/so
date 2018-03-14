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



import org.openecomp.mso.apihandler.common.CommonConstants;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * JavaBean JSON class for a "variables" which contains the xml payload that
 * will be passed to the Camunda process
 * 
 */
@JsonPropertyOrder({ CommonConstants.CAMUNDA_SERVICE_INPUT, CommonConstants.CAMUNDA_HOST, 
	CommonConstants.SCHEMA_VERSION_HEADER, CommonConstants.REQUEST_ID_HEADER, CommonConstants.SERVICE_INSTANCE_ID_HEADER,
	CommonConstants. REQUEST_TIMEOUT_HEADER, CommonConstants.CAMUNDA_SERVICE_INPUT})
@JsonRootName(CommonConstants.CAMUNDA_ROOT_INPUT)
public class CamundaRequest {

	@JsonProperty(CommonConstants.CAMUNDA_SERVICE_INPUT)
	private CamundaInput serviceInput;
	
	@JsonProperty(CommonConstants.CAMUNDA_HOST)
	private CamundaInput host;
	
	@JsonProperty(CommonConstants.SCHEMA_VERSION_HEADER)
	private CamundaInput schema;
	
	@JsonProperty(CommonConstants.REQUEST_ID_HEADER)
	private CamundaInput reqid;
	
	@JsonProperty(CommonConstants.SERVICE_INSTANCE_ID_HEADER)
	private CamundaInput svcid;
	
	@JsonProperty(CommonConstants.REQUEST_TIMEOUT_HEADER)
	private CamundaInput timeout;
	
	public CamundaRequest() {
	}
	
	@JsonProperty(CommonConstants.CAMUNDA_SERVICE_INPUT)
	public CamundaInput getServiceInput() {
		return serviceInput;
	}

	@JsonProperty(CommonConstants.CAMUNDA_SERVICE_INPUT)
	public void setServiceInput(CamundaInput serviceInput) {
		this.serviceInput = serviceInput;
	}
	
	@JsonProperty(CommonConstants.CAMUNDA_HOST)
	public CamundaInput getHost() {
		return host;
	}

	@JsonProperty(CommonConstants.CAMUNDA_HOST)
	public void setHost(CamundaInput host) {
		this.host = host;
	}

	@JsonProperty(CommonConstants.SCHEMA_VERSION_HEADER)
	public CamundaInput getSchema() {
		return schema;
	}

	@JsonProperty(CommonConstants.SCHEMA_VERSION_HEADER)
	public void setSchema(CamundaInput schema) {
		this.schema = schema;
	}

	@JsonProperty(CommonConstants.REQUEST_ID_HEADER)
	public CamundaInput getReqid() {
		return reqid;
	}

	@JsonProperty(CommonConstants.REQUEST_ID_HEADER)
	public void setReqid(CamundaInput reqid) {
		this.reqid = reqid;
	}

	@JsonProperty(CommonConstants.SERVICE_INSTANCE_ID_HEADER)
	public CamundaInput getSvcid() {
		return svcid;
	}

	@JsonProperty(CommonConstants.SERVICE_INSTANCE_ID_HEADER)
	public void setSvcid(CamundaInput svcid) {
		this.svcid = svcid;
	}
	

	@JsonProperty(CommonConstants.REQUEST_TIMEOUT_HEADER)
	public CamundaInput getTimeout() {
		return timeout;
	}

	@JsonProperty(CommonConstants.REQUEST_TIMEOUT_HEADER)
	public void setTimeout(CamundaInput timeout) {
		this.timeout = timeout;
	}

		
	
	@Override
	public String toString() {
		return "CamundaRequest [serviceInput=" + serviceInput + ", host="
				+ host + ", schema=" + schema + ", reqid=" + reqid + ", svcid="
				+ svcid + ", timeout=" + timeout + "]";
	}	
	
}
