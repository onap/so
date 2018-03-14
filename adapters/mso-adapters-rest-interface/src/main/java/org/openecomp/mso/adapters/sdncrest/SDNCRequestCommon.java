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

import java.io.IOException;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import org.openecomp.mso.logger.MsoLogger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Base class for all SDNC adapter requests.
 */
public abstract class SDNCRequestCommon implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.RA);

	// Endpoint on which BPMN can receive notifications from the SDNC adapter.
	private String bpNotificationUrl;

	// BPMN flow timeout value in ISO 8601 format, e.g. PT5M.
	// Not currently used by the SDNC adapter.
	private String bpTimeout;

	// Identifies the MSO transaction with SDNC.
	// Maps to sdnc-request-header/requestId in the SDNC request.
	private String sdncRequestId;

	public SDNCRequestCommon(String sdncRequestId, String bpNotificationUrl,
			String bpTimeout) {
		this.sdncRequestId = sdncRequestId;
		this.bpNotificationUrl = bpNotificationUrl;
		this.bpTimeout = bpTimeout;
	}

	public SDNCRequestCommon() {
	}

	@JsonProperty("bpNotificationUrl")
	@XmlElement(name = "bpNotificationUrl")
	public String getBPNotificationUrl() {
		return bpNotificationUrl;
	}

	@JsonProperty("bpNotificationUrl")
	public void setBPNotificationUrl(String bpNotificationUrl) {
		this.bpNotificationUrl = bpNotificationUrl;
	}

	@JsonProperty("bpTimeout")
	@XmlElement(name = "bpTimeout")
	public String getBPTimeout() {
		return bpTimeout;
	}

	@JsonProperty("bpTimeout")
	public void setBPTimeout(String bpTimeout) {
		this.bpTimeout = bpTimeout;
	}

	@JsonProperty("sdncRequestId")
	@XmlElement(name = "sdncRequestId")
	public String getSDNCRequestId() {
		return sdncRequestId;
	}

	@JsonProperty("sdncRequestId")
	public void setSDNCRequestId(String sdncRequestId) {
		this.sdncRequestId = sdncRequestId;
	}

	@JsonIgnore
	public boolean isSynchronous() {
		return bpNotificationUrl == null || bpNotificationUrl.isEmpty();
	}

	public String toJson() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
			mapper.setSerializationInclusion(Include.NON_NULL);
			return mapper.writeValueAsString(this);
		} catch (IOException e) {
		    LOGGER.debug("Exception:", e);
			throw new UnsupportedOperationException("Cannot convert "
				+ getClass().getSimpleName() + " to JSON", e);
		}
	}
}