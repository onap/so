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

package org.onap.so.db.catalog.beans;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
/*
 * import javax.persistence.Column; import javax.persistence.Entity; import javax.persistence.Id; import
 * javax.persistence.PrePersist; import javax.persistence.Table; import javax.persistence.Temporal; import
 * javax.persistence.TemporalType;
 */
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

/**
 * EntityBean class for a Cloudify Manager. This bean represents a Cloudify node through which TOSCA-based VNFs may be
 * deployed. Each CloudSite in the CloudConfig may have a Cloudify Manager for deployments using TOSCA blueprints.
 * Cloudify Managers may support multiple Cloud Sites, but each site will have at most one Cloudify Manager.
 * 
 * This does not replace the ability to use the CloudSite directly via Openstack.
 *
 * @author JC1348
 */
@Entity
@RemoteResource("/cloudifyManager")
@Table(name = "cloudify_managers")
public class CloudifyManager {

    @JsonProperty
    @BusinessKey
    @Id
    @Column(name = "ID")
    private String id;

    @BusinessKey
    @JsonProperty("cloudify_url")
    @Column(name = "CLOUDIFY_URL")
    private String cloudifyUrl;

    @BusinessKey
    @JsonProperty("username")
    @Column(name = "USERNAME")
    private String username;

    @BusinessKey
    @JsonProperty("password")
    @Column(name = "PASSWORD")
    private String password;

    @BusinessKey
    @JsonProperty("version")
    @Column(name = "VERSION")
    private String version;

    @JsonProperty("last_updated_by")
    @BusinessKey
    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @JsonProperty("creation_timestamp")
    @BusinessKey
    @Column(name = "CREATION_TIMESTAMP", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @JsonProperty("update_timestamp")
    @BusinessKey
    @Column(name = "UPDATE_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    public CloudifyManager() {}

    @PrePersist
    protected void onCreate() {
        this.created = new Date();
        this.updated = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCloudifyUrl() {
        return cloudifyUrl;
    }

    public void setCloudifyUrl(String cloudifyUrl) {
        this.cloudifyUrl = cloudifyUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public CloudifyManager clone() {
        CloudifyManager cloudifyManagerCopy = new CloudifyManager();
        cloudifyManagerCopy.id = this.id;
        cloudifyManagerCopy.cloudifyUrl = this.cloudifyUrl;
        cloudifyManagerCopy.username = this.username;
        cloudifyManagerCopy.password = this.password;
        cloudifyManagerCopy.version = this.version;
        return cloudifyManagerCopy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
                .append("cloudifyUrl", getCloudifyUrl()).append("username", getUsername())
                .append("password", getPassword()).append("version", getVersion()).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!getClass().equals(other.getClass())) {
            return false;
        }
        CloudifyManager castOther = (CloudifyManager) other;
        return new EqualsBuilder().append(getId(), castOther.getId())
                .append(getCloudifyUrl(), castOther.getCloudifyUrl()).append(getUsername(), castOther.getUsername())
                .append(getPassword(), castOther.getPassword()).append(getVersion(), castOther.getVersion()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(1, 31).append(getId()).append(getCloudifyUrl()).append(getUsername())
                .append(getPassword()).append(getVersion()).toHashCode();
    }
}
