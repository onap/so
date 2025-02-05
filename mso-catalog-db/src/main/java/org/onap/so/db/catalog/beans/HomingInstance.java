/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018. Intel Corp. All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openpojo.business.annotation.BusinessKey;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.net.URI;

/**
 * EntityBean class for a HomingInstance. This bean represents a homing instance of a service, populated on successful
 * homing
 *
 */
@Entity
@Table(name = "homing_instances")
public class HomingInstance {
    @JsonProperty
    @BusinessKey
    @Id
    @Column(name = "SERVICE_INSTANCE_ID")
    private String serviceInstanceId;

    @JsonProperty("cloud_region_id")
    @BusinessKey
    @Column(name = "CLOUD_REGION_ID")
    private String cloudRegionId;

    @JsonProperty("cloud_owner")
    @BusinessKey
    @Column(name = "CLOUD_OWNER")
    private String cloudOwner;



    @JsonProperty("oof_directives")
    @BusinessKey
    @Column(name = "OOF_DIRECTIVES", columnDefinition = "LONGTEXT")
    private String oofDirectives;

    @Transient
    private URI uri;

    public HomingInstance() {

    }

    public HomingInstance(HomingInstance homingInstance) {
        this.serviceInstanceId = homingInstance.getServiceInstanceId();
        this.cloudRegionId = homingInstance.getCloudRegionId();
        this.cloudOwner = homingInstance.getCloudOwner();
        this.oofDirectives = homingInstance.getOofDirectives();
    }


    public String getServiceInstanceId() {
        return this.serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {

        this.serviceInstanceId = serviceInstanceId;
    }

    public String getCloudRegionId() {

        return this.cloudRegionId;
    }

    public void setCloudRegionId(String cloudRegionId) {

        this.cloudRegionId = cloudRegionId;
    }

    public String getCloudOwner() {

        return this.cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {

        this.cloudOwner = cloudOwner;
    }

    public String getOofDirectives() {
        return oofDirectives;
    }

    public void setOofDirectives(String oofDirectives) {
        this.oofDirectives = oofDirectives;
    }

    public URI getUri() {
        return this.uri;
    }

    public void setUri(URI uri) {

        this.uri = uri;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("serviceInstanceId", serviceInstanceId)
                .append("cloudRegionId", cloudRegionId).append("cloudOwner", cloudOwner)
                .append("oofDirectives", oofDirectives).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HomingInstance)) {
            return false;
        }
        HomingInstance castOther = (HomingInstance) other;
        return new EqualsBuilder().append(serviceInstanceId, castOther.serviceInstanceId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceInstanceId).toHashCode();
    }
}
