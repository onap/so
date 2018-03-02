package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"vm-id",
"identity-url"
})
public class SnapshotAction {

@JsonProperty("vm-id")
private String vmId;
@JsonProperty("identity-url")
private String identityUrl;

@JsonProperty("vm-id")
public String getVmId() {
return vmId;
}

@JsonProperty("vm-id")
public void setVmId(String vmId) {
this.vmId = vmId;
}

@JsonProperty("identity-url")
public String getIdentityUrl() {
return identityUrl;
}

@JsonProperty("identity-url")
public void setIdentityUrl(String identityUrl) {
this.identityUrl = identityUrl;
}

}