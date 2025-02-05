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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "activity_spec_to_activity_spec_categories")
public class ActivitySpecActivitySpecCategories implements Serializable {

    private static final long serialVersionUID = 6228647901909437418L;

    @Id
    @Column(name = "ID", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ID;

    @BusinessKey
    @Column(name = "ACTIVITY_SPEC_ID")
    private Integer activitySpecId;

    @BusinessKey
    @Column(name = "ACTIVITY_SPEC_CATEGORIES_ID")
    private Integer activitySpecCategoriesId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTIVITY_SPEC_ID", updatable = false, insertable = false)
    private ActivitySpec activitySpec;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTIVITY_SPEC_CATEGORIES_ID", updatable = false, insertable = false)
    private ActivitySpecCategories activitySpecCategories;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("activitySpecId", activitySpecId)
                .append("activitySpecCategoriesId", activitySpecCategoriesId).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ActivitySpecActivitySpecCategories)) {
            return false;
        }
        ActivitySpecActivitySpecCategories castOther = (ActivitySpecActivitySpecCategories) other;
        return new EqualsBuilder().append(activitySpecId, castOther.activitySpecId)
                .append(activitySpecCategoriesId, castOther.activitySpecCategoriesId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(activitySpecId).append(activitySpecCategoriesId).toHashCode();
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

    public Integer getActivitySpecCategoriesId() {
        return activitySpecCategoriesId;
    }

    public void setActivitySpecCategoriesId(Integer activitySpecCategoriesId) {
        this.activitySpecCategoriesId = activitySpecCategoriesId;
    }

    public ActivitySpec getActivitySpec() {
        return activitySpec;
    }

    public void setActivitySpec(ActivitySpec activitySpec) {
        this.activitySpec = activitySpec;
    }

    public ActivitySpecCategories getActivitySpecCategories() {
        return activitySpecCategories;
    }

    public void setActivitySpecCategories(ActivitySpecCategories activitySpecCategories) {
        this.activitySpecCategories = activitySpecCategories;
    }

}
