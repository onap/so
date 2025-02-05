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
 *         &lt;element name="SvcInstanceId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SvcAction" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SvcOperation" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CallbackUrl" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="MsoAction" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"requestId", "svcInstanceId", "svcAction", "svcOperation", "callbackUrl", "msoAction"})
@XmlRootElement(name = "RequestHeader")
public class RequestHeader {

    @XmlElement(name = "RequestId", required = true)
    protected String requestId;
    @XmlElement(name = "SvcInstanceId")
    protected String svcInstanceId;
    @XmlElement(name = "SvcAction", required = true)
    protected String svcAction;
    @XmlElement(name = "SvcOperation", required = true)
    protected String svcOperation;
    @XmlElement(name = "CallbackUrl", required = true)
    protected String callbackUrl;
    @XmlElement(name = "MsoAction")
    protected String msoAction;

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
     * Gets the value of the svcInstanceId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSvcInstanceId() {
        return svcInstanceId;
    }

    /**
     * Sets the value of the svcInstanceId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSvcInstanceId(String value) {
        this.svcInstanceId = value;
    }

    /**
     * Gets the value of the svcAction property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSvcAction() {
        return svcAction;
    }

    /**
     * Sets the value of the svcAction property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSvcAction(String value) {
        this.svcAction = value;
    }

    /**
     * Gets the value of the svcOperation property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSvcOperation() {
        return svcOperation;
    }

    /**
     * Sets the value of the svcOperation property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setSvcOperation(String value) {
        this.svcOperation = value;
    }

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
     * Gets the value of the msoAction property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMsoAction() {
        return msoAction;
    }

    /**
     * Sets the value of the msoAction property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setMsoAction(String value) {
        this.msoAction = value;
    }

}
