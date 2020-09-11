package org.onap.so.beans.nsmf.oof;

import lombok.Data;
import org.onap.so.beans.nsmf.ServiceProfile;
import java.io.Serializable;
import java.util.List;

@Data
public class NsiReqBody implements Serializable {
    private static final long serialVersionUID = -1383112063216226985L;

    private ServiceProfile serviceProfile;

    private RequestInfo requestInfo;

    private TemplateInfo NSTInfo;

    private List<TemplateInfo> NSSTInfo;

    private Boolean preferReuse;

    private List<SubnetCapability> subnetCapabilities;
}
