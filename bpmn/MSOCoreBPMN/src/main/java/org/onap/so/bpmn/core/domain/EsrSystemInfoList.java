/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.core.domain;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("esr-system-info-list")
public class EsrSystemInfoList extends JsonWrapper implements Serializable {

    private static final long serialVersionUID = 5629921809747079454L;

    @JsonProperty("esr-system-info")
    private List<EsrSystemInfo> esrSystemInfo;

    public List<EsrSystemInfo> getEsrSystemInfo() {
        return esrSystemInfo;
    }

    public void setEsrSystemInfo(List<EsrSystemInfo> esrSystemInfo) {
        this.esrSystemInfo = esrSystemInfo;
    }

}
