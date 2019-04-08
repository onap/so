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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.openpojo.business.annotation.BusinessKey;

public class ActivitySpecActivitySpecCategoriesId implements Serializable {
	
	private static final long serialVersionUID = 1563771827209840959L;
	private Integer ID;
	@BusinessKey
	private Integer activitySpecId;
	@BusinessKey
	private Integer activitySpecCategoriesId;
	
	public Integer getID() {
		return ID;
	}
	public void setID(Integer iD) {
		ID = iD;
	}
	public Integer getActivitySpecCategoriesId() {
		return activitySpecCategoriesId;
	}
	public void setActivitySpecCategoriesId(Integer activitySpecCategoriesId) {
		this.activitySpecCategoriesId = activitySpecCategoriesId;
	}
	public Integer getActivitySpecId() {
		return activitySpecId;
	}
	public void setActivitySpecId(Integer activitySpecId) {
		this.activitySpecId = activitySpecId;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("activitySpecId", activitySpecId)
				.append("activitySpecCategoriesId", activitySpecCategoriesId)
				.toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ActivitySpecActivitySpecCategoriesId)) {
			return false;
		}
		ActivitySpecActivitySpecCategoriesId castOther = (ActivitySpecActivitySpecCategoriesId) other;
		return new EqualsBuilder().append(activitySpecId, castOther.activitySpecId)
				.append(activitySpecCategoriesId, castOther.activitySpecCategoriesId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(activitySpecId).append(activitySpecCategoriesId).toHashCode();
	}
}
