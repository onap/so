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

@JsonRootName(value = "requestInfo")
@JsonInclude(Include.NON_DEFAULT)
public class RequestInfo implements Serializable {

	private static final long serialVersionUID = 1346372792555344857L;
	@JsonProperty("resourceType")
    protected ResourceType resourceType;
	@JsonProperty("source")
    protected String source;
	@JsonProperty("instanceName")
    protected String instanceName;
	@JsonProperty("requestorId")
    protected String requestorId;

	/**
     * Gets the value of the resourceType property.
     *
     * @return
     *     possible object is
     *     {@link ResourceType }
     *
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value
     *     allowed object is
     *     {@link ResourceType }
     *
     */
    public void setResourceType(ResourceType value) {
        this.resourceType = value;
    }


    /**
     * Gets the value of the source property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSource(String value) {
        this.source = value;
    }

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getRequestorId() {
		return requestorId;
	}

	public void setRequestorId(String requestorId) {
		this.requestorId = requestorId;
	}

	@Override
	public String toString() {
		return "RequestInfo [source=" + source 
				        + ", instanceName=" + instanceName
				        + ", requestorId=" 	+ requestorId 
				        + ", resourceType=" 	+ resourceType + "]";
	}


}
