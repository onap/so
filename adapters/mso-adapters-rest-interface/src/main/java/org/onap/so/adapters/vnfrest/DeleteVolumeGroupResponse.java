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


import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("deleteVolumeGroupResponse")
@XmlRootElement(name = "deleteVolumeGroupResponse")
public class DeleteVolumeGroupResponse extends VfResponseCommon {
    private Boolean volumeGroupDeleted;

    public DeleteVolumeGroupResponse() {
        super();
    }

    public DeleteVolumeGroupResponse(Boolean volumeGroupDeleted, String messageId) {
        super(messageId);
        this.volumeGroupDeleted = volumeGroupDeleted;
    }

    public Boolean getVolumeGroupDeleted() {
        return volumeGroupDeleted;
    }

    public void setVolumeGroupDeleted(Boolean volumeGroupDeleted) {
        this.volumeGroupDeleted = volumeGroupDeleted;
    }
}
