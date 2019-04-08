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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

public class HeatTemplateParamId implements Serializable {
    @BusinessKey
    private String heatTemplateArtifactUuid;
    @BusinessKey
    private String paramName;

    public static final long serialVersionUID = -1322322139926390329L;

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof HeatTemplateParamId)) {
            return false;
        }
        HeatTemplateParamId castOther = (HeatTemplateParamId) other;
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
        return this.paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("heatTemplateArtifactUuid", heatTemplateArtifactUuid)
                .append("paramName", paramName).toString();
    }
}
