/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Bell Canada. All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openpojo.business.annotation.BusinessKey;
import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@Entity
@RemoteResource("/buildingBlockRollback")
@Table(name = "building_block_rollback")
public class BuildingBlockRollback implements Serializable {

    private static final long serialVersionUID = 1;

    @Id
    @BusinessKey
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @BusinessKey
    @JsonProperty("building_block_name")
    @Column(name = "BUILDING_BLOCK_NAME", nullable = false, length = 200)
    private String buildingBlockName;

    @BusinessKey
    @JsonProperty("action")
    @Column(name = "ACTION", length = 200)
    private String action;

    @BusinessKey
    @JsonProperty("rollback_building_block_name")
    @Column(name = "ROLLBACK_BUILDING_BLOCK_NAME", nullable = false, length = 200)
    private String rollbackBuildingBlockName;

    @BusinessKey
    @JsonProperty("rollback_action")
    @Column(name = "ROLLBACK_ACTION", length = 200)
    private String rollbackAction;

    public BuildingBlockRollback() {}

    public BuildingBlockRollback(Integer id, String buildingBlockName, String action, String rollbackBuildingBlockName,
            String rollbackAction) {
        this.id = id;
        this.buildingBlockName = buildingBlockName;
        this.action = action;
        this.rollbackBuildingBlockName = rollbackBuildingBlockName;
        this.rollbackAction = rollbackAction;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRollbackBuildingBlockName() {
        return rollbackBuildingBlockName;
    }

    public void setRollbackBuildingBlockName(String rollbackBuildingBlockName) {
        this.rollbackBuildingBlockName = rollbackBuildingBlockName;
    }

    public String getRollbackAction() {
        return rollbackAction;
    }

    public void setRollbackAction(String rollbackAction) {
        this.rollbackAction = rollbackAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BuildingBlockRollback that = (BuildingBlockRollback) o;
        return id.equals(that.id) && buildingBlockName.equals(that.buildingBlockName)
                && Objects.equals(action, that.action)
                && rollbackBuildingBlockName.equals(that.rollbackBuildingBlockName)
                && Objects.equals(rollbackAction, that.rollbackAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, buildingBlockName, action, rollbackBuildingBlockName, rollbackAction);
    }

    @Override
    public String toString() {
        return "BuildingBlockRollback{" + "id='" + id + '\'' + ", buildingBlockName='" + buildingBlockName + '\''
                + ", action='" + action + '\'' + ", rollbackBuildingBlockName='" + rollbackBuildingBlockName + '\''
                + ", rollbackAction='" + rollbackAction + '\'' + '}';
    }
}
