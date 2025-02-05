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

package org.onap.so.bpmn.common.adapter.vnf;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;



/**
 * <p>
 * Java class for rollbackVnfNotification complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rollbackVnfNotification">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="messageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="completed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="exception" type="{http://org.onap.so/vnfNotify}msoExceptionCategory" minOccurs="0"/>
 *         &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rollbackVnfNotification1", propOrder = {"messageId", "completed", "exception", "errorMessage"})
public class RollbackVnfNotification {

    @XmlElement(required = true)
    protected String messageId;
    protected boolean completed;
    protected MsoExceptionCategory exception;
    protected String errorMessage;

    /**
     * Gets the value of the messageId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the value of the messageId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setMessageId(String value) {
        this.messageId = value;
    }

    /**
     * Gets the value of the completed property.
     * 
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Sets the value of the completed property.
     * 
     */
    public void setCompleted(boolean value) {
        this.completed = value;
    }

    /**
     * Gets the value of the exception property.
     * 
     * @return possible object is {@link MsoExceptionCategory }
     * 
     */
    public MsoExceptionCategory getException() {
        return exception;
    }

    /**
     * Sets the value of the exception property.
     * 
     * @param value allowed object is {@link MsoExceptionCategory }
     * 
     */
    public void setException(MsoExceptionCategory value) {
        this.exception = value;
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    public String toString() {
        String rollbackVnfNotification = "";
        if (exception == null) {
            rollbackVnfNotification = "<ns2:rollbackVnfNotification xmlns:ns2=\"http://org.onap.so/vnfNotify\"" + '\n'
                    + "  xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + '\n' + "   <messageId>"
                    + messageId + "</messageId>" + '\n' + "   <completed>" + completed + "</completed>" + '\n'
                    + "</ns2:rollbackVnfNotification>";
        } else {
            rollbackVnfNotification = "<ns2:rollbackVnfNotification xmlns:ns2=\"http://org.onap.so/vnfNotify\"" + '\n'
                    + "  xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + '\n' + "   <messageId>"
                    + messageId + "</messageId>" + '\n' + "   <completed>" + completed + "</completed>" + '\n'
                    + "   <exception>" + exception + "</exception>" + '\n' + "   <errorMessage>" + errorMessage
                    + "</errorMessage>" + '\n' + "</ns2:rollbackVnfNotification>";
        }
        return rollbackVnfNotification;

    }

}
