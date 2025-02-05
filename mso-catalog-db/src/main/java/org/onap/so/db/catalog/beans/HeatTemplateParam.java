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
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@IdClass(HeatTemplateParamId.class)
@Entity
@Table(name = "heat_template_params")
public class HeatTemplateParam implements Serializable {

    @BusinessKey
    @Id
    @Column(name = "HEAT_TEMPLATE_ARTIFACT_UUID")
    private String heatTemplateArtifactUuid;

    @BusinessKey
    @Id
    @Column(name = "PARAM_NAME")
    private String paramName;

    @Column(name = "IS_REQUIRED")
    private Boolean required;

    @Column(name = "PARAM_TYPE")
    private String paramType;

    @Column(name = "PARAM_ALIAS")
    private String paramAlias;

    public static final long serialVersionUID = -1322322139926390329L;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("heatTemplateArtifactUuid", heatTemplateArtifactUuid)
                .append("paramName", paramName).append("required", required).append("paramType", paramType)
                .append("paramAlias", paramAlias).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HeatTemplateParam)) {
            return false;
        }
        HeatTemplateParam castOther = (HeatTemplateParam) other;
        return new EqualsBuilder().append(heatTemplateArtifactUuid, castOther.heatTemplateArtifactUuid)
                .append(paramName, castOther.paramName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(heatTemplateArtifactUuid).append(paramName).toHashCode();
    }

    public String getHeatTemplateArtifactUuid() {
        return this.heatTemplateArtifactUuid;
    }

    public void setHeatTemplateArtifactUuid(String heatTemplateArtifactUuid) {
        this.heatTemplateArtifactUuid = heatTemplateArtifactUuid;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public Boolean isRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getParamAlias() {
        return paramAlias;
    }

    public void setParamAlias(String paramAlias) {
        this.paramAlias = paramAlias;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }
}
