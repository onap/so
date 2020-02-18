/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.core.domain;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("LogicalLink")
public class LogicalLink extends JsonWrapper implements Serializable {

    private static final long serialVersionUID = -7493461787172382640L;

    @Id
    @JsonProperty("link-name")
    private String linkName;
    @JsonProperty("in-maint")
    private String inMaint;
    @JsonProperty("link-type")
    private String linkType;
    @JsonProperty("speed-value")
    private String speedValue;
    @JsonProperty("speed-units")
    private String speedUnits;
    @JsonProperty("ip-version")
    private String ipVersion;
    @JsonProperty("routing-protocol")
    private String routingProtocol;
    @JsonProperty("resource-version")
    private String resourceVersion;
    @JsonProperty("model-invariant-id")
    private String modelInvariantId;
    @JsonProperty("model-version-id")
    private String modelVersionId;
    @JsonProperty("widget-model-id")
    private String widgetModelId;
    @JsonProperty("widget-model-version")
    private String widgetModelVersion;
    @JsonProperty("operational-status")
    private String operationalStatus;
    @JsonProperty("prov-status")
    private String provStatus;
    @JsonProperty("link-role")
    private String linkRole;
    @JsonProperty("link-name2")
    private String linkName2;
    @JsonProperty("link-id")
    private String linkId;
    @JsonProperty("circuit-id")
    private String circuitId;
    @JsonProperty("purpose")
    private String purpose;

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String name) {
        this.linkName = name;
    }

    public String getInMaint() {
        return inMaint;
    }

    public void setInMaint(String inMaint) {
        this.inMaint = inMaint;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setlinkType(String type) {
        this.linkType = type;
    }

    public String getSpeedValue() {
        return speedValue;
    }

    public void setSpeedValue(String value) {
        this.speedValue = value;
    }

    public String getSpeedUnits() {
        return speedUnits;
    }

    public void setSpeedUnits(String units) {
        this.speedUnits = units;
    }

    public String getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(String type) {
        this.ipVersion = type;
    }

    public String getRoutingProtocol() {
        return routingProtocol;
    }

    public void setRoutingProtocol(String protocol) {
        this.routingProtocol = protocol;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String version) {
        this.resourceVersion = version;
    }

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public void setModelInvariantId(String id) {
        this.modelInvariantId = id;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public void setModelVersionId(String id) {
        this.modelVersionId = id;
    }

    public String getWidgetModelId() {
        return widgetModelId;
    }

    public void setWidgetModelId(String id) {
        this.widgetModelId = id;
    }

    public String getWidgetModelVersion() {
        return widgetModelVersion;
    }

    public void setWidgetModelVersion(String version) {
        this.widgetModelVersion = version;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String status) {
        this.operationalStatus = status;
    }

    public String getProvStatus() {
        return provStatus;
    }

    public void setProvStatus(String status) {
        this.provStatus = status;
    }

    public String getLinkRole() {
        return linkRole;
    }

    public void setLinkRole(String role) {
        this.linkRole = role;
    }

    public String getLinkName2() {
        return linkName2;
    }

    public void setLinkName2(String name) {
        this.linkName2 = name;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String id) {
        this.linkId = id;
    }

    public String getCircuitId() {
        return circuitId;
    }

    public void setCircuitId(String id) {
        this.circuitId = id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String name) {
        this.purpose = name;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((linkName == null) ? 0 : linkName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogicalLink other = (LogicalLink) obj;
        if (linkName == null) {
            if (other.linkName != null)
                return false;
        } else if (!linkName.equals(other.linkName))
            return false;
        return true;
    }


}
