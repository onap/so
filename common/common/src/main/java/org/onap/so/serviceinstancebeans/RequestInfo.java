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

package org.onap.so.serviceinstancebeans;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName(value = "requestInfo")
@JsonInclude(Include.NON_DEFAULT)
public class RequestInfo implements Serializable {

    private static final long serialVersionUID = -1370946827136030181L;
    @JsonProperty("billingAccountNumber")
    protected String billingAccountNumber;
    @JsonProperty("callbackUrl")
    protected String callbackUrl;
    @JsonProperty("correlator")
    protected String correlator;
    @JsonProperty("orderNumber")
    protected String orderNumber;
    @JsonProperty("productFamilyId")
    protected String productFamilyId;
    @JsonProperty("productFamilyName")
    protected String productFamilyName;
    @JsonProperty("orderVersion")
    protected Integer orderVersion;
    @JsonInclude(Include.ALWAYS)
    @JsonProperty("source")
    protected String source;
    @JsonProperty("instanceName")
    protected String instanceName;
    @JsonProperty("suppressRollback")
    @JsonInclude(Include.ALWAYS)
    protected boolean suppressRollback;
    @JsonProperty("requestorId")
    protected String requestorId;
    @JsonProperty("applicationId")
    protected String applicationId;

    /**
     * Gets the value of the callbackUrl property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getCallbackUrl() {
        return callbackUrl;
    }

    /**
     * Sets the value of the callbackUrl property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setCallbackUrl(String value) {
        this.callbackUrl = value;
    }

    /**
     * Gets the value of the correlator property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getCorrelator() {
        return correlator;
    }

    /**
     * Sets the value of the correlator property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setCorrelator(String value) {
        this.correlator = value;
    }

    /**
     * Gets the value of the orderNumber property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    /**
     * Sets the value of the orderNumber property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setOrderNumber(String value) {
        this.orderNumber = value;
    }

    /**
     * Gets the value of the orderVersion property.
     *
     * @return possible object is {@link Integer }
     *
     */
    public Integer getOrderVersion() {
        return orderVersion;
    }

    /**
     * Sets the value of the orderVersion property.
     *
     * @param value allowed object is {@link Integer }
     *
     */
    public void setOrderVersion(Integer value) {
        this.orderVersion = value;
    }

    /**
     * Gets the value of the source property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     *
     * @param value allowed object is {@link String }
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
    };

    public String getProductFamilyName() {
        return productFamilyName;
    }

    public void setProductFamilyName(String productFamilyName) {
        this.productFamilyName = productFamilyName;
    }

    /**
     * Required for Marshalers to send the fields.
     *
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

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public String toString() {
        return "RequestInfo [billingAccountNumber=" + billingAccountNumber + ", callbackUrl=" + callbackUrl
                + ", correlator=" + correlator + ", orderNumber=" + orderNumber + ", productFamilyId=" + productFamilyId
                + ", productFamilyName=" + productFamilyName + ", orderVersion=" + orderVersion + ", source=" + source
                + ", instanceName=" + instanceName + ", suppressRollback=" + suppressRollback + ", requestorId="
                + requestorId + ", applicationId=" + applicationId + "]";
    }


}
