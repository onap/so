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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoNetwork;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("l3Network")
public class L3Network implements Serializable, ShallowCopy<L3Network> {

    private static final long serialVersionUID = 4434492567957111317L;

    @Id
    @JsonProperty("network-id")
    private String networkId;
    @JsonProperty("cascaded")
    private Boolean cascaded;
    @JsonProperty("cloud-params")
    private Map<String, String> cloudParams = new HashMap<>();
    @JsonProperty("network-name")
    private String networkName;
    @JsonProperty("neutron-network-id")
    private String neutronNetworkId;
    @JsonProperty("network-type")
    private String networkType;
    @JsonProperty("network-technology")
    private String networkTechnology;
    @JsonProperty("network-role")
    private String networkRole;
    @JsonProperty("is-bound-to-vpn")
    private Boolean isBoundToVpn;
    @JsonProperty("service-id")
    private String serviceId;
    @JsonProperty("network-role-instance")
    private Long networkRoleInstance;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
    @JsonProperty("heat-stack-id")
    private String heatStackId;
    @JsonProperty("contrail-network-fqdn")
    private String contrailNetworkFqdn;
    @JsonProperty("network-policies")
    private List<NetworkPolicy> networkPolicies = new ArrayList<>();
    @JsonProperty("contrail-network-route-table-references")
    private List<RouteTableReference> contrailNetworkRouteTableReferences = new ArrayList<>();
    @JsonProperty("widget-model-id")
    private String widgetModelId;
    @JsonProperty("widget-model-version")
    private String widgetModelVersion;
    @JsonProperty("physical-network-name")
    private String physicalNetworkName;
    @JsonProperty("is-provider-network")
    private Boolean isProviderNetwork;
    @JsonProperty("is-shared-network")
    private Boolean isSharedNetwork;
    @JsonProperty("is-external-network")
    private Boolean isExternalNetwork;
    @JsonProperty("self-link")
    private String selflink;
    @JsonProperty("operational-status")
    private String operationalStatus;
    @JsonProperty("subnets")
    private List<Subnet> subnets = new ArrayList<>();
    @JsonProperty("ctag-assignments")
    private List<CtagAssignment> ctagAssignments = new ArrayList<>();
    @JsonProperty("segmentation-assignments")
    private List<SegmentationAssignment> segmentationAssignments = new ArrayList<>();
    @JsonProperty("model-info-network")
    private ModelInfoNetwork modelInfoNetwork;
    @JsonProperty("aggregate-routes")
    private List<AggregateRoute> aggregateRoutes = new ArrayList<>();
    @JsonProperty("vpn-binding")
    private List<VpnBinding> vpnBindings = new ArrayList<>();
    @JsonProperty("line-of-business")
    private LineOfBusiness lineOfBusiness;
    @JsonProperty("platform")
    private Platform platform;

    public ModelInfoNetwork getModelInfoNetwork() {
        return modelInfoNetwork;
    }

    public void setModelInfoNetwork(ModelInfoNetwork modelInfoNetwork) {
        this.modelInfoNetwork = modelInfoNetwork;
    }

    public String getNeutronNetworkId() {
        return neutronNetworkId;
    }

    public void setNeutronNetworkId(String neutronNetworkId) {
        this.neutronNetworkId = neutronNetworkId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getNetworkTechnology() {
        return networkTechnology;
    }

    public void setNetworkTechnology(String networkTechnology) {
        this.networkTechnology = networkTechnology;
    }

    public String getNetworkRole() {
        return networkRole;
    }

    public void setNetworkRole(String networkRole) {
        this.networkRole = networkRole;
    }

    public Boolean isIsBoundToVpn() {
        return isBoundToVpn;
    }

    public void setIsBoundToVpn(Boolean isBoundToVpn) {
        this.isBoundToVpn = isBoundToVpn;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Long getNetworkRoleInstance() {
        return networkRoleInstance;
    }

    public void setNetworkRoleInstance(Long networkRoleInstance) {
        this.networkRoleInstance = networkRoleInstance;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public String getHeatStackId() {
        return heatStackId;
    }

    public void setHeatStackId(String heatStackId) {
        this.heatStackId = heatStackId;
    }

    public String getContrailNetworkFqdn() {
        return contrailNetworkFqdn;
    }

    public void setContrailNetworkFqdn(String contrailNetworkFqdn) {
        this.contrailNetworkFqdn = contrailNetworkFqdn;
    }

    public List<NetworkPolicy> getNetworkPolicies() {
        return networkPolicies;
    }

    public List<RouteTableReference> getContrailNetworkRouteTableReferences() {
        return contrailNetworkRouteTableReferences;
    }

    public String getWidgetModelId() {
        return widgetModelId;
    }

    public void setWidgetModelId(String widgetModelId) {
        this.widgetModelId = widgetModelId;
    }

    public String getWidgetModelVersion() {
        return widgetModelVersion;
    }

    public void setWidgetModelVersion(String widgetModelVersion) {
        this.widgetModelVersion = widgetModelVersion;
    }

    public String getPhysicalNetworkName() {
        return physicalNetworkName;
    }

    public void setPhysicalNetworkName(String physicalNetworkName) {
        this.physicalNetworkName = physicalNetworkName;
    }

    public Boolean isIsProviderNetwork() {
        return isProviderNetwork;
    }

    public void setIsProviderNetwork(Boolean isProviderNetwork) {
        this.isProviderNetwork = isProviderNetwork;
    }

    public Boolean isIsSharedNetwork() {
        return isSharedNetwork;
    }

    public void setIsSharedNetwork(Boolean isSharedNetwork) {
        this.isSharedNetwork = isSharedNetwork;
    }

    public Boolean isIsExternalNetwork() {
        return isExternalNetwork;
    }

    public void setIsExternalNetwork(Boolean isExternalNetwork) {
        this.isExternalNetwork = isExternalNetwork;
    }

    public String getSelflink() {
        return selflink;
    }

    public void setSelflink(String selflink) {
        this.selflink = selflink;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public List<Subnet> getSubnets() {
        return subnets;
    }

    public List<CtagAssignment> getCtagAssignments() {
        return ctagAssignments;
    }

    public List<SegmentationAssignment> getSegmentationAssignments() {
        return segmentationAssignments;
    }

    public Boolean isCascaded() {
        return cascaded;
    }

    public void setIsCascaded(Boolean cascaded) {
        this.cascaded = cascaded;
    }

    public Map<String, String> getCloudParams() {
        return cloudParams;
    }

    public void setCloudParams(Map<String, String> cloudParams) {
        this.cloudParams = cloudParams;
    }

    public List<AggregateRoute> getAggregateRoutes() {
        return aggregateRoutes;
    }

    public List<VpnBinding> getVpnBindings() {
        return vpnBindings;
    }

    public LineOfBusiness getLineOfBusiness() {
        return lineOfBusiness;
    }

    public void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
        this.lineOfBusiness = lineOfBusiness;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof L3Network)) {
            return false;
        }
        L3Network castOther = (L3Network) other;
        return new EqualsBuilder().append(networkId, castOther.networkId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(networkId).toHashCode();
    }
}
