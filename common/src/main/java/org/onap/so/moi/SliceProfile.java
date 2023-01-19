package org.onap.so.moi;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"sliceProfileId", "plmnInfoList", "RANSliceSubnetProfile"})
public class SliceProfile {

    @JsonProperty("sliceProfileId")
    private String sliceProfileId = null;

    @JsonProperty("plmnInfoList")
    private List<PlmnInfo> plmnInfoList = null;

    @JsonProperty("RANSliceSubnetProfile")
    private RANSliceSubnetProfile rANSliceSubnetProfile;

    @JsonProperty("sliceProfileId")
    public String getSliceProfileId() {
        return sliceProfileId;
    }

    @JsonProperty("sliceProfileId")
    public void setSliceProfileId(String sliceProfileId) {
        this.sliceProfileId = sliceProfileId;
    }

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("plmnInfoList")
    public List<PlmnInfo> getPlmnInfoList() {
        return plmnInfoList;
    }

    @JsonProperty("plmnInfoList")
    public void setPlmnInfoList(List<PlmnInfo> plmnInfoList) {
        this.plmnInfoList = plmnInfoList;
    }

    @JsonProperty("RANSliceSubnetProfile")
    public RANSliceSubnetProfile getRANSliceSubnetProfile() {
        return rANSliceSubnetProfile;
    }

    @JsonProperty("RANSliceSubnetProfile")
    public void setrANSliceSubnetProfile(RANSliceSubnetProfile rANSliceSubnetProfile) {
        this.rANSliceSubnetProfile = rANSliceSubnetProfile;
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
