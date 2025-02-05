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

package org.onap.so.db.catalog.beans;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "activity_spec_to_activity_spec_parameters")
public class ActivitySpecActivitySpecParameters implements Serializable {

    private static final long serialVersionUID = -2036788837696381115L;

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;

    @BusinessKey
    @Column(name = "ACTIVITY_SPEC_ID")
    private Integer activitySpecId;

    @BusinessKey
    @Column(name = "ACTIVITY_SPEC_PARAMETERS_ID")
    private Integer activitySpecParametersId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTIVITY_SPEC_ID", updatable = false, insertable = false)
    private ActivitySpec activitySpec;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTIVITY_SPEC_PARAMETERS_ID", updatable = false, insertable = false)
    private ActivitySpecParameters activitySpecParameters;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("activitySpecId", activitySpecId)
                .append("activitySpecParametersId", activitySpecParametersId).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ActivitySpecActivitySpecParameters)) {
            return false;
        }
        ActivitySpecActivitySpecParameters castOther = (ActivitySpecActivitySpecParameters) other;
        return new EqualsBuilder().append(activitySpecId, castOther.activitySpecId)
                .append(activitySpecParametersId, castOther.activitySpecParametersId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(activitySpecId).append(activitySpecParametersId).toHashCode();
    }

    public Integer getID() {
        return ID;
    }

    public Integer getActivitySpecId() {
        return activitySpecId;
    }

    public void setActivitySpecId(Integer activitySpecId) {
        this.activitySpecId = activitySpecId;
    }

    public Integer getActivitySpecParametersId() {
        return activitySpecParametersId;
    }

    public void setActivitySpecParametersId(Integer activitySpecParametersId) {
        this.activitySpecParametersId = activitySpecParametersId;
    }

    public ActivitySpec getActivitySpec() {
        return activitySpec;
    }

    public void setActivitySpec(ActivitySpec activitySpec) {
        this.activitySpec = activitySpec;
    }

    public ActivitySpecParameters getActivitySpecParameters() {
        return activitySpecParameters;
    }

    public void setActivitySpecParameters(ActivitySpecParameters activitySpecParameters) {
        this.activitySpecParameters = activitySpecParameters;
    }

}
