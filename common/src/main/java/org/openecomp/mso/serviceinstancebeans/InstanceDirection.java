package org.openecomp.mso.serviceinstancebeans;

public enum InstanceDirection {

    source,
    destination;

    public String value() {
        return name();
    }

    public static InstanceDirection fromValue(String v) {
        return valueOf(v);
    }
}
