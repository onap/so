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
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value = "requestDetails")
@JsonInclude(Include.NON_DEFAULT)
public class RequestList {


	@JsonProperty("request")
    protected Request request;
	@JsonProperty("requestStatus")
    protected RequestStatus requestStatus;

    /**
     * Gets the value of the request property.
     *
     * @return
     *     possible object is
     *     {@link Request }
     *
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Sets the value of the requestInfo property.
     *
     * @param value
     *     allowed object is
     *     {@link Request }
     *
     */
    public void setRequest(Request value) {
        this.request = value;
    }

    /**
     * Gets the value of the requestStatus property.
     *
     * @return
     *     possible object is
     *     {@link RequestStatus }
     *
     */
    public RequestStatus getRequestStatus() {
        return requestStatus;
    }
    

    /**
     * Sets the value of the requestStatus property.
     *
     * @param value
     *     allowed object is
     *     {@link RequestStatus }
     *
     */
    public void setRequestStatus(RequestStatus value) {
        this.requestStatus = value;
    }


	@Override
	public String toString() {
		return "RequestList [request=" + request + 
				            ", requestStatus=" + requestStatus + "]";
	}
}
