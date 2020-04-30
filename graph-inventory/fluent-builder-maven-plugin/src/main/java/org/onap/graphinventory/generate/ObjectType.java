package org.onap.graphinventory.generate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ObjectType {

    private List<String> paths = new ArrayList<>();

    private Set<String> children = new HashSet<>();

    private String name;

    private String additionalName;

    private String topLevel;

    private String type;

    private String partialUri;

    private List<ObjectField> fields = new ArrayList<>();

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public String getTopLevel() {
        return topLevel;
    }

    public void setTopLevel(String topLevel) {
        this.topLevel = topLevel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ObjectField> getFields() {
        return fields;
    }

    public void setFields(List<ObjectField> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getChildren() {
        return children;
    }

    public void setChildren(Set<String> children) {
        this.children = children;
    }

    public String getPartialUri() {
        return partialUri;
    }

    public void setPartialUri(String partialUri) {
        this.partialUri = partialUri;
    }

    public String getAdditionalName() {
        return additionalName;
    }

    public void setAdditionalName(String additionalName) {
        this.additionalName = additionalName;
    }


}
