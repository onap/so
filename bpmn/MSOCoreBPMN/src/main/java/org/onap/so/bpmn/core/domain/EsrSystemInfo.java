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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("esr-system-info")
public class EsrSystemInfo extends JsonWrapper implements Serializable {

    private static final long serialVersionUID = 5629921809747079453L;

    @Id
    @JsonProperty("esr-system-info-id")
    private String esrSystemInfoId;
    @JsonProperty("system-name")
    private String systemName;
    @JsonProperty("type")
    private String type;
    @JsonProperty("vendor")
    private String vendor;
    @JsonProperty("version")
    private String version;
    @JsonProperty("service-url")
    private String serviceUrl;
    @JsonProperty("user-name")
    private String userName;
    @JsonProperty("password")
    private String password;
    @JsonProperty("system-type")
    private String systemType;
    @JsonProperty("protocol")
    private String protocol;
    @JsonProperty("ssl-cacert")
    private String sslCacert;
    @JsonProperty("ssl-insecure")
    private Boolean sslInsecure;
    @JsonProperty("ip-address")
    private String ipAddress;
    @JsonProperty("port")
    private String port;
    @JsonProperty("cloud-domain")
    private String cloudDomain;
    @JsonProperty("default-tenant")
    private String defaultTenant;
    @JsonProperty("passive")
    private Boolean passive;
    @JsonProperty("remote-path")
    private String remotePath;
    @JsonProperty("system-status")
    private String systemStatus;
    @JsonProperty("openstack-region-id")
    private String openStackRegionId;
    @JsonProperty("resource-version")
    private String resourceVersion;

    public String getEsrSystemInfoId() {
        return esrSystemInfoId;
    }

    public void setEsrSystemInfoId(String id) {
        this.esrSystemInfoId = id;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String name) {
        this.systemName = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String val) {
        this.type = val;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String val) {
        this.vendor = val;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String ver) {
        this.version = ver;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String url) {
        this.serviceUrl = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Boolean getSslInsecure() {
        return sslInsecure;
    }

    public void setSslInsecure(boolean sslInsecure) {
        this.sslInsecure = sslInsecure;
    }

    public String getSslCacert() {
        return sslCacert;
    }

    public void setSslCacert(String sslCacert) {
        this.sslCacert = sslCacert;
    }

    public Boolean getPassive() {
        return passive;
    }

    public void getPassive(Boolean passive) {
        this.passive = passive;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getCloudDomain() {
        return cloudDomain;
    }

    public void setCloudDomain(String cloudDomain) {
        this.cloudDomain = cloudDomain;
    }

    public String getDefaultTenant() {
        return defaultTenant;
    }

    public void setDefaultTenant(String defaultTenant) {
        this.defaultTenant = defaultTenant;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }

    public String getOpenStackRegionId() {
        return openStackRegionId;
    }

    public void setOpenStackRegionId(String openStackRegionId) {
        this.openStackRegionId = openStackRegionId;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof EsrSystemInfo)) {
            return false;
        }
        EsrSystemInfo castOther = (EsrSystemInfo) other;
        return new EqualsBuilder().append(esrSystemInfoId, castOther.esrSystemInfoId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(esrSystemInfoId).toHashCode();
    }
}
