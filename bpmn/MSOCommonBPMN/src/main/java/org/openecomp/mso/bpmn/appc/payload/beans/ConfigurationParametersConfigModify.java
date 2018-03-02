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