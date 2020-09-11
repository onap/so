package org.onap.so.beans.nsmf.oof;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

@Data
public class SubnetCapability implements Serializable {

    private static final long serialVersionUID = -7671021271451538821L;

    private String domainType;

    private Map<?, ?> capabilityDetails;
}
