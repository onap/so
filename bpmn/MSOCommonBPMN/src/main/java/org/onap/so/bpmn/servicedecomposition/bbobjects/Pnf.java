/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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
import javax.persistence.Id;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoPnf;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import org.onap.so.bpmn.servicedecomposition.ShallowCopy;

@JsonRootName("pnf")
public class Pnf implements Serializable, ShallowCopy<Pnf> {

    private static final long serialVersionUID = -2544848120774529501L;

    @Id
    @JsonProperty("pnf-id")
    private String pnfId;

    @JsonProperty("pnf-name")
    private String pnfName;

    @JsonProperty("nf-role")
    private String role;

    @JsonProperty("orchestration-status")
    private OrchestrationStatus orchestrationStatus;

    @JsonProperty("cloud-region")
    private CloudRegion cloudRegion;

    @JsonProperty("in-maint")
    private Boolean inMaint;

    @JsonProperty("model-info-pnf")
    private ModelInfoPnf modelInfoPnf;

    public String getPnfId() {
        return pnfId;
    }

    public void setPnfId(String pnfId) {
        this.pnfId = pnfId;
    }

    public String getPnfName() {
        return pnfName;
    }

    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

    /**
     * Distinguishes Primary or Secondary
     */
    public String getRole() {
        return role;
    }

    /**
     * Distinguishes Primary or Secondary
     */
    public void setRole(String role) {
        this.role = role;
    }

    public OrchestrationStatus getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public void setOrchestrationStatus(OrchestrationStatus orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public Boolean isInMaint() {
        return inMaint;
    }

    public void setInMaint(Boolean inMaint) {
        this.inMaint = inMaint;
    }

    public CloudRegion getCloudRegion() {
        return cloudRegion;
    }

    public void setCloudRegion(CloudRegion cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    public ModelInfoPnf getModelInfoPnf() {
        return modelInfoPnf;
    }

    public void setModelInfoPnf(ModelInfoPnf modelInfoPnf) {
        this.modelInfoPnf = modelInfoPnf;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Pnf)) {
            return false;
        }
        Pnf castOther = (Pnf) other;
        return new EqualsBuilder().append(pnfId, castOther.pnfId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pnfId).toHashCode();
    }
}
