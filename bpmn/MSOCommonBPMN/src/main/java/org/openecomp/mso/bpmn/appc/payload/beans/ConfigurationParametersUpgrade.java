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

package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"vnf_name",
"existing-software-version",
"new-software-version"
})
public class ConfigurationParametersUpgrade {
@JsonProperty("vnf_name")
private String vnfName;
@JsonProperty("existing-software-version")
private String existingSoftwareVersion;
@JsonProperty("new-software-version")
private String newSoftwareVersion;

@JsonProperty("vnf_name")
public String getVnfName() {
return vnfName;
}

@JsonProperty("vnf_name")
public void setVnfName(String vnfName) {
this.vnfName = vnfName;
}

@JsonProperty("existing-software-version")
public String getExistingSoftwareVersion() {
return existingSoftwareVersion;
}

@JsonProperty("existing-software-version")
public void setExistingSoftwareVersion(String existingSoftwareVersion) {
this.existingSoftwareVersion = existingSoftwareVersion;
}

@JsonProperty("new-software-version")
public String getNewSoftwareVersion() {
return newSoftwareVersion;
}

@JsonProperty("new-software-version")
public void setNewSoftwareVersion(String newSoftwareVersion) {
this.newSoftwareVersion = newSoftwareVersion;
}

}