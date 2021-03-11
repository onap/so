/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */
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

    private String serviceInstanceID;

    private String subscriptionServiceType;

    private NetworkType networkType;

    private String sST;

    private Object additionalProperties;

    public NssmfRequest() {}

    public NssmfRequest(ServiceInfo serviceInfo, NetworkType networkType, Object additionalProperties) {
        this.modelInvariantUuid = serviceInfo.getServiceInvariantUuid();
        this.modelUuid = serviceInfo.getServiceUuid();
        this.globalSubscriberId = serviceInfo.getGlobalSubscriberId();
        this.subscriptionServiceType = serviceInfo.getSubscriptionServiceType();
        this.networkType = networkType;
        this.additionalProperties = additionalProperties;
        this.serviceInstanceID = serviceInfo.getNssiId();
        this.name = serviceInfo.getNssiName();
        this.sST = serviceInfo.getSST();
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
