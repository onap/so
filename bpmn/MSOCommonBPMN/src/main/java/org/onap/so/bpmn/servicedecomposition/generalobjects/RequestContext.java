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

package org.onap.so.bpmn.servicedecomposition.generalobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("request-context")
public class RequestContext implements Serializable {

    private static final long serialVersionUID = -6482733428879732822L;

    @JsonProperty("product-family-id")
    private String productFamilyId;
    @JsonProperty("source")
    private String source;
    @JsonProperty("requestor-id")
    private String requestorId;
    @JsonProperty("subscription-service-type")
    private String subscriptionServiceType;
    @JsonProperty("user-params")
    private Map<String, Object> userParams = new HashMap<>();
    @JsonProperty("action")
    private String action;
    @JsonProperty("callback-url")
    private String callbackURL;
    @JsonProperty("service-uri")
    private String serviceURI;
    @JsonProperty("mso-request-id")
    private String msoRequestId;
    @JsonProperty("requestParameters")
    private RequestParameters requestParameters;
    @JsonProperty("configurationParameters")
    private List<Map<String, String>> configurationParameters = new ArrayList<>();
    @JsonProperty("application-id")
    private String applicationId;
    @JsonProperty("is-helm")
    private Boolean isHelm;

    public Boolean getIsHelm() {
        return isHelm;
    }

    public void setIsHelm(Boolean isHelm) {
        this.isHelm = isHelm;
    }

    public String getServiceURI() {
        return serviceURI;
    }

    public void setServiceURI(String serviceURI) {
        this.serviceURI = serviceURI;
    }

    public String getProductFamilyId() {
        return productFamilyId;
    }

    public void setProductFamilyId(String productFamilyId) {
        this.productFamilyId = productFamilyId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }

    public String getSubscriptionServiceType() {
        return subscriptionServiceType;
    }

    public void setSubscriptionServiceType(String subscriptionServiceType) {
        this.subscriptionServiceType = subscriptionServiceType;
    }

    public Map<String, Object> getUserParams() {
        return userParams;
    }

    public void setUserParams(Map<String, Object> userParams) {
        this.userParams = userParams;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public String getMsoRequestId() {
        return msoRequestId;
    }

    public void setMsoRequestId(String msoRequestId) {
        this.msoRequestId = msoRequestId;
    }

    public RequestParameters getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(RequestParameters requestParameters) {
        this.requestParameters = requestParameters;
    }

    public List<Map<String, String>> getConfigurationParameters() {
        return configurationParameters;
    }

    public void setConfigurationParameters(List<Map<String, String>> configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
}
