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

package org.onap.so.bpmn.common.adapter.sdnc;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequestId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ResponseCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ResponseMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"requestId", "responseCode", "responseMessage"},
        namespace = "http://org.onap/workflow/sdnc/adapter/schema/v1")
@XmlRootElement(name = "CallbackHeader")
public class CallbackHeader {

    @XmlElement(name = "RequestId", required = true, namespace = "http://org.onap/workflow/sdnc/adapter/schema/v1")
    protected String requestId;
    @XmlElement(name = "ResponseCode", required = true, namespace = "http://org.onap/workflow/sdnc/adapter/schema/v1")
    protected String responseCode;
    @XmlElement(name = "ResponseMessage", required = true,
            namespace = "http://org.onap/workflow/sdnc/adapter/schema/v1")
    protected String responseMessage;

    /**
     * Gets the value of the requestId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the value of the requestId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setRequestId(String value) {
        this.requestId = value;
    }

    /**
     * Gets the value of the responseCode property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getResponseCode() {
        return responseCode;
    }

    /**
     * Sets the value of the responseCode property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setResponseCode(String value) {
        this.responseCode = value;
    }

    /**
     * Gets the value of the responseMessage property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * Sets the value of the responseMessage property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setResponseMessage(String value) {
        this.responseMessage = value;
    }

}
