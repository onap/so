package org.onap.so.adapters.cnf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"GVK", "Name"})
public class Resource {

    @JsonProperty("GVK")
    private GVK gVK;
    @JsonProperty("Name")
    private String name;

    @JsonProperty("GVK")
    public GVK getGVK() {
        return gVK;
    }

    @JsonProperty("GVK")
    public void setGVK(GVK gVK) {
        this.gVK = gVK;
    }

    @JsonProperty("Name")
    public String getName() {
        return name;
    }

    @JsonProperty("Name")
    public void setName(String name) {
        this.name = name;
    }
}
