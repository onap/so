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
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;

public class CloudRegion implements Serializable, ShallowCopy<CloudRegion> {

    private static final long serialVersionUID = 680593168655326021L;

    @Id
    @JsonProperty("lcp-cloud-region-id")
    private String lcpCloudRegionId;
    @Id
    @JsonProperty("cloud-owner")
    private String cloudOwner;
    @JsonProperty("tenant-id")
    private String tenantId;
    @JsonProperty("complex")
    private String complex;
    @JsonProperty("cloud-region-version")
    private String cloudRegionVersion;
    @JsonProperty("orchestration-disabled")
    private Boolean orchestrationDisabled;

    public String getLcpCloudRegionId() {
        return lcpCloudRegionId;
    }

    public void setLcpCloudRegionId(String lcpCloudRegionId) {
        this.lcpCloudRegionId = lcpCloudRegionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCloudOwner() {
        return cloudOwner;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    /**
     * i.e. aic version
     */
    public String getCloudRegionVersion() {
        return cloudRegionVersion;
    }

    /**
     * i.e. aic version
     */
    public void setCloudRegionVersion(String cloudRegionVersion) {
        this.cloudRegionVersion = cloudRegionVersion;
    }

    /**
     * i.e. aic clli, physical location id, site id
     */
    public String getComplex() {
        return complex;
    }

    /**
     * i.e. aic clli, physical location id, site id
     */
    public void setComplex(String complex) {
        this.complex = complex;
    }

    public Boolean getOrchestrationDisabled() {
        return orchestrationDisabled;
    }

    public void setOrchestrationDisabled(Boolean orchestrationDisabled) {
        this.orchestrationDisabled = orchestrationDisabled;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CloudRegion)) {
            return false;
        }
        CloudRegion castOther = (CloudRegion) other;
        return new EqualsBuilder().append(lcpCloudRegionId, castOther.lcpCloudRegionId)
                .append(cloudOwner, castOther.cloudOwner).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lcpCloudRegionId).append(cloudOwner).toHashCode();
    }
}
