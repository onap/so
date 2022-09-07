/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.servicedecomposition.modelinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class ModelInfoPnf extends ModelInfoMetadata implements Serializable {
    private static final long serialVersionUID = 50687109134317615L;

    @JsonProperty("nf-role")
    private String role;

    @JsonProperty("nf-type")
    private String NfType;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNfType() {
        return NfType;
    }

    public void setNfType(String nfType) {
        NfType = nfType;
    }
}
