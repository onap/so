package org.onap.so.adapters.cnf.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "request", "namespace", "resources"})
public class InstanceResponse {

    @JsonProperty("id")
    private String id;
    @JsonProperty("request")
    private MulticloudInstanceRequest request;
    @JsonProperty("namespace")
    private String namespace;
    @JsonProperty("resources")
    private List<Resource> resources = null;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("request")
    public MulticloudInstanceRequest getRequest() {
        return request;
    }

    @JsonProperty("request")
    public void setRequest(MulticloudInstanceRequest request) {
        this.request = request;
    }

    @JsonProperty("namespace")
    public String getNamespace() {
        return namespace;
    }

    @JsonProperty("namespace")
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @JsonProperty("resources")
    public List<Resource> getResources() {
        return resources;
    }

    @JsonProperty("resources")
    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

}
