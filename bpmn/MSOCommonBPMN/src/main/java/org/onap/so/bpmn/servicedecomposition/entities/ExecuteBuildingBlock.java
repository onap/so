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

package org.onap.so.bpmn.servicedecomposition.entities;

import java.io.Serializable;

public class ExecuteBuildingBlock extends BuildingBlockBase<ExecuteBuildingBlock> implements Serializable {

    private BuildingBlock buildingBlock;
    private ConfigurationResourceKeys configurationResourceKeys;
    private Boolean homing = false;
    private String oldVolumeGroupName;
    private static final long serialVersionUID = 3L;

    public BuildingBlock getBuildingBlock() {
        return buildingBlock;
    }

    public ExecuteBuildingBlock setBuildingBlock(BuildingBlock buildingBlock) {
        this.buildingBlock = buildingBlock;
        return this;
    }

    public Boolean isHoming() {
        return homing;
    }

    public ExecuteBuildingBlock setHoming(Boolean homing) {
        this.homing = homing;
        return this;
    }

    public ConfigurationResourceKeys getConfigurationResourceKeys() {
        return configurationResourceKeys;
    }

    public ExecuteBuildingBlock setConfigurationResourceKeys(ConfigurationResourceKeys configurationResourceKeys) {
        this.configurationResourceKeys = configurationResourceKeys;
        return this;
    }

    public String getOldVolumeGroupName() {
        return oldVolumeGroupName;
    }

    public ExecuteBuildingBlock setOldVolumeGroupName(String oldVolumeGroupName) {
        this.oldVolumeGroupName = oldVolumeGroupName;
        return this;
    }
}
