/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("vnfc")
public class Vnfc implements Serializable {


    /**
     * 
     */
    private static final long serialVersionUID = -9170269565756850796L;
    @Id
    @JsonProperty("vnfc-name")
    private String vnfcName;
    @JsonProperty("nfc-naming-code")
    private String nfcNamingCode;
    @JsonProperty("nfc-function")
    private String nfcFunction;
    @JsonProperty("prov-status")
    private String provStatus;
    @JsonProperty("orchestration-status")
    private String orchestrationStatus;
    @JsonProperty("ipaddress-v4-oam-vip")
    private String ipaddressV4OamVip;
    @JsonProperty("in-maint")
    private String inMaint;
    @JsonProperty("is-closed-loop-disabled")
    private String isClosedLoopDisabled;
    @JsonProperty("group-notation")
    private String groupNotation;
    @JsonProperty("model-invariant-id")
    private String modelInvariantId;
    @JsonProperty("model-version-id")
    private String modelVersionId;
    @JsonProperty("model-customization-id")
    private String modelCustomizationId;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Vnfc)) {
            return false;
        }
        Vnfc castOther = (Vnfc) other;
        return new EqualsBuilder().append(vnfcName, castOther.vnfcName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vnfcName).toHashCode();
    }

    public String getVnfcName() {
        return vnfcName;
    }

    public void setVnfcName(String vnfcName) {
        this.vnfcName = vnfcName;
    }

    public String getNfcNamingCode() {
        return nfcNamingCode;
    }

    public void setNfcNamingCode(String nfcNamingCode) {
        this.nfcNamingCode = nfcNamingCode;
    }

    public String getNfcFunction() {
        return nfcFunction;
    }

    public void setNfcFunction(String nfcFunction) {
        this.nfcFunction = nfcFunction;
    }

    public String getProvStatus() {
        return provStatus;
    }

    public void setProvStatus(String provStatus) {
        this.provStatus = provStatus;
    }

    public String getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(String orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public String getIpaddressV4OamVip() {
        return ipaddressV4OamVip;
    }

    public void setIpaddressV4OamVip(String ipaddressV4OamVip) {
        this.ipaddressV4OamVip = ipaddressV4OamVip;
    }

    public String getInMaint() {
        return inMaint;
    }

    public void setInMaint(String inMaint) {
        this.inMaint = inMaint;
    }

    public String getIsClosedLoopDisabled() {
        return isClosedLoopDisabled;
    }

    public void setIsClosedLoopDisabled(String isClosedLoopDisabled) {
        this.isClosedLoopDisabled = isClosedLoopDisabled;
    }

    public String getGroupNotation() {
        return groupNotation;
    }

    public void setGroupNotation(String groupNotation) {
        this.groupNotation = groupNotation;
    }

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    public String getModelCustomizationId() {
        return modelCustomizationId;
    }

    public void setModelCustomizationId(String modelCustomizationId) {
        this.modelCustomizationId = modelCustomizationId;
    }
}
