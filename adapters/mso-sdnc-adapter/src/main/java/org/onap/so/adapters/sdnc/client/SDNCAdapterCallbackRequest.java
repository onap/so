/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.adapters.sdnc.client;


import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *         &lt;element name="RequestData" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
// SDNCAdapter to BPEL Async response
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"callbackHeader", "requestData"})
@XmlRootElement(name = "SDNCAdapterCallbackRequest")
public class SDNCAdapterCallbackRequest {

    @XmlElement(name = "CallbackHeader", required = true)
    protected CallbackHeader callbackHeader;
    @XmlElement(name = "RequestData")
    protected Object requestData;

    private static Logger logger = LoggerFactory.getLogger(SDNCAdapterCallbackRequest.class);

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
        try {
            JAXBContext ctx = JAXBContext.newInstance("org.onap.so.adapters.sdnc.client");
            Marshaller m = ctx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            StringWriter w = new StringWriter();
            m.marshal(this, w);
            return w.toString();
        } catch (Exception e) {
            logger.error(LoggingAnchor.THREE, MessageEnum.RA_MARSHING_ERROR.toString(), ErrorCode.DataError.getValue(),
                    "Exception - MARSHING_ERROR", e);
        }
        return "";
    }
}
