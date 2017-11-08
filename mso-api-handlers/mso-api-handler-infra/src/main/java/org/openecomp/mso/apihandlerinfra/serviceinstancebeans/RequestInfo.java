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

package org.openecomp.mso.apihandlerinfra.serviceinstancebeans;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_DEFAULT)
public class RequestInfo {

	protected String billingAccountNumber;
	protected String callbackUrl;
    protected String correlator;
    protected String orderNumber;
    protected String productFamilyId;
    protected Integer orderVersion;
    protected String source;
    protected String instanceName;
    @JsonSerialize(include=Inclusion.ALWAYS)
    protected boolean suppressRollback;
    protected String requestorId;

    /**
     * Gets the value of the callbackUrl property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Sets the value of the callbackUrl property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCallbackUrl(String value) {
        this.callbackUrl = value;
    }

    /**
     * Gets the value of the correlator property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCorrelator() {
        return correlator;
    }

    /**
     * Sets the value of the correlator property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCorrelator(String value) {
        this.correlator = value;
    }

    /**
     * Gets the value of the orderNumber property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    /**
     * Sets the value of the orderNumber property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setOrderNumber(String value) {
        this.orderNumber = value;
    }

    /**
     * Gets the value of the orderVersion property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getOrderVersion() {
        return orderVersion;
    }

    /**
     * Sets the value of the orderVersion property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setOrderVersion(Integer value) {
        this.orderVersion = value;
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
    	if(null == source || source.isEmpty()){
    		source = "VID";
    	}
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

	public String getBillingAccountNumber() {
		return billingAccountNumber;
	}

	public void setBillingAccountNumber(String billingAccountNumber) {
		this.billingAccountNumber = billingAccountNumber;
	}

	public String getProductFamilyId() {
		return productFamilyId;
	}

	public void setProductFamilyId(String productFamilyId) {
		this.productFamilyId = productFamilyId;
	}

	/**
	 * Required for Marshalers to send the fields.
	 * @return
	 */
	public boolean getSuppressRollback() {
		return suppressRollback;
	}

	public void setSuppressRollback(boolean suppressRollback) {
		this.suppressRollback = suppressRollback;
	}

	public String getRequestorId() {
		return requestorId;
	}

	public void setRequestorId(String requestorId) {
		this.requestorId = requestorId;
	}

	@Override
	public String toString() {
		return "RequestInfo [billingAccountNumber=" + billingAccountNumber
				+ ", callbackUrl=" + callbackUrl + ", correlator=" + correlator
				+ ", orderNumber=" + orderNumber + ", productFamilyId="
				+ productFamilyId + ", orderVersion=" + orderVersion
				+ ", source=" + source + ", instanceName=" + instanceName
				+ ", suppressRollback=" + suppressRollback + ", requestorId="
				+ requestorId + "]";
	}


}
