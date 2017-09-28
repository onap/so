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

package org.openecomp.mso.bpmn.common.adapter.vnf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createVnfNotification complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createVnfNotification">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="messageId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="completed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="exception" type="{http://org.openecomp.mso/vnfNotify}msoExceptionCategory" minOccurs="0"/>
 *         &lt;element name="errorMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vnfId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="outputs" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="rollback" type="{http://org.openecomp.mso/vnfNotify}vnfRollback" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createVnfNotification1", propOrder = {
    "messageId",
    "completed",
    "exception",
    "errorMessage",
    "vnfId",
    "outputs",
    "rollback"
})
public class CreateVnfNotification {

    @XmlElement(required = true)
    protected String messageId;
    protected boolean completed;
    protected MsoExceptionCategory exception;
    protected String errorMessage;
    protected String vnfId;
    protected CreateVnfNotification.Outputs outputs;
    protected VnfRollback rollback;

    /**
     * Gets the value of the messageId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the value of the messageId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
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
     * @return
     *     possible object is
     *     {@link MsoExceptionCategory }
     *     
     */
    public MsoExceptionCategory getException() {
        return exception;
    }

    /**
     * Sets the value of the exception property.
     * 
     * @param value
     *     allowed object is
     *     {@link MsoExceptionCategory }
     *     
     */
    public void setException(MsoExceptionCategory value) {
        this.exception = value;
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
     * Gets the value of the vnfId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVnfId() {
        return vnfId;
    }

    /**
     * Sets the value of the vnfId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVnfId(String value) {
        this.vnfId = value;
    }

    /**
     * Gets the value of the outputs property.
     * 
     * @return
     *     possible object is
     *     {@link CreateVnfNotification.Outputs }
     *     
     */
    public CreateVnfNotification.Outputs getOutputs() {
        return outputs;
    }

    /**
     * Sets the value of the outputs property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreateVnfNotification.Outputs }
     *     
     */
    public void setOutputs(CreateVnfNotification.Outputs value) {
        this.outputs = value;
    }

    /**
     * Gets the value of the rollback property.
     * 
     * @return
     *     possible object is
     *     {@link VnfRollback }
     *     
     */
    public VnfRollback getRollback() {
        return rollback;
    }

    /**
     * Sets the value of the rollback property.
     * 
     * @param value
     *     allowed object is
     *     {@link VnfRollback }
     *     
     */
    public void setRollback(VnfRollback value) {
        this.rollback = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "entry"
    })
    public static class Outputs {

        protected List<CreateVnfNotification.Outputs.Entry> entry;
        
        /**
         * Gets the value of the entry property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the entry property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getEntry().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CreateVnfNotification.Outputs.Entry }
         * 
         * 
         */
        public List<CreateVnfNotification.Outputs.Entry> getEntry() {
            if (entry == null) {
                entry = new ArrayList<>();
            }
            return this.entry;
        }

        // Not a generated method
	@Override
        public String toString() {
        	StringBuilder out = new StringBuilder();
        	out.append("<outputs>\n");
        	if (entry != null) {
        		for (Entry e : entry) {
        			out.append(e.toString());
        			out.append('\n');
        		}
        	}
        	out.append("</outputs>");
        	return out.toString();
        }

        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;sequence>
         *         &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *       &lt;/sequence>
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "key",
            "value"
        })
        public static class Entry {

            protected String key;
            protected String value;

            /**
             * Gets the value of the key property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getKey() {
                return key;
            }

            /**
             * Sets the value of the key property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setKey(String value) {
                this.key = value;
            }

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
                
            }
            
            // Not a generated method
            @Override
            public String toString() {
            	String entrystr;
            		entrystr =
               "<entry>"+ '\n' +
    	       "   <key>"+key+"</key>" + '\n' +
    	       "   <value>"+value+"</value>" + '\n' +
    	       "</entry>";
            	return entrystr; 
            }

        }

    }

	// Not a generated method
	@Override
	public String toString() {
    	String createVnfNotification;
    	if (exception==null) {
    		createVnfNotification = "<ns2:createVnfNotification xmlns:ns2=\"http://org.openecomp.mso/vnfNotify\"" + '\n' +
    				       "  xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + '\n' +
    				       "   <messageId>"+messageId+"</messageId>" + '\n' +
    				       "   <completed>"+completed+"</completed>" + '\n' +
    				       "   <vnfId>"+vnfId+"</vnfId>" + '\n' +
    				       (outputs == null ? "" : outputs.toString() + '\n') +
    				       "   <rollback>"+rollback+"</rollback>" + '\n' +
    				       "</ns2:createVnfNotification>";
    	} else {
    		createVnfNotification = "<ns2:createVnfNotification xmlns:ns2=\"http://org.openecomp.mso/vnfNotify\"" + '\n' +
				       "  xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + '\n' +
				       "   <messageId>"+messageId+"</messageId>" + '\n' +
				       "   <completed>"+completed+"</completed>" + '\n' +
    				       (outputs == null ? "" : outputs.toString() + '\n') +
    				   "   <exception>"+exception+"</exception>" + '\n' +
    				   "   <errorMessage>"+errorMessage+"</errorMessage>" + '\n' +
				       "</ns2:createVnfNotification>";
    	}
    	return createVnfNotification;
   	}    
}
