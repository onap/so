package org.onap.so.client.adapter.vnf.mapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Attributes implements Serializable {

    private static final long serialVersionUID = -5782985934617532582L;

    @JsonProperty("attributes")
    private List<AttributeNameValue> attributes = new ArrayList<>();

    public List<AttributeNameValue> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeNameValue> attributes) {
        this.attributes = attributes;
    }

}
