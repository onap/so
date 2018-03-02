package org.openecomp.mso.bpmn.appc.payload.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
" AICIdentity "
})
public class StartStopAction {

@JsonProperty(" AICIdentity ")
private String aICIdentity;

@JsonProperty(" AICIdentity ")
public String getAICIdentity() {
return aICIdentity;
}

@JsonProperty(" AICIdentity ")
public void setAICIdentity(String aICIdentity) {
this.aICIdentity = aICIdentity;
}
}