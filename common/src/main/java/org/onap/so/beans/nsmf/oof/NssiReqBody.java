package org.onap.so.beans.nsmf.oof;

import lombok.Data;
import org.onap.so.beans.nsmf.ServiceProfile;
import java.io.Serializable;

@Data
public class NssiReqBody implements Serializable {

    private static final long serialVersionUID = -76327522074333341L;

    private ServiceProfile serviceProfile;

    private RequestInfo requestInfo;

    private TemplateInfo NSSTInfo;
}
