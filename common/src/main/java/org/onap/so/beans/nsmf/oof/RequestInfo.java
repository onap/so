package org.onap.so.beans.nsmf.oof;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

@Data
public class RequestInfo implements Serializable {

    private static final long serialVersionUID = -875589918188540922L;

    private String transactionId;

    private String requestId;

    private String callbackUrl;

    private Map<?, ?> callbackHeader;

    private String sourceId;

    private Integer numSolutions;

    private Integer timeout;

    private Map<?, ?> addtnlArgs;
}
