package org.onap.so.beans.nsmf;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class ServiceInfo implements Serializable {

    private static final long serialVersionUID = 7895110339097615695L;

    private String serviceInvariantUuid;

    private String serviceUuid;

    private String globalSubscriberId;

    private String subscriptionServiceType;

    private String serviceType;

    private String nsiId;

    private String nssiId;

    private String sST;

    private String nssiName;

    private String pLMNIdList;
}
