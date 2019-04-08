/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.network.async.client;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>
 * Java class for networkRollback complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="networkRollback">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="cloudId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="msoRequest" type="{http://org.onap.so/networkNotify}msoRequest" minOccurs="0"/>
 *         &lt;element name="networkCreated" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="networkId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="networkStackId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="networkName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="networkType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="networkUpdated" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="neutronNetworkId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="physicalNetwork" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tenantId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vlans" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "networkRollback",
        propOrder = {"cloudId", "msoRequest", "networkCreated", "networkId", "networkStackId", "networkName",
                "networkType", "networkUpdated", "neutronNetworkId", "physicalNetwork", "tenantId", "vlans"})
public class NetworkRollback {

    protected String cloudId;
    protected MsoRequest msoRequest;
    protected boolean networkCreated;
    protected String networkId;
    protected String networkStackId;
    protected String networkName;
    protected String networkType;
    protected boolean networkUpdated;
    protected String neutronNetworkId;
    protected String physicalNetwork;
    protected String tenantId;
    @XmlElement(nillable = true)
    protected List<Integer> vlans;

    /**
     * Gets the value of the cloudId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCloudId() {
        return cloudId;
    }

    /**
     * Sets the value of the cloudId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setCloudId(String value) {
        this.cloudId = value;
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
     * Gets the value of the networkCreated property.
     * 
     */
    public boolean isNetworkCreated() {
        return networkCreated;
    }

    /**
     * Sets the value of the networkCreated property.
     * 
     */
    public void setNetworkCreated(boolean value) {
        this.networkCreated = value;
    }

    /**
     * Gets the value of the networkId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getNetworkId() {
        return networkId;
    }

    /**
     * Sets the value of the networkId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setNetworkId(String value) {
        this.networkId = value;
    }

    /**
     * Gets the value of the networkStackId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getNetworkStackId() {
        return networkStackId;
    }

    /**
     * Sets the value of the networkStackId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setNetworkStackId(String value) {
        this.networkStackId = value;
    }

    /**
     * Gets the value of the networkName property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getNetworkName() {
        return networkName;
    }

    /**
     * Sets the value of the networkName property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setNetworkName(String value) {
        this.networkName = value;
    }

    /**
     * Gets the value of the networkType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getNetworkType() {
        return networkType;
    }

    /**
     * Sets the value of the networkType property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setNetworkType(String value) {
        this.networkType = value;
    }

    /**
     * Gets the value of the networkUpdated property.
     * 
     */
    public boolean isNetworkUpdated() {
        return networkUpdated;
    }

    /**
     * Sets the value of the networkUpdated property.
     * 
     */
    public void setNetworkUpdated(boolean value) {
        this.networkUpdated = value;
    }

    /**
     * Gets the value of the neutronNetworkId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getNeutronNetworkId() {
        return neutronNetworkId;
    }

    /**
     * Sets the value of the neutronNetworkId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setNeutronNetworkId(String value) {
        this.neutronNetworkId = value;
    }

    /**
     * Gets the value of the physicalNetwork property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPhysicalNetwork() {
        return physicalNetwork;
    }

    /**
     * Sets the value of the physicalNetwork property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setPhysicalNetwork(String value) {
        this.physicalNetwork = value;
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
     * Gets the value of the vlans property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the vlans property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getVlans().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Integer }
     * 
     * 
     */
    public List<Integer> getVlans() {
        if (vlans == null) {
            vlans = new ArrayList<>();
        }
        return this.vlans;
    }

}
