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
import org.onap.so.bpmn.servicedecomposition.generalobjects.License;
import org.onap.so.bpmn.servicedecomposition.homingobjects.SolutionInfo;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("generic-vnf")
public class GenericVnf implements Serializable, ShallowCopy<GenericVnf> {

    private static final long serialVersionUID = -5107610336831330403L;

    @Id
    @JsonProperty("vnf-id")
    private String vnfId;
    @JsonProperty("vnf-name")
    private String vnfName;
    @JsonProperty("vnf-type")
    private String vnfType;
    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;
    @JsonProperty("vf-modules")
    private List<VfModule> vfModules = new ArrayList<>();
    @JsonProperty("volume-groups")
    private List<VolumeGroup> volumeGroups = new ArrayList<>();
    @JsonProperty("line-of-business")
    private LineOfBusiness lineOfBusiness;
    @JsonProperty("platform")
    private Platform platform;
    @JsonProperty("cascaded")
    private Boolean cascaded;
    @JsonProperty("cloud-params")
    private Map<String, String> cloudParams = new HashMap<>();
    @JsonProperty("cloud-context")
    private CloudRegion cloudRegion;
    @JsonProperty("solution")
    private SolutionInfo solution;
    @JsonProperty("vnf-name-2")
    private String vnfName2;
    @JsonProperty("service-id")
    private String serviceId;
    @JsonProperty("regional-resource-zone")
    private String regionalResourceZone;
    @JsonProperty("prov-status")
    private String provStatus;
    @JsonProperty("operational-status")
    private String operationalStatus;
    @JsonProperty("equipment-role")
    private String equipmentRole;
    @JsonProperty("management-option")
    private String managementOption;
    @JsonProperty("ipv4-oam-address")
    private String ipv4OamAddress;
    @JsonProperty("ipv4-loopback0-address")
    private String ipv4Loopback0Address;
    @JsonProperty("nm-lan-v6-address")
    private String nmLanV6Address;
    @JsonProperty("management-v6-address")
    private String managementV6Address;
    @JsonProperty("vcpu")
    private Long vcpu;
    @JsonProperty("vcpu-units")
    private String vcpuUnits;
    @JsonProperty("vmemory")
    private Long vmemory;
    @JsonProperty("vmemory-units")
    private String vmemoryUnits;
    @JsonProperty("vdisk")
    private Long vdisk;
    @JsonProperty("vdisk-units")
    private String vdiskUnits;
    @JsonProperty("in-maint")
    private Boolean inMaint;
    @JsonProperty("is-closed-loop-disabled")
    private Boolean isClosedLoopDisabled;
    @JsonProperty("summary-status")
    private String summaryStatus;
    @JsonProperty("encrypted-access-flag")
    private Boolean encryptedAccessFlag;
    @JsonProperty("as-number")
    private String asNumber;
    @JsonProperty("regional-resource-subzone")
    private String regionalResourceSubzone;
    @JsonProperty("self-link")
    private String selflink;
    @JsonProperty("ipv4-oam-gateway-address")
    private String ipv4OamGatewayAddress;
    @JsonProperty("ipv4-oam-gateway-address-prefix-length")
    private Integer ipv4OamGatewayAddressPrefixLength;
    @JsonProperty("vlan-id-outer")
    private Long vlanIdOuter;
    @JsonProperty("nm-profile-name")
    private String nmProfileName;
    @JsonProperty("l-interfaces")
    private List<LInterface> lInterfaces = new ArrayList<>();
    @JsonProperty("lag-interfaces")
    private List<LagInterface> lagInterfaces = new ArrayList<>();
    @JsonProperty("license")
    private License license;
    @JsonProperty("entitlements") // TODO remove, duplicated
    private List<Entitlement> entitlements = new ArrayList<>();
    @JsonProperty("model-info-generic-vnf")
    private ModelInfoGenericVnf modelInfoGenericVnf;
    @JsonProperty("instance-groups")
    private List<InstanceGroup> instanceGroups = new ArrayList<>();
    @JsonProperty("call-homing")
    private Boolean callHoming;
    @JsonProperty("nf-function")
    private String nfFunction;
    @JsonProperty("nf-role")
    private String nfRole;
    @JsonProperty("CDS_BLUEPRINT_NAME")
    private String blueprintName;
    @JsonProperty("CDS_BLUEPRINT_VERSION")
    private String blueprintVersion;
    @JsonProperty("application-id")
    private String applicationId;


    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public String getBlueprintVersion() {
        return blueprintVersion;
    }

    public void setBlueprintVersion(String blueprintVersion) {
        this.blueprintVersion = blueprintVersion;
    }

    public String getNfFunction() {
        return nfFunction;
    }

    public void setNfFunction(String nfFunction) {
        this.nfFunction = nfFunction;
    }

    public String getNfRole() {
        return nfRole;
    }

    public void setNfRole(String nfRole) {
        this.nfRole = nfRole;
    }

    public List<InstanceGroup> getInstanceGroups() {
        return instanceGroups;
    }

    public List<VolumeGroup> getVolumeGroups() {
        return volumeGroups;
    }

    public ModelInfoGenericVnf getModelInfoGenericVnf() {
        return modelInfoGenericVnf;
    }

    public void setModelInfoGenericVnf(ModelInfoGenericVnf modelInfoGenericVnf) {
        this.modelInfoGenericVnf = modelInfoGenericVnf;
    }

    public String getVnfName2() {
        return vnfName2;
    }

    public void setVnfName2(String vnfName2) {
        this.vnfName2 = vnfName2;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getRegionalResourceZone() {
        return regionalResourceZone;
    }

    public void setRegionalResourceZone(String regionalResourceZone) {
        this.regionalResourceZone = regionalResourceZone;
    }

    public String getProvStatus() {
        return provStatus;
    }

    public void setProvStatus(String provStatus) {
        this.provStatus = provStatus;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public String getEquipmentRole() {
        return equipmentRole;
    }

    public void setEquipmentRole(String equipmentRole) {
        this.equipmentRole = equipmentRole;
    }

    public String getManagementOption() {
        return managementOption;
    }

    public void setManagementOption(String managementOption) {
        this.managementOption = managementOption;
    }

    public String getIpv4OamAddress() {
        return ipv4OamAddress;
    }

    public void setIpv4OamAddress(String ipv4OamAddress) {
        this.ipv4OamAddress = ipv4OamAddress;
    }

    public String getIpv4Loopback0Address() {
        return ipv4Loopback0Address;
    }

    public void setIpv4Loopback0Address(String ipv4Loopback0Address) {
        this.ipv4Loopback0Address = ipv4Loopback0Address;
    }

    public String getNmLanV6Address() {
        return nmLanV6Address;
    }

    public void setNmLanV6Address(String nmLanV6Address) {
        this.nmLanV6Address = nmLanV6Address;
    }

    public String getManagementV6Address() {
        return managementV6Address;
    }

    public void setManagementV6Address(String managementV6Address) {
        this.managementV6Address = managementV6Address;
    }

    public Long getVcpu() {
        return vcpu;
    }

    public void setVcpu(Long vcpu) {
        this.vcpu = vcpu;
    }

    public String getVcpuUnits() {
        return vcpuUnits;
    }

    public void setVcpuUnits(String vcpuUnits) {
        this.vcpuUnits = vcpuUnits;
    }

    public List<LInterface> getlInterfaces() {
        return lInterfaces;
    }

    public List<LagInterface> getLagInterfaces() {
        return lagInterfaces;
    }

    public List<Entitlement> getEntitlements() {
        return entitlements;
    }

    public List<VfModule> getVfModules() {
        return vfModules;
    }

    public Long getVmemory() {
        return vmemory;
    }

    public void setVmemory(Long vmemory) {
        this.vmemory = vmemory;
    }

    public String getVmemoryUnits() {
        return vmemoryUnits;
    }

    public void setVmemoryUnits(String vmemoryUnits) {
        this.vmemoryUnits = vmemoryUnits;
    }

    public Long getVdisk() {
        return vdisk;
    }

    public void setVdisk(Long vdisk) {
        this.vdisk = vdisk;
    }

    public String getVdiskUnits() {
        return vdiskUnits;
    }

    public void setVdiskUnits(String vdiskUnits) {
        this.vdiskUnits = vdiskUnits;
    }

    public Boolean isInMaint() {
        return inMaint;
    }

    public void setInMaint(Boolean inMaint) {
        this.inMaint = inMaint;
    }

    public Boolean isIsClosedLoopDisabled() {
        return isClosedLoopDisabled;
    }

    public void setClosedLoopDisabled(Boolean isClosedLoopDisabled) {
        this.isClosedLoopDisabled = isClosedLoopDisabled;
    }

    public String getSummaryStatus() {
        return summaryStatus;
    }

    public void setSummaryStatus(String summaryStatus) {
        this.summaryStatus = summaryStatus;
    }

    public Boolean getEncryptedAccessFlag() {
        return encryptedAccessFlag;
    }

    public void setEncryptedAccessFlag(Boolean encryptedAccessFlag) {
        this.encryptedAccessFlag = encryptedAccessFlag;
    }

    public String getAsNumber() {
        return asNumber;
    }

    public void setAsNumber(String asNumber) {
        this.asNumber = asNumber;
    }

    public String getRegionalResourceSubzone() {
        return regionalResourceSubzone;
    }

    public void setRegionalResourceSubzone(String regionalResourceSubzone) {
        this.regionalResourceSubzone = regionalResourceSubzone;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public String getSelflink() {
        return selflink;
    }

    public void setSelflink(String selflink) {
        this.selflink = selflink;
    }

    public String getIpv4OamGatewayAddress() {
        return ipv4OamGatewayAddress;
    }

    public void setIpv4OamGatewayAddress(String ipv4OamGatewayAddress) {
        this.ipv4OamGatewayAddress = ipv4OamGatewayAddress;
    }

    public Integer getIpv4OamGatewayAddressPrefixLength() {
        return ipv4OamGatewayAddressPrefixLength;
    }

    public void setIpv4OamGatewayAddressPrefixLength(Integer ipv4OamGatewayAddressPrefixLength) {
        this.ipv4OamGatewayAddressPrefixLength = ipv4OamGatewayAddressPrefixLength;
    }

    public Long getVlanIdOuter() {
        return vlanIdOuter;
    }

    public void setVlanIdOuter(Long vlanIdOuter) {
        this.vlanIdOuter = vlanIdOuter;
    }

    public String getNmProfileName() {
        return nmProfileName;
    }

    public void setNmProfileName(String nmProfileName) {
        this.nmProfileName = nmProfileName;
    }

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public String getVnfName() {
        return vnfName;
    }

    public void setVnfName(String vnfName) {
        this.vnfName = vnfName;
    }

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
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

    public Boolean isCascaded() {
        return cascaded;
    }

    public void setCascaded(Boolean cascaded) {
        this.cascaded = cascaded;
    }

    public Map<String, String> getCloudParams() {
        return cloudParams;
    }

    public void setCloudParams(Map<String, String> cloudParams) {
        this.cloudParams = cloudParams;
    }

    public SolutionInfo getSolution() {
        return solution;
    }

    public void setSolution(SolutionInfo solution) {
        this.solution = solution;
    }

    public CloudRegion getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(CloudRegion cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public Boolean isCallHoming() {
        return callHoming;
    }

    public void setCallHoming(Boolean callHoming) {
        this.callHoming = callHoming;
    }


    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof GenericVnf)) {
            return false;
        }
        GenericVnf castOther = (GenericVnf) other;
        return new EqualsBuilder().append(vnfId, castOther.vnfId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vnfId).toHashCode();
    }

}
