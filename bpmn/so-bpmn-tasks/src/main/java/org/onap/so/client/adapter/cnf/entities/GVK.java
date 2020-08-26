
package org.onap.so.client.adapter.cnf.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"Group", "Version", "Kind"})
public class GVK {

    @JsonProperty("Group")
    private String group;
    @JsonProperty("Version")
    private String version;
    @JsonProperty("Kind")
    private String kind;

    @JsonProperty("Group")
    public String getGroup() {
        return group;
    }

    @JsonProperty("Group")
    public void setGroup(String group) {
        this.group = group;
    }

    @JsonProperty("Version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("Version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("Kind")
    public String getKind() {
        return kind;
    }

    @JsonProperty("Kind")
    public void setKind(String kind) {
        this.kind = kind;
    }

}
