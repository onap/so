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
public class RequestDetails implements Serializable {

	private static final long serialVersionUID = -73080684945860609L;
	@JsonProperty("requestInfo")
    protected RequestInfo requestInfo;
	@JsonProperty("relatedInstanceList")
    protected RelatedInstanceList[] relatedInstanceList;
	@JsonProperty("requestParameters")
    protected RequestParameters requestParameters;

    /**
     * Gets the value of the requestInfo property.
     *
     * @return
     *     possible object is
     *     {@link RequestInfo }
     *
     */
    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    /**
     * Sets the value of the requestInfo property.
     *
     * @param value
     *     allowed object is
     *     {@link RequestInfo }
     *
     */
    public void setRequestInfo(RequestInfo value) {
        this.requestInfo = value;
    }

    /**
     * Gets the value of the requestParameters property.
     *
     * @return
     *     possible object is
     *     {@link RequestParameters }
     *
     */
    public RequestParameters getRequestParameters() {
        return requestParameters;
    }

    /**
     * Sets the value of the requestParameters property.
     *
     * @param value
     *     allowed object is
     *     {@link RequestParameters }
     *
     */
    public void setRequestParameters(RequestParameters value) {
        this.requestParameters = value;
    }

	public RelatedInstanceList[] getRelatedInstanceList() {
		return relatedInstanceList;
	}

	public void setRelatedInstanceList(RelatedInstanceList[] relatedInstanceList) {
		this.relatedInstanceList = relatedInstanceList;
	}
	@Override
	public String toString() {
		return "RequestDetails [requestInfo=" + requestInfo + 
				            ", relatedInstanceList=" + Arrays.toString(relatedInstanceList) + 
				            ", requestParameters=" + requestParameters + "]";
	}
}
