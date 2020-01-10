package org.onap.so.adapters.appc.orchestrator.client.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"identity-url"})
public class Identity {

    @JsonProperty("identity-url")
    private String identityUrl;

    @JsonProperty("identity-url")
    public String getIdentityUrl() {
        return identityUrl;
    }

    @JsonProperty("identity-url")
    public void setIdentityUrl(String identityUrl) {
        this.identityUrl = identityUrl;
    }

}
