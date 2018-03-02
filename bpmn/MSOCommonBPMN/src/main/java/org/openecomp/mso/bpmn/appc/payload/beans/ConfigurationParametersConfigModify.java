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
"node0_hostname",
"node0_backup_router_address"
})
public class ConfigurationParametersConfigModify {

@JsonProperty("node0_hostname")
private String node0Hostname;
@JsonProperty("node0_backup_router_address")
private String node0BackupRouterAddress;

@JsonProperty("node0_hostname")
public String getNode0Hostname() {
return node0Hostname;
}

@JsonProperty("node0_hostname")
public void setNode0Hostname(String node0Hostname) {
this.node0Hostname = node0Hostname;
}

@JsonProperty("node0_backup_router_address")
public String getNode0BackupRouterAddress() {
return node0BackupRouterAddress;
}

@JsonProperty("node0_backup_router_address")
public void setNode0BackupRouterAddress(String node0BackupRouterAddress) {
this.node0BackupRouterAddress = node0BackupRouterAddress;
}
}