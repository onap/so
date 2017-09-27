
package org.openecomp.mso.client.sdno;

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
    "operation",
    "nodeType",
    "body"
})
public class SDNO implements Serializable
{

    @JsonProperty("operation")
    private String operation;
    @JsonProperty("nodeType")
    private String nodeType;
    @JsonProperty("body")
    private Body body;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();
    private final static long serialVersionUID = -5303297382564282650L;

    @JsonProperty("operation")
    public String getOperation() {
        return operation;
    }

    @JsonProperty("operation")
    public void setOperation(String operation) {
        this.operation = operation;
    }

    public SDNO withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    @JsonProperty("nodeType")
    public String getNodeType() {
        return nodeType;
    }

    @JsonProperty("nodeType")
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public SDNO withNodeType(String nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    @JsonProperty("body")
    public Body getBody() {
        return body;
    }

    @JsonProperty("body")
    public void setBody(Body body) {
        this.body = body;
    }

    public SDNO withBody(Body body) {
        this.body = body;
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

    public SDNO SDNO (String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
