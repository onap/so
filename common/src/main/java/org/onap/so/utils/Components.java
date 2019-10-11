package org.onap.so.utils;

import java.util.EnumSet;
import java.util.Set;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.logging.filter.base.ONAPComponentsList;

public enum Components implements ONAPComponentsList {
    OPENSTACK, UNKNOWN, ASDC_CONTROLLER, APIH;

    public static Set<Components> getSOInternalComponents() {
        return EnumSet.of(ASDC_CONTROLLER, APIH);
    }

    @Override
    public String toString() {
        if (getSOInternalComponents().contains(this))
            return ONAPComponents.SO + "." + this.name();
        else
            return this.name();
    }
}
