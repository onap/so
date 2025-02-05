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

@JsonRootName("createVfModuleResponse")
@XmlRootElement(name = "createVfModuleResponse")
public class CreateVfModuleResponse extends VfResponseCommon {
    private String vnfId;
    private String vfModuleId;
    private String vfModuleStackId;
    private Boolean vfModuleCreated;
    private Map<String, String> vfModuleOutputs = new HashMap<>();
    private VfModuleRollback rollback = new VfModuleRollback();

    public CreateVfModuleResponse() {
        super();
    }

    public CreateVfModuleResponse(String vnfId, String vfModuleId, String vfModuleStackId, Boolean vfModuleCreated,
            Map<String, String> vfModuleOutputs, VfModuleRollback rollback, String messageId) {
        super(messageId);
        this.vnfId = vnfId;
        this.vfModuleId = vfModuleId;
        this.vfModuleStackId = vfModuleStackId;
        this.vfModuleCreated = vfModuleCreated;
        this.vfModuleOutputs = vfModuleOutputs;
        this.rollback = rollback;
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

    public String getVfModuleStackId() {
        return vfModuleStackId;
    }

    public void setVfModuleStackId(String vfModuleStackId) {
        this.vfModuleStackId = vfModuleStackId;
    }

    public Boolean getVfModuleCreated() {
        return vfModuleCreated;
    }

    public void setVfModuleCreated(Boolean vfModuleCreated) {
        this.vfModuleCreated = vfModuleCreated;
    }

    public Map<String, String> getVfModuleOutputs() {
        return vfModuleOutputs;
    }

    public void setVfModuleOutputs(Map<String, String> vfModuleOutputs) {
        this.vfModuleOutputs = vfModuleOutputs;
    }

    public VfModuleRollback getRollback() {
        return rollback;
    }

    public void setRollback(VfModuleRollback rollback) {
        this.rollback = rollback;
    }
}
