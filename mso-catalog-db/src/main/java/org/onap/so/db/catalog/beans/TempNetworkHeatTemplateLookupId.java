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

public class TempNetworkHeatTemplateLookupId implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3660507980620002091L;
    @BusinessKey
    private String networkResourceModelName;
    @BusinessKey
    private String heatTemplateArtifactUuid;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("networkResourceModelName", networkResourceModelName)
                .append("heatTemplateArtifactUuid", heatTemplateArtifactUuid).toString();
    }

    public String getNetworkResourceModelName() {
        return networkResourceModelName;
    }

    public void setNetworkResourceModelName(String networkResourceModelName) {
        this.networkResourceModelName = networkResourceModelName;
    }

    public String getHeatTemplateArtifactUuid() {
        return heatTemplateArtifactUuid;
    }

    public void setHeatTemplateArtifactUuid(String heatTemplateArtifactUuid) {
        this.heatTemplateArtifactUuid = heatTemplateArtifactUuid;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof TempNetworkHeatTemplateLookupId)) {
            return false;
        }
        TempNetworkHeatTemplateLookupId castOther = (TempNetworkHeatTemplateLookupId) other;
        return new EqualsBuilder().append(networkResourceModelName, castOther.networkResourceModelName)
                .append(heatTemplateArtifactUuid, castOther.heatTemplateArtifactUuid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(networkResourceModelName).append(heatTemplateArtifactUuid).toHashCode();
    }
}
