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

package org.openecomp.mso.apihandlerinfra.tasksbeans;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import org.json.JSONArray;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_DEFAULT)
public class TaskList {    
    protected String taskId;   
    protected String type;   
    protected String nfRole;   
    protected String subscriptionServiceType;   
    protected String originalRequestId;   
    protected String originalRequestorId;    
    protected String errorSource;   
    protected String errorCode;   
    protected String errorMessage;    
    protected String buildingBlockName;   
    protected String buildingBlockStep;    
    protected JSONArray validResponses;

    /**
     * Gets the value of the taskId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Sets the value of the taskId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskId(String value) {
        this.taskId = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the nfRole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNfRole() {
        return nfRole;
    }

    /**
     * Sets the value of the nfRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNfRole(String value) {
        this.nfRole = value;
    }

    /**
     * Gets the value of the subscriptionServiceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubscriptionServiceType() {
        return subscriptionServiceType;
    }

    /**
     * Sets the value of the subscriptionServiceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubscriptionServiceType(String value) {
        this.subscriptionServiceType = value;
    }

    /**
     * Gets the value of the originalRequestId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalRequestId() {
        return originalRequestId;
    }

    /**
     * Sets the value of the originalRequestId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalRequestId(String value) {
        this.originalRequestId = value;
    }

    /**
     * Gets the value of the originalRequestorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalRequestorId() {
        return originalRequestorId;
    }

    /**
     * Sets the value of the originalRequestorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalRequestorId(String value) {
        this.originalRequestorId = value;
    }

    /**
     * Gets the value of the errorSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorSource() {
        return errorSource;
    }

    /**
     * Sets the value of the errorSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorSource(String value) {
        this.errorSource = value;
    }

    /**
     * Gets the value of the errorCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the value of the errorCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorCode(String value) {
        this.errorCode = value;
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    /**
     * Gets the value of the buildingBlockName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBuildingBlockName() {
        return buildingBlockName;
    }

    /**
     * Sets the value of the buildingBlockName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBuildingBlockName(String value) {
        this.buildingBlockName = value;
    }

    /**
     * Gets the value of the buildingBlockStep property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBuildingBlockStep() {
        return buildingBlockStep;
    }

    /**
     * Sets the value of the buildingBlockStep property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBuildingBlockStep(String value) {
        this.buildingBlockStep = value;
    }

    /**
     * Gets the value of the validResponses property.
     * 
     * @return
     *     possible object is
     *     {@link ValidResponses }
     *     
     */
    public JSONArray getValidResponses() {
        return validResponses;
    }

    /**
     * Sets the value of the validResponses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValidResponses }
     *     
     */
    public void setValidResponses(JSONArray value) {
        this.validResponses = value;
    }


    

}
