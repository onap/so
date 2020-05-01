package org.onap.aaiclient.client.aai;

import org.onap.aaiclient.client.graphinventory.GraphInventoryObjectName;
import com.google.common.base.CaseFormat;

public class AAIObjectName implements GraphInventoryObjectName {

    private final String name;

    public AAIObjectName(String name) {
        this.name = name;
    }

    @Override
    public String typeName() {
        return name;
    }

    @Override
    public String typeName(CaseFormat format) {
        return CaseFormat.LOWER_HYPHEN.to(format, this.name);
    }
}
