/*-
 * ============LICENSE_START=======================================================
 * OPENECOMP - MSO
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

package org.openecomp.mso.apihandlerinfra.serviceinstancebeans;

import java.util.Arrays;

import org.codehaus.jackson.map.annotate.JsonRootName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonRootName(value = "requestDetails")
@JsonSerialize(include=JsonSerialize.Inclusion.NON_DEFAULT)
public class RequestDetails {

    protected ModelInfo modelInfo;
    protected RequestInfo requestInfo;
    protected RelatedInstanceList[] relatedInstanceList;
    protected SubscriberInfo subscriberInfo;
    protected CloudConfiguration cloudConfiguration;
    protected RequestParameters requestParameters;

    /**
     * Gets the value of the serviceInfo property.
     *
     * @return
     *     possible object is
     *     {@link ModelInfo }
     *
     */
    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    /**
     * Sets the value of the serviceInfo property.
     *
     * @param value
     *     allowed object is
     *     {@link ModelInfo }
     *
     */
    public void setModelInfo(ModelInfo value) {
        this.modelInfo = value;
    }

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
     * Gets the value of the subscriberInfo property.
     *
     * @return
     *     possible object is
     *     {@link SubscriberInfo }
     *
     */
    public SubscriberInfo getSubscriberInfo() {
        return subscriberInfo;
    }

    /**
     * Sets the value of the subscriberInfo property.
     *
     * @param value
     *     allowed object is
     *     {@link SubscriberInfo }
     *
     */
    public void setSubscriberInfo(SubscriberInfo value) {
        this.subscriberInfo = value;
    }

    /**
     * Gets the value of the cloudConfiguration property.
     *
     * @return
     *     possible object is
     *     {@link CloudConfiguration }
     *
     */
    public CloudConfiguration getCloudConfiguration() {
        return cloudConfiguration;
    }

    /**
     * Sets the value of the cloudConfiguration property.
     *
     * @param value
     *     allowed object is
     *     {@link CloudConfiguration }
     *
     */
    public void setCloudConfiguration(CloudConfiguration value) {
        this.cloudConfiguration = value;
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
		return "RequestDetails [modelInfo=" + modelInfo + ", requestInfo="
				+ requestInfo + ", relatedInstanceList="
				+ Arrays.toString(relatedInstanceList) + ", subscriberInfo="
				+ subscriberInfo + ", cloudConfiguration=" + cloudConfiguration
				+ ", requestParameters=" + requestParameters + "]";
	}

}
