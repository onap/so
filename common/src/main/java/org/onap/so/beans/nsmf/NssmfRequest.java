package org.onap.so.beans.nsmf;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class NssmfRequest implements Serializable {

    private static final long serialVersionUID = 3313218757241310655L;

    private String name;

    private String modelInvariantUuid;

    private String modelUuid;

    private String globalSubscriberId;

    private String serviceInstanceId;

    private String subscriptionServiceType;

    private NetworkType networkType;

    private Object additionalProperties;

    public NssmfRequest() {}

    public NssmfRequest(ServiceInfo serviceInfo, NetworkType networkType, Object additionalProperties) {
        this.modelInvariantUuid = serviceInfo.getServiceInvariantUuid();
        this.modelUuid = serviceInfo.getServiceUuid();
        this.globalSubscriberId = serviceInfo.getGlobalSubscriberId();
        this.subscriptionServiceType = serviceInfo.getSubscriptionServiceType();
        this.networkType = networkType;
        this.additionalProperties = additionalProperties;
        this.serviceInstanceId = serviceInfo.getNssiId();
        this.name = serviceInfo.getNssiName();
    }

}


/**
 * { "name": "eMBB-001", "modelInvariantUuid": "NSST-C-001-HDBNJ-NSSMF-01-A-ZX", "modelUuid":
 * "NSST-C-001-HDBNJ-NSSMF-01-A-ZX-UUID", "globalSubscriberId": "5GCustomer", "subscriptionServiceType": "5G",
 * "networkType": "AN/CN/TN", "additionalProperties": { "sliceProfile": { "snssaiList": [ "001-100001" ],
 * "sliceProfileId": "ab9af40f13f721b5f13539d87484098", "plmnIdList": [ "460-00", "460-01" ], "perfReq": {
 * "perfReqEmbbList ": [ { "activityFactor": 50 } ] }, "maxNumberofUEs": 200, "coverageAreaTAList": [ "1", "2", "3", "4"
 * ], "latency": 2, "resourceSharingLevel": "non-shared" }, "endPoints": [ { "nodeId": "", "additionalInfo": { "xxx":
 * "xxx" } }, { "nodeId": "", "additionalInfo": { "xxx": "xxx" } } ], "nsiInfo": { "nsiId":
 * "NSI-M-001-HDBNJ-NSMF-01-A-ZX", "nsiName": "eMBB-001" }, "scriptName": "AN1" } }
 */
