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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.Metadata;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoConfiguration;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;

@JsonRootName("configuration")
public class Configuration implements Serializable, ShallowCopy<Configuration> {
    private static final long serialVersionUID = 4525487672816730299L;

    @Id
    @JsonProperty("configuration-id")
    private String configurationId;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
    @JsonProperty("tunnel-bandwidth")
    private String tunnelBandwidth;
    @JsonProperty("vendor-allowed-max-bandwidth")
    private String vendorAllowedMaxBandwidth;
    @JsonProperty("management-option")
    private String managementOption;
    @JsonProperty("configuration-name")
    private String configurationName;
    @JsonProperty("configuration-type")
    private String configurationType;
    @JsonProperty("configuration-sub-type")
    private String configurationSubType;
    @JsonProperty("operational-status")
    private String operationalStatus;
    @JsonProperty("configuration-selflink")
    private String configurationSelflink;
    @JsonProperty("metadata")
    private Metadata metadata;
    @JsonProperty("forwarder-evcs")
    private List<ForwarderEvc> forwarderEvcs = new ArrayList<>();
    @JsonProperty("evcs")
    private List<Evc> evcs = new ArrayList<>();
    @JsonProperty("vnfc")
    private Vnfc vnfc = new Vnfc();
    @JsonProperty("model-info-configuration")
    private ModelInfoConfiguration modelInfoConfiguration;

    @JsonProperty("related-configuration")
    private Configuration relatedConfiguration;

    @JsonProperty("l3-network")
    private L3Network network;

    @JsonProperty("l-interface")
    private LInterface lInterface;

    @JsonProperty("vpn-binding")
    private VpnBinding vpnBinding;

    public ModelInfoConfiguration getModelInfoConfiguration() {
        return modelInfoConfiguration;
    }

    public void setModelInfoConfiguration(ModelInfoConfiguration modelInfoConfiguration) {
        this.modelInfoConfiguration = modelInfoConfiguration;
    }

    public List<ForwarderEvc> getForwarderEvcs() {
        return forwarderEvcs;
    }

    public Vnfc getVnfc() {
        return vnfc;
    }

    public void setVnfc(Vnfc vnfc) {
        this.vnfc = vnfc;
    }

    public List<Evc> getEvcs() {
        return evcs;
    }

    public String getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
    }

    public String getManagementOption() {
        return managementOption;
    }

    public void setManagementOption(String managementOption) {
        this.managementOption = managementOption;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public String getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    public String getConfigurationSubType() {
        return configurationSubType;
    }

    public void setConfigurationSubType(String configurationSubType) {
        this.configurationSubType = configurationSubType;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public String getConfigurationSelflink() {
        return configurationSelflink;
    }

    public void setConfigurationSelflink(String configurationSelflink) {
        this.configurationSelflink = configurationSelflink;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public String getTunnelBandwidth() {
        return tunnelBandwidth;
    }

    public void setTunnelBandwidth(String tunnelBandwidth) {
        this.tunnelBandwidth = tunnelBandwidth;
    }

    public String getVendorAllowedMaxBandwidth() {
        return vendorAllowedMaxBandwidth;
    }

    public void setVendorAllowedMaxBandwidth(String vendorAllowedMaxBandwidth) {
        this.vendorAllowedMaxBandwidth = vendorAllowedMaxBandwidth;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Configuration getRelatedConfiguration() {
        return relatedConfiguration;
    }

    public void setRelatedConfiguration(Configuration relatedConfiguration) {
        this.relatedConfiguration = relatedConfiguration;
    }

    public void setForwarderEvcs(List<ForwarderEvc> forwarderEvcs) {
        this.forwarderEvcs = forwarderEvcs;
    }

    public void setEvcs(List<Evc> evcs) {
        this.evcs = evcs;
    }

    public L3Network getNetwork() {
        return network;
    }

    public void setNetwork(L3Network network) {
        this.network = network;
    }

    public LInterface getlInterface() {
        return lInterface;
    }

    public void setlInterface(LInterface lInterface) {
        this.lInterface = lInterface;
    }

    public VpnBinding getVpnBinding() {
        return vpnBinding;
    }

    public void setVpnBinding(VpnBinding vpnBinding) {
        this.vpnBinding = vpnBinding;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Configuration)) {
            return false;
        }
        Configuration castOther = (Configuration) other;
        return new EqualsBuilder().append(configurationId, castOther.configurationId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationId).toHashCode();
    }

}
