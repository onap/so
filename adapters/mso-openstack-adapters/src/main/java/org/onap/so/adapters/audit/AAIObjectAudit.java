package org.onap.so.adapters.audit;

import java.io.Serializable;
import java.net.URI;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AAIObjectAudit implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4560928512855386021L;
    private boolean doesObjectExist = false;
    private Object aaiObject;
    private URI resourceURI;
    private String aaiObjectType;

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("doesObjectExist", doesObjectExist).append("aaiObject", aaiObject)
                .append("resourceURI", resourceURI).append("aaiObjectType", aaiObjectType).toString();
    }

    public String getAaiObjectType() {
        return aaiObjectType;
    }

    public void setAaiObjectType(String aaiObjectType) {
        this.aaiObjectType = aaiObjectType;
    }

    public boolean isDoesObjectExist() {
        return doesObjectExist;
    }

    public void setDoesObjectExist(boolean doesObjectExist) {
        this.doesObjectExist = doesObjectExist;
    }

    public Object getAaiObject() {
        return aaiObject;
    }

    public void setAaiObject(Object aaiObject) {
        this.aaiObject = aaiObject;
    }

    public URI getResourceURI() {
        return resourceURI;
    }

    public void setResourceURI(URI resourceURI) {
        this.resourceURI = resourceURI;
    }
}
