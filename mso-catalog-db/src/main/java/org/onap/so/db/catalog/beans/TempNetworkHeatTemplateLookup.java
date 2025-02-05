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

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "temp_network_heat_template_lookup")
@IdClass(TempNetworkHeatTemplateLookupId.class)
public class TempNetworkHeatTemplateLookup implements Serializable {

    public static final long serialVersionUID = -1322322139926390329L;

    @BusinessKey
    @Id
    @Column(name = "NETWORK_RESOURCE_MODEL_NAME")
    private String networkResourceModelName;

    @BusinessKey
    @Id
    @Column(name = "HEAT_TEMPLATE_ARTIFACT_UUID")
    private String heatTemplateArtifactUuid;

    @Column(name = "AIC_VERSION_MIN")
    private String aicVersionMin;

    @Column(name = "AIC_VERSION_MAX")
    private String aicVersionMax;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof TempNetworkHeatTemplateLookup)) {
            return false;
        }
        TempNetworkHeatTemplateLookup castOther = (TempNetworkHeatTemplateLookup) other;
        return new EqualsBuilder().append(networkResourceModelName, castOther.networkResourceModelName)
                .append(heatTemplateArtifactUuid, castOther.heatTemplateArtifactUuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(networkResourceModelName).append(heatTemplateArtifactUuid).toHashCode();
    }

    public TempNetworkHeatTemplateLookup() {
        super();
    }

    public String getNetworkResourceModelName() {
        return this.networkResourceModelName;
    }

    public void setNetworkResourceModelName(String networkResourceModelName) {
        this.networkResourceModelName = networkResourceModelName;
    }

    public String getHeatTemplateArtifactUuid() {
        return this.heatTemplateArtifactUuid;
    }

    public void setHeatTemplateArtifactUuid(String heatTemplateArtifactUuid) {
        this.heatTemplateArtifactUuid = heatTemplateArtifactUuid;
    }

    public String getAicVersionMin() {
        return this.aicVersionMin;
    }

    public void setAicVersionMin(String aicVersionMin) {
        this.aicVersionMin = aicVersionMin;
    }

    public String getAicVersionMax() {
        return this.aicVersionMax;
    }

    public void setAicVersionMax(String aicVersionMax) {
        this.aicVersionMax = aicVersionMax;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NetworkResourceModelName=" + this.networkResourceModelName);
        sb.append("HeatTemplateArtifactUuid=" + this.heatTemplateArtifactUuid);
        sb.append("aicVersionMin=" + this.aicVersionMin);
        sb.append("aicVersionMax=" + this.aicVersionMax);
        return sb.toString();
    }
}
