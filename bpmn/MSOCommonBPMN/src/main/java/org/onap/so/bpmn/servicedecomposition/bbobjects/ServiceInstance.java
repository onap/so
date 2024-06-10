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
import jakarta.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.Metadata;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;
import org.onap.so.bpmn.servicedecomposition.homingobjects.SolutionInfo;

@JsonRootName("service-instance")
public class ServiceInstance implements Serializable, ShallowCopy<ServiceInstance> {

    private static final long serialVersionUID = -1843348234891739356L;

    @Id
    @JsonProperty("service-instance-id")
    private String serviceInstanceId;
    @JsonProperty("service-instance-name")
    private String serviceInstanceName;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
    @JsonProperty("owning-entity")
    private OwningEntity owningEntity;
    @JsonProperty("project")
    private Project project;
    @JsonProperty("collection")
    private Collection collection;
    @JsonProperty("vnfs")
    private List<GenericVnf> vnfs = new ArrayList<>();
    @JsonProperty("pnfs")
    private List<Pnf> pnfs = new ArrayList<>();
    @JsonProperty("allotted-resources")
    private List<AllottedResource> allottedResources = new ArrayList<>();
    @JsonProperty("networks")
    private List<L3Network> networks = new ArrayList<>();
    @JsonProperty("vpn-bonding-links")
    private List<VpnBondingLink> vpnBondingLinks = new ArrayList<>();
    @JsonProperty("vhn-portal-url")
    private String vhnPortalUrl;
    @JsonProperty("service-instance-location-id")
    private String serviceInstanceLocationId;
    @JsonProperty("selflink")
    private String selflink;
    @JsonProperty("metadata")
    private Metadata metadata;
    @JsonProperty("configurations")
    private List<Configuration> configurations = new ArrayList<>();
    @JsonProperty("solution-info")
    private SolutionInfo solutionInfo;
    @JsonProperty("model-info-service-instance")
    private ModelInfoServiceInstance modelInfoServiceInstance;
    @JsonProperty("instance-groups")
    private List<InstanceGroup> instanceGroups = new ArrayList<>();
    @JsonProperty("service-proxies")
    private List<ServiceProxy> serviceProxies = new ArrayList<>();

    public void setServiceProxies(List<ServiceProxy> serviceProxies) {
        this.serviceProxies = serviceProxies;
    }

    public List<GenericVnf> getVnfs() {
        return vnfs;
    }

    public List<AllottedResource> getAllottedResources() {
        return allottedResources;
    }

    public List<L3Network> getNetworks() {
        return networks;
    }

    public ModelInfoServiceInstance getModelInfoServiceInstance() {
        return modelInfoServiceInstance;
    }

    public void setModelInfoServiceInstance(ModelInfoServiceInstance modelInfoServiceInstance) {
        this.modelInfoServiceInstance = modelInfoServiceInstance;
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    public String getVhnPortalUrl() {
        return vhnPortalUrl;
    }

    public void setVhnPortalUrl(String vhnPortalUrl) {
        this.vhnPortalUrl = vhnPortalUrl;
    }

    public String getServiceInstanceLocationId() {
        return serviceInstanceLocationId;
    }

    public void setServiceInstanceLocationId(String serviceInstanceLocationId) {
        this.serviceInstanceLocationId = serviceInstanceLocationId;
    }

    public String getSelflink() {
        return selflink;
    }

    public void setSelflink(String selflink) {
        this.selflink = selflink;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getServiceInstanceName() {
        return serviceInstanceName;
    }

    public void setServiceInstanceName(String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public OwningEntity getOwningEntity() {
        return owningEntity;
    }

    public void setOwningEntity(OwningEntity owningEntity) {
        this.owningEntity = owningEntity;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public List<VpnBondingLink> getVpnBondingLinks() {
        return vpnBondingLinks;
    }

    public List<Pnf> getPnfs() {
        return pnfs;
    }

    public SolutionInfo getSolutionInfo() {
        return solutionInfo;
    }

    public void setSolutionInfo(SolutionInfo solutionInfo) {
        this.solutionInfo = solutionInfo;
    }

    public List<InstanceGroup> getInstanceGroups() {
        return instanceGroups;
    }

    public void setInstanceGroups(List<InstanceGroup> instanceGroups) {
        this.instanceGroups = instanceGroups;
    }

    public List<ServiceProxy> getServiceProxies() {
        return serviceProxies;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ServiceInstance)) {
            return false;
        }
        ServiceInstance castOther = (ServiceInstance) other;
        return new EqualsBuilder().append(serviceInstanceId, castOther.serviceInstanceId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceInstanceId).toHashCode();
    }
}
