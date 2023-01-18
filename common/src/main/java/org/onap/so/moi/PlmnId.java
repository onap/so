package org.onap.so.moi;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"mcc", "mnc"})
public class PlmnId {

    @JsonProperty("mcc")
    private Integer mcc;
    @JsonProperty("mnc")
    private Integer mnc;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("mcc")
    public Integer getMcc() {
        return mcc;
    }

    @JsonProperty("mcc")
    public void setMcc(Integer mcc) {
        this.mcc = mcc;
    }

    @JsonProperty("mnc")
    public Integer getMnc() {
        return mnc;
    }

    @JsonProperty("mnc")
    public void setMnc(Integer mnc) {
        this.mnc = mnc;
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
