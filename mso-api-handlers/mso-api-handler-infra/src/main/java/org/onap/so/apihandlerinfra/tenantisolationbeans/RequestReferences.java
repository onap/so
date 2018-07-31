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

package org.onap.so.apihandlerinfra.tenantisolationbeans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "requestReferences")
@JsonInclude(Include.NON_DEFAULT)
public class RequestReferences implements Serializable {
	
	private static final long serialVersionUID = 5873356773819905368L;
	
	@JsonProperty("requestId")
	protected String requestId;
	
	@JsonProperty("instanceId")
	String instanceId;

    /**
     * Gets the value of the requestId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */	
	public String getRequestId() {
		return requestId;
	}
	
    /**
     * Sets the value of the requestId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */	
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
    /**
     * Gets the value of the instanceId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */		
	public String getInstanceId() {
		return instanceId;
	}
	
    /**
     * Sets the value of the instanceId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */		
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	@Override
	public String toString() {
		return "RequestReferences [requestId=" + requestId + 
				               ", instanceId=" + instanceId + "]";
	}
	
}
