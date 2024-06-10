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

@JsonRootName("deleteVfModuleResponse")
@XmlRootElement(name = "deleteVfModuleResponse")
public class DeleteVfModuleResponse extends VfResponseCommon {
    private String vnfId;
    private String vfModuleId;
    private Boolean vfModuleDeleted;
    private Map<String, String> vfModuleOutputs = new HashMap<>();

    public DeleteVfModuleResponse() {
        super();
    }

    public DeleteVfModuleResponse(String vnfId, String vfModuleId, Boolean vfModuleDeleted, String messageId,
            Map<String, String> outputs) {
        super(messageId);
        this.vnfId = vnfId;
        this.vfModuleId = vfModuleId;
        this.vfModuleDeleted = vfModuleDeleted;
        this.vfModuleOutputs = outputs;
    }

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    public Boolean getVfModuleDeleted() {
        return vfModuleDeleted;
    }

    public void setVfModuleDeleted(Boolean vfModuleDeleted) {
        this.vfModuleDeleted = vfModuleDeleted;
    }

    public Map<String, String> getVfModuleOutputs() {
        return vfModuleOutputs;
    }

    public void setVfModuleOutputs(Map<String, String> vfModuleOutputs) {
        this.vfModuleOutputs = vfModuleOutputs;
    }
}
