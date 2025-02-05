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

import java.io.StringWriter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
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
 *         &lt;element ref="{http://org.onap/workflow/sdnc/adapter/schema/v1}CallbackHeader"/>
 *         &lt;element name="RequestData" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", namespace = "http://org.onap/workflow/sdnc/adapter/schema/v1",
        propOrder = {"callbackHeader", "requestData"})
@XmlRootElement(name = "SDNCAdapterCallbackRequest", namespace = "http://org.onap/workflow/sdnc/adapter/schema/v1")
public class SDNCAdapterCallbackRequest {

    @XmlElement(name = "CallbackHeader", required = true, namespace = "http://org.onap/workflow/sdnc/adapter/schema/v1")
    protected CallbackHeader callbackHeader;
    @XmlElement(name = "RequestData", required = false, namespace = "http://org.onap/workflow/sdnc/adapter/schema/v1")
    protected Object requestData;

    /**
     * Gets the value of the callbackHeader property.
     * 
     * @return possible object is {@link CallbackHeader }
     * 
     */
    public CallbackHeader getCallbackHeader() {
        return callbackHeader;
    }

    /**
     * Sets the value of the callbackHeader property.
     * 
     * @param value allowed object is {@link CallbackHeader }
     * 
     */
    public void setCallbackHeader(CallbackHeader value) {
        this.callbackHeader = value;
    }

    /**
     * Gets the value of the requestData property.
     * 
     * @return possible object is {@link Object }
     * 
     */
    public Object getRequestData() {
        return requestData;
    }

    /**
     * Sets the value of the requestData property.
     * 
     * @param value allowed object is {@link Object }
     * 
     */
    public void setRequestData(Object value) {
        this.requestData = value;
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            JAXBContext context = JAXBContext.newInstance(SDNCAdapterCallbackRequest.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(this, writer);
            return writer.getBuffer().toString();
        } catch (JAXBException e) {
            return "";
        }
    }
}
