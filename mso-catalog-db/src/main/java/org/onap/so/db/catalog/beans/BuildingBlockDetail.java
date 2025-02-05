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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import com.openpojo.business.annotation.BusinessKey;

@Entity
@Table(name = "building_block_detail")
public class BuildingBlockDetail implements Serializable {
    private static final long serialVersionUID = -2375223199178059155L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @BusinessKey
    @Column(name = "BUILDING_BLOCK_NAME")
    private String buildingBlockName;

    @Enumerated(EnumType.STRING)
    @Column(name = "RESOURCE_TYPE")
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "TARGET_ACTION")
    private OrchestrationAction targetAction;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("buildingBlockName", buildingBlockName)
                .append("resourceType", resourceType).append("targetAction", targetAction).toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof BuildingBlockDetail)) {
            return false;
        }
        BuildingBlockDetail castOther = (BuildingBlockDetail) other;
        return new EqualsBuilder().append(buildingBlockName, castOther.buildingBlockName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(buildingBlockName).toHashCode();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBuildingBlockName() {
        return buildingBlockName;
    }

    public void setBuildingBlockName(String buildingBlockName) {
        this.buildingBlockName = buildingBlockName;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public OrchestrationAction getTargetAction() {
        return targetAction;
    }

    public void setTargetAction(OrchestrationAction targetAction) {
        this.targetAction = targetAction;
    }
}
