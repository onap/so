package org.onap.so.moi;

import com.fasterxml.jackson.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "operationalState", "administrativeState", "attributes"})
public class GETMoiResponse {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("operationalState")
    private String operationalState = null;

    @JsonProperty("administrativeState")
    private String administrativeState = null;

    @Autowired
    @JsonProperty("attributes")
    private Attributes attributes;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("operationalState")
    public String getOperationalState() {
        return operationalState;
    }

    @JsonProperty("operationalState")
    public void setOperationalState(String operationalState) {
        this.operationalState = operationalState;
    }

    @JsonProperty("administrativeState")
    public String getAdministrativeState() {
        return administrativeState;
    }

    @JsonProperty("administrativeState")
    public void setAdministrativeState(String administrativeState) {
        this.administrativeState = administrativeState;
    }

    @JsonProperty("attributes")
    public Attributes getAttributes() {
        return attributes;
    }

    @JsonProperty("attributes")
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
