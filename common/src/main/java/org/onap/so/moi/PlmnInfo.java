package org.onap.so.moi;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"plmnId", "snssai"})
public class PlmnInfo {

    @JsonProperty("plmnId")
    private PlmnId plmnId;
    @JsonProperty("snssai")
    private Snssai snssai;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("plmnId")
    public PlmnId getPlmnId() {
        return plmnId;
    }

    @JsonProperty("plmnId")
    public void setPlmnId(PlmnId plmnId) {
        this.plmnId = plmnId;
    }

    @JsonProperty("snssai")
    public Snssai getSnssai() {
        return snssai;
    }

    @JsonProperty("snssai")
    public void setSnssai(Snssai snssai) {
        this.snssai = snssai;
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
