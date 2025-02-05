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
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import jakarta.persistence.Id;

@JsonRootName("lag-interface")
public class LagInterface implements Serializable, ShallowCopy<LagInterface> {

    private static final long serialVersionUID = -7493461787172382640L;

    @Id
    @JsonProperty("interface-name")
    private String interfaceName;
    @JsonProperty("interface-description")
    private String interfaceDescription;
    @JsonProperty("speed-value")
    private String speedValue;
    @JsonProperty("speed-units")
    private String speedUnits;
    @JsonProperty("interface-id")
    private String interfaceId;
    @JsonProperty("interface-role")
    private String interfaceRole;
    @JsonProperty("prov-status")
    private String provStatus;
    @JsonProperty("in-maint")
    private Boolean inMaint;
    @JsonProperty("l-interfaces")
    private List<LInterface> lInterfaces = new ArrayList<>();

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceDescription() {
        return interfaceDescription;
    }

    public void setInterfaceDescription(String interfaceDescription) {
        this.interfaceDescription = interfaceDescription;
    }

    public String getSpeedValue() {
        return speedValue;
    }

    public void setSpeedValue(String speedValue) {
        this.speedValue = speedValue;
    }

    public String getSpeedUnits() {
        return speedUnits;
    }

    public void setSpeedUnits(String speedUnits) {
        this.speedUnits = speedUnits;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getInterfaceRole() {
        return interfaceRole;
    }

    public void setInterfaceRole(String interfaceRole) {
        this.interfaceRole = interfaceRole;
    }

    public String getProvStatus() {
        return provStatus;
    }

    public void setProvStatus(String provStatus) {
        this.provStatus = provStatus;
    }

    public Boolean isInMaint() {
        return inMaint;
    }

    public void setInMaint(boolean inMaint) {
        this.inMaint = inMaint;
    }

    public List<LInterface> getlInterfaces() {
        return lInterfaces;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof LagInterface)) {
            return false;
        }
        LagInterface castOther = (LagInterface) other;
        return new EqualsBuilder().append(interfaceName, castOther.interfaceName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(interfaceName).toHashCode();
    }
}
