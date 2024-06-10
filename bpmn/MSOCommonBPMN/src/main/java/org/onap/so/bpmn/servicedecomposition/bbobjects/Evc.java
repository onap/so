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

package org.onap.so.bpmn.servicedecomposition.bbobjects;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import jakarta.persistence.Id;

@JsonRootName("evc")
public class Evc implements Serializable, ShallowCopy<Evc> {

    private static final long serialVersionUID = -3556506672371317659L;

    @Id
    @JsonProperty("evc-id")
    private String evcId;
    @JsonProperty("forwarding-path-topology")
    private String forwardingPathTopology;
    @JsonProperty("cir-value")
    private String cirValue;
    @JsonProperty("cir-units")
    private String cirUnits;
    @JsonProperty("connection-diversity-group-id")
    private String connectionDiversityGroupId;
    @JsonProperty("service-hours")
    private String serviceHours;
    @JsonProperty("esp-evc-circuit-id")
    private String espEvcCircuitId;
    @JsonProperty("esp-evc-cir-value")
    private String espEvcCirValue;
    @JsonProperty("esp-evc-cir-units")
    private String espEvcCirUnits;
    @JsonProperty("esp-itu-code")
    private String espItuCode;
    @JsonProperty("collector-pop-clli")
    private String collectorPopClli;
    @JsonProperty("inter-connect-type-ingress")
    private String interConnectTypeIngress;
    @JsonProperty("tagmode-access-ingress")
    private String tagmodeAccessIngress;
    @JsonProperty("tagmode-access-egress")
    private String tagmodeAccessEgress;

    public String getEvcId() {
        return evcId;
    }

    public void setEvcId(String evcId) {
        this.evcId = evcId;
    }

    public String getForwardingPathTopology() {
        return forwardingPathTopology;
    }

    public void setForwardingPathTopology(String forwardingPathTopology) {
        this.forwardingPathTopology = forwardingPathTopology;
    }

    public String getCirValue() {
        return cirValue;
    }

    public void setCirValue(String cirValue) {
        this.cirValue = cirValue;
    }

    public String getCirUnits() {
        return cirUnits;
    }

    public void setCirUnits(String cirUnits) {
        this.cirUnits = cirUnits;
    }

    public String getConnectionDiversityGroupId() {
        return connectionDiversityGroupId;
    }

    public void setConnectionDiversityGroupId(String connectionDiversityGroupId) {
        this.connectionDiversityGroupId = connectionDiversityGroupId;
    }

    public String getServiceHours() {
        return serviceHours;
    }

    public void setServiceHours(String serviceHours) {
        this.serviceHours = serviceHours;
    }

    public String getEspEvcCircuitId() {
        return espEvcCircuitId;
    }

    public void setEspEvcCircuitId(String espEvcCircuitId) {
        this.espEvcCircuitId = espEvcCircuitId;
    }

    public String getEspEvcCirValue() {
        return espEvcCirValue;
    }

    public void setEspEvcCirValue(String espEvcCirValue) {
        this.espEvcCirValue = espEvcCirValue;
    }

    public String getEspEvcCirUnits() {
        return espEvcCirUnits;
    }

    public void setEspEvcCirUnits(String espEvcCirUnits) {
        this.espEvcCirUnits = espEvcCirUnits;
    }

    public String getEspItuCode() {
        return espItuCode;
    }

    public void setEspItuCode(String espItuCode) {
        this.espItuCode = espItuCode;
    }

    public String getCollectorPopClli() {
        return collectorPopClli;
    }

    public void setCollectorPopClli(String collectorPopClli) {
        this.collectorPopClli = collectorPopClli;
    }

    public String getInterConnectTypeIngress() {
        return interConnectTypeIngress;
    }

    public void setInterConnectTypeIngress(String interConnectTypeIngress) {
        this.interConnectTypeIngress = interConnectTypeIngress;
    }

    public String getTagmodeAccessIngress() {
        return tagmodeAccessIngress;
    }

    public void setTagmodeAccessIngress(String tagmodeAccessIngress) {
        this.tagmodeAccessIngress = tagmodeAccessIngress;
    }

    public String getTagmodeAccessEgress() {
        return tagmodeAccessEgress;
    }

    public void setTagmodeAccessEgress(String tagmodeAccessEgress) {
        this.tagmodeAccessEgress = tagmodeAccessEgress;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Evc)) {
            return false;
        }
        Evc castOther = (Evc) other;
        return new EqualsBuilder().append(evcId, castOther.evcId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(evcId).toHashCode();
    }
}
