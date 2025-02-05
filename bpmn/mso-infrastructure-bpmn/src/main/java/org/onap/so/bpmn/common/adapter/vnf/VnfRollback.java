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
import jakarta.xml.bind.annotation.XmlType;



/**
 * <p>
 * Java class for vnfRollback complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="vnfRollback">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cloudSiteId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cloudOwner" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="msoRequest" type="{http://org.onap.so/vnfNotify}msoRequest" minOccurs="0"/>
 *         &lt;element name="tenantCreated" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="tenantId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vnfCreated" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="vnfId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "vnfRollback",
        propOrder = {"cloudSiteId", "cloudOwner", "msoRequest", "tenantCreated", "tenantId", "vnfCreated", "vnfId"})
public class VnfRollback {

    protected String cloudSiteId;
    protected String cloudOwner;
    protected MsoRequest msoRequest;
    protected boolean tenantCreated;
    protected String tenantId;
    protected boolean vnfCreated;
    protected String vnfId;

    /**
     * Gets the value of the cloudSiteId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCloudSiteId() {
        return cloudSiteId;
    }

    /**
     * Sets the value of the cloudSiteId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setCloudSiteId(String value) {
        this.cloudSiteId = value;
    }

    /**
     * Gets the value of the cloudOwner property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCloudOwner() {
        return cloudOwner;
    }

    /**
     * Sets the value of the cloudOwner property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setCloudOwner(String value) {
        this.cloudOwner = value;
    }

    /**
     * Gets the value of the msoRequest property.
     * 
     * @return possible object is {@link MsoRequest }
     * 
     */
    public MsoRequest getMsoRequest() {
        return msoRequest;
    }

    /**
     * Sets the value of the msoRequest property.
     * 
     * @param value allowed object is {@link MsoRequest }
     * 
     */
    public void setMsoRequest(MsoRequest value) {
        this.msoRequest = value;
    }

    /**
     * Gets the value of the tenantCreated property.
     * 
     */
    public boolean isTenantCreated() {
        return tenantCreated;
    }

    /**
     * Sets the value of the tenantCreated property.
     * 
     */
    public void setTenantCreated(boolean value) {
        this.tenantCreated = value;
    }

    /**
     * Gets the value of the tenantId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Sets the value of the tenantId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTenantId(String value) {
        this.tenantId = value;
    }

    /**
     * Gets the value of the vnfCreated property.
     * 
     */
    public boolean isVnfCreated() {
        return vnfCreated;
    }

    /**
     * Sets the value of the vnfCreated property.
     * 
     */
    public void setVnfCreated(boolean value) {
        this.vnfCreated = value;
    }

    /**
     * Gets the value of the vnfId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getVnfId() {
        return vnfId;
    }

    /**
     * Sets the value of the vnfId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setVnfId(String value) {
        this.vnfId = value;
    }

    public String toString() {
        String msoRequestElement = msoRequest == null ? "" : "<msoRequest>" + msoRequest + "</msoRequest>" + '\n';

        return "<cloudSiteId>" + cloudSiteId + "</cloudSiteId>" + '\n' + "<cloudOwner>" + cloudOwner + "</cloudOwner>"
                + '\n' + msoRequestElement + "<tenantCreated>" + tenantCreated + "</tenantCreated>" + '\n'
                + "<tenantId>" + tenantId + "</tenantId>" + '\n' + "<vnfCreated>" + vnfCreated + "</vnfCreated>" + '\n'
                + "<vnfId>" + vnfId + "</vnfId>";
    }
}
