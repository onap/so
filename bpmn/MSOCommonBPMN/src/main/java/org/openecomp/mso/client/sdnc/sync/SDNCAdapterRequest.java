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

package org.openecomp.mso.client.sdnc.sync;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
 *         &lt;element ref="{http://org.openecomp/workflow/sdnc/adapter/schema/v1}RequestHeader"/>
 *         &lt;element name="RequestData" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
//BPEL to SDNCAdapter request
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "requestHeader",
    "requestData"
})
@XmlRootElement(name = "SDNCAdapterRequest")
public class SDNCAdapterRequest {

    @XmlElement(name = "RequestHeader", required = true)
    protected RequestHeader requestHeader;
    @XmlElement(name = "RequestData", required = true)
    protected Object requestData;

    /**
     * Gets the value of the requestHeader property.
     *
     * @return
     *     possible object is
     *     {@link RequestHeader }
     *
     */
    public RequestHeader getRequestHeader() {
        return requestHeader;
    }

    /**
     * Sets the value of the requestHeader property.
     *
     * @param value
     *     allowed object is
     *     {@link RequestHeader }
     *
     */
    public void setRequestHeader(RequestHeader value) {
        this.requestHeader = value;
    }

    /**
     * Gets the value of the requestData property.
     *
     * @return
     *     possible object is
     *     {@link Object }
     *
     */
    public Object getRequestData() {
        return requestData;
    }

    /**
     * Sets the value of the requestData property.
     *
     * @param value
     *     allowed object is
     *     {@link Object }
     *
     */
    public void setRequestData(Object value) {
        this.requestData = value;
    }

	@Override
	public String toString() {

		String rd = "";
		if (requestData != null)
		{
			Node node = (Node) requestData;
			Document doc = node.getOwnerDocument();
			rd = Utils.domToStr(doc);
		}
		return "SDNCAdapterRequest [requestHeader=" + requestHeader.toString()
				+ ", requestData=" + rd + "]";
	}
}
