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
package org.openecomp.mso.adapters.sdncrest;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

// NOTE: the JAXB (XML) annotations are required with JBoss AS7 and RESTEasy,
//       even though we are using JSON exclusively.  The @NoJackson annotation
//       is also required in this environment.

/**
 * SDNC adapter request for "agnostic" API services.
 * The target action is determined by a service type and an operation.
 */
@JsonRootName("SDNCServiceRequest")
@JsonInclude(Include.NON_NULL)
@XmlRootElement(name = "SDNCServiceRequest")
public class SDNCServiceRequest extends SDNCRequestCommon implements Serializable {
	private static final long serialVersionUID = 1L;

	// Request Information specified by SDNC "agnostic" API
	private RequestInformation requestInformation;

	// Service Information specified by: SDNC "agnostic" API
	private ServiceInformation serviceInformation;

	// The SDNC service type specified by SDNC "agnostic" API
	private String sdncService;

	// The SDNC operation specified by SDNC "agnostic" API
	private String sdncOperation;

	// The SDNC service data type specified by SDNC "agnostic" API
	private String sdncServiceDataType;

	// The SDNC service data specified by SDNC "agnostic" API
    private String sndcServiceData;

	public SDNCServiceRequest() {
	}

	public SDNCServiceRequest(String bpNotificationUrl, String bpTimeout,
			String sdncRequestId, String sdncService, String sdncOperation,
			RequestInformation requestInformation,
			ServiceInformation serviceInformation, String sdncServiceDataType,
			String sndcServiceData) {
		super(bpNotificationUrl, bpTimeout, sdncRequestId);
		this.requestInformation = requestInformation;
		this.serviceInformation = serviceInformation;
		this.sdncService = sdncService;
		this.sdncOperation = sdncOperation;
		this.sdncServiceDataType = sdncServiceDataType;
		this.sndcServiceData = sndcServiceData;
	}

	@JsonProperty("requestInformation")
	@XmlElement(name = "requestInformation")
	public RequestInformation getRequestInformation() {
		return requestInformation;
	}

	@JsonProperty("requestInformation")
	public void setRequestInformation(RequestInformation requestInformation) {
		this.requestInformation = requestInformation;
	}

	@JsonProperty("serviceInformation")
	@XmlElement(name = "serviceInformation")
	public ServiceInformation getServiceInformation() {
		return serviceInformation;
	}

	@JsonProperty("serviceInformation")
	public void setServiceInformation(ServiceInformation serviceInformation) {
		this.serviceInformation = serviceInformation;
	}

	@JsonProperty("sdncService")
	@XmlElement(name = "sdncService")
	public String getSDNCService() {
		return sdncService;
	}

	@JsonProperty("sdncService")
	public void setSDNCService(String sdncService) {
		this.sdncService = sdncService;
	}

	@JsonProperty("sdncOperation")
	@XmlElement(name = "sdncOperation")
	public String getSDNCOperation() {
		return sdncOperation;
	}

	@JsonProperty("sdncOperation")
	public void setSDNCOperation(String sdncOperation) {
		this.sdncOperation = sdncOperation;
	}

	@JsonProperty("sdncServiceDataType")
	@XmlElement(name = "sdncServiceDataType")
	public String getSDNCServiceDataType() {
		return sdncServiceDataType;
	}

	@JsonProperty("sdncServiceDataType")
	public void setSDNCServiceDataType(String sdncServiceDataType) {
		this.sdncServiceDataType = sdncServiceDataType;
	}

	@JsonProperty("sdncServiceData")
	@XmlElement(name = "sdncServiceData")
	public String getSDNCServiceData() {
		return sndcServiceData;
	}

	@JsonProperty("sdncServiceData")
	public void setSDNCServiceData(String sndcServiceData) {
		this.sndcServiceData = sndcServiceData;
	}
}