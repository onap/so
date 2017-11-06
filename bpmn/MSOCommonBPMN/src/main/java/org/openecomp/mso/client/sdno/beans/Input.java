
package org.openecomp.mso.client.sdno.beans;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "request-healthdiagnostic",
    "request-hd-custom"
})
public class Input implements Serializable
{

    @JsonProperty("request-healthdiagnostic")
    private RequestHealthDiagnostic RequestHealthDiagnostic;
    
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 7155546785389227528L;

    @JsonProperty("request-healthdiagnostic")
    public RequestHealthDiagnostic getRequestHealthDiagnostic() {
        return RequestHealthDiagnostic;
    }

    @JsonProperty("request-healthdiagnostic")
    public void setRequestHealthDiagnostic(RequestHealthDiagnostic RequestHealthDiagnostic) {
        this.RequestHealthDiagnostic = RequestHealthDiagnostic;
    }

    public Input withRequestHealthDiagnostic(RequestHealthDiagnostic RequestHealthDiagnostic) {
        this.RequestHealthDiagnostic = RequestHealthDiagnostic;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Input withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
