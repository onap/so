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

package org.onap.so.adapters.vnfrest;


import java.util.HashMap;
import java.util.Map;
import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonRootName;


@JsonRootName("createVolumeGroupResponse")
@XmlRootElement(name = "createVolumeGroupResponse")
public class CreateVolumeGroupResponse extends VfResponseCommon {
    private String volumeGroupId;
    private String volumeGroupStackId;
    private Boolean volumeGroupCreated;
    private Map<String, String> volumeGroupOutputs = new HashMap<>();
    private VolumeGroupRollback volumeGroupRollback = new VolumeGroupRollback();

    public CreateVolumeGroupResponse() {
        super();
    }

    public CreateVolumeGroupResponse(String volumeGroupId, String volumeGroupStackId, Boolean volumeGroupCreated,
            Map<String, String> volumeGroupOutputs, VolumeGroupRollback volumeGroupRollback, String messageId) {
        super(messageId);
        this.volumeGroupId = volumeGroupId;
        this.volumeGroupStackId = volumeGroupStackId;
        this.volumeGroupCreated = volumeGroupCreated;
        this.volumeGroupOutputs = volumeGroupOutputs;
        this.volumeGroupRollback = volumeGroupRollback;
    }

    public String getVolumeGroupId() {
        return volumeGroupId;
    }

    public void setVolumeGroupId(String volumeGroupId) {
        this.volumeGroupId = volumeGroupId;
    }

    public String getVolumeGroupStackId() {
        return volumeGroupStackId;
    }

    public void setVolumeGroupStackId(String volumeGroupStackId) {
        this.volumeGroupStackId = volumeGroupStackId;
    }

    public Boolean getVolumeGroupCreated() {
        return volumeGroupCreated;
    }

    public void setVolumeGroupCreated(Boolean volumeGroupCreated) {
        this.volumeGroupCreated = volumeGroupCreated;
    }

    public Map<String, String> getVolumeGroupOutputs() {
        return volumeGroupOutputs;
    }

    public void setVolumeGroupOutputs(Map<String, String> volumeGroupOutputs) {
        this.volumeGroupOutputs = volumeGroupOutputs;
    }

    public VolumeGroupRollback getVolumeGroupRollback() {
        return volumeGroupRollback;
    }

    public void setVolumeGroupRollback(VolumeGroupRollback volumeGroupRollback) {
        this.volumeGroupRollback = volumeGroupRollback;
    }
}
