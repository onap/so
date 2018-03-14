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

@JsonRootName(value = "tenantIsolationResponse")
@JsonInclude(Include.NON_DEFAULT)
public class TenantIsolationResponse implements Serializable {
	
	private static final long serialVersionUID = 756749312745898666L;
	@JsonProperty("requestId")
	protected String requestId;
	@JsonProperty("status")
	String status;
	@JsonProperty("message")
	String message;	

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
     * Gets the value of the status property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */		
	public String getStatus() {
		return status;
	}
	
    /**
     * Sets the value of the status property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */		
	public void setStatus(String status) {
		this.status = status;
	}
	
	   /**
     * Gets the value of the message property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */		
	public String getMessage() {
		return message;
	}	
	
    /**
     * Sets the value of the message property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */		
	public void setMessage(String message) {
		this.message = message;
	}	


}
