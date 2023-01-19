package org.onap.so.moi;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"sst", "sd"})
public class Snssai {

    @JsonProperty("sst")
    private String sst;
    @JsonProperty("sd")
    private String sd;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("sst")
    public String getSst() {
        return sst;
    }

    @JsonProperty("sst")
    public void setSst(String sst) {
        this.sst = sst;
    }

    @JsonProperty("sd")
    public String getSd() {
        return sd;
    }

    @JsonProperty("sd")
    public void setSd(String sd) {
        this.sd = sd;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
