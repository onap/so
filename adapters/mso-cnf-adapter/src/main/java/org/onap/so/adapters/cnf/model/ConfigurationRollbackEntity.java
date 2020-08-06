package org.onap.so.adapters.cnf.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = "true")
public class ConfigurationRollbackEntity {

    // TODO
    @JsonProperty(value = "anyOf")
    private List<Config> anyOf;

    public List<Config> getAnyOf() {
        return anyOf;
    }

    public void setAnyOf(List<Config> anyOf) {
        this.anyOf = anyOf;
    }

}
