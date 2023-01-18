package org.onap.so.moi;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"sliceProfileList"})
public class Attributes {

    @JsonProperty("sliceProfileList")
    private List<SliceProfile> sliceProfileList = null;

    @JsonProperty("operationalState")
    private String operationalState;

    @JsonProperty("administrativeState")
    private String administrativeState;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("sliceProfileList")
    public List<SliceProfile> getSliceProfileList() {
        return sliceProfileList;
    }

    @JsonProperty("sliceProfileList")
    public void setSliceProfileList(List<SliceProfile> sliceProfileList) {
        this.sliceProfileList = sliceProfileList;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
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
}
