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

package org.onap.so.adapters.nssmf.consts;

import org.onap.so.adapters.nssmf.entity.NssmfUrlInfo;
import org.onap.so.adapters.nssmf.enums.ActionType;
import org.onap.so.adapters.nssmf.enums.ExecutorType;
import org.onap.so.adapters.nssmf.enums.HttpMethod;
import org.onap.so.beans.nsmf.NetworkType;
import java.util.HashMap;
import java.util.Map;

public class NssmfAdapterConsts {

    public final static String ONAP_INTERNAL_TAG = "ONAP_internal";

    public final static String CURRENT_INTERNAL_NSSMF_API_VERSION = "v1";

    private static Map<String, NssmfUrlInfo> urlInfoMap = new HashMap<>();

    private final static String EXTERNAL_CN_ALLOCATE_URL = "/api/rest/provMns/{apiVersion}/NSS/SliceProfiles";

    private final static String EXTERNAL_TN_ALLOCATE_URL = "/api/rest/provMns/{apiVersion}/tn/NSS/SliceProfiles";

    private final static String EXTERNAL_AN_ALLOCATE_URL = "/ObjectManagement/NSS/SliceProfiles";

    private final static String INTERNAL_ALLOCATE_URL = "/onap/so/infra/3gppservices/{apiVersion}/allocate";

    private final static String EXTERNAL_CN_DEALLOCATE_URL =
            "/api/rest/provMns/{apiVersion}/NSS/SliceProfiles/{sliceProfileId}";

    private final static String EXTERNAL_TN_DEALLOCATE_URL =
            "/api/rest/provMns/{apiVersion}/tn/NSS/SliceProfiles/{sliceProfileId}";

    private final static String EXTERNAL_AN_DEALLOCATE_URL = "/ObjectManagement/NSS/SliceProfiles/{sliceProfileId}";

    private final static String INTERNAL_DEALLOCATE_URL = "/onap/so/infra/3gppservices/{apiVersion}/deAllocate";

    private final static String EXTERNAL_CN_ACTIVATE_URL = "/api/rest/provMns/{apiVersion}/NSS/{snssai}/activation";

    private final static String EXTERNAL_TN_ACTIVATE_URL = "/api/rest/provMns/{apiVersion}/tn/NSS/{snssai}/activation";

    private final static String EXTERNAL_AN_ACTIVATE_URL = "/api/rest/provMns/{apiVersion}/an/NSS/{snssai}/activations";

    private final static String INTERNAL_ACTIVATE_URL = "/onap/so/infra/3gppservices/{apiVersion}/activate";

    private final static String EXTERNAL_CN_DEACTIVATE_URL = "/api/rest/provMns/{apiVersion}/NSS/{snssai}/deactivation";

    private final static String EXTERNAL_TN_DEACTIVATE_URL =
            "/api/rest/provMns/{apiVersion}/tn/NSS/{snssai}/deactivation";

    private final static String EXTERNAL_AN_DEACTIVATE_URL =
            "/api/rest/provMns/{apiVersion}/an/NSS/{snssai}/deactivation";

    private final static String INTERNAL_DEACTIVATE_URL = "/onap/so/infra/3gppservices/{apiVersion}/deActivate";

    //
    private final static String EXTERNAL_CN_TERMINATE_URL =
            "/api/rest/provMns/{apiVersion}/NSS/SliceProfiles/{SliceProfileId}";

    private final static String EXTERNAL_TN_TERMINATE_URL =
            "/api/rest/provMns/{apiVersion}/tn/NSS/SliceProfiles/{SliceProfileId}";

    private final static String EXTERNAL_AN_TERMINATE_URL =
            "/api/rest/provMns/{apiVersion}/an/NSS/SliceProfiles/{SliceProfileId}";

    private final static String INTERNAL_TERMINATE_URL = "/onap/so/infra/3gppservices/{apiVersion}/terminate";

    //
    private final static String EXTERNAL_AN_MODIFY_URL =
            "/api/rest/provMns/{apiVersion}/an/NSS/SliceProfiles/{SliceProfileId}";

    private final static String INTERNAL_MODIFY_URL = "/onap/so/infra/3gppservices/{apiVersion}/modify";

    //
    private final static String EXTERNAL_QUERY_JOB_STATUS =
            "/api/rest/provMns/{apiVersion}/NSS/jobs/{jobId}?responseId={responseId}";

    private final static String INTERNAL_QUERY_SUB_NET_CAPABILITY =
            "/onap/so/infra/3gppservices/{apiVersion}/subnetCapabilityQuery";

    static {
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.ACCESS, ActionType.ALLOCATE),
                new NssmfUrlInfo(EXTERNAL_AN_ALLOCATE_URL, HttpMethod.POST));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.TRANSPORT, ActionType.ALLOCATE),
                new NssmfUrlInfo(EXTERNAL_TN_ALLOCATE_URL, HttpMethod.POST));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.CORE, ActionType.ALLOCATE),
                new NssmfUrlInfo(EXTERNAL_CN_ALLOCATE_URL, HttpMethod.POST));
        urlInfoMap.put(generateKey(ExecutorType.INTERNAL, null, ActionType.ALLOCATE),
                new NssmfUrlInfo(INTERNAL_ALLOCATE_URL, HttpMethod.POST));

        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.ACCESS, ActionType.DEALLOCATE),
                new NssmfUrlInfo(EXTERNAL_AN_DEALLOCATE_URL, HttpMethod.DELETE));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.TRANSPORT, ActionType.DEALLOCATE),
                new NssmfUrlInfo(EXTERNAL_TN_DEALLOCATE_URL, HttpMethod.DELETE));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.CORE, ActionType.DEALLOCATE),
                new NssmfUrlInfo(EXTERNAL_CN_DEALLOCATE_URL, HttpMethod.DELETE));
        urlInfoMap.put(generateKey(ExecutorType.INTERNAL, null, ActionType.DEALLOCATE),
                new NssmfUrlInfo(INTERNAL_DEALLOCATE_URL, HttpMethod.DELETE));

        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.ACCESS, ActionType.ACTIVATE),
                new NssmfUrlInfo(EXTERNAL_AN_ACTIVATE_URL, HttpMethod.PUT));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.TRANSPORT, ActionType.ACTIVATE),
                new NssmfUrlInfo(EXTERNAL_TN_ACTIVATE_URL, HttpMethod.PUT));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.CORE, ActionType.ACTIVATE),
                new NssmfUrlInfo(EXTERNAL_CN_ACTIVATE_URL, HttpMethod.PUT));
        urlInfoMap.put(generateKey(ExecutorType.INTERNAL, null, ActionType.ACTIVATE),
                new NssmfUrlInfo(INTERNAL_ACTIVATE_URL, HttpMethod.PUT));

        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.ACCESS, ActionType.DEACTIVATE),
                new NssmfUrlInfo(EXTERNAL_AN_DEACTIVATE_URL, HttpMethod.PUT));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.TRANSPORT, ActionType.DEACTIVATE),
                new NssmfUrlInfo(EXTERNAL_TN_DEACTIVATE_URL, HttpMethod.PUT));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.CORE, ActionType.DEACTIVATE),
                new NssmfUrlInfo(EXTERNAL_CN_DEACTIVATE_URL, HttpMethod.PUT));
        urlInfoMap.put(generateKey(ExecutorType.INTERNAL, null, ActionType.DEACTIVATE),
                new NssmfUrlInfo(INTERNAL_DEACTIVATE_URL, HttpMethod.PUT));

        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.ACCESS, ActionType.TERMINATE),
                new NssmfUrlInfo(EXTERNAL_AN_TERMINATE_URL, HttpMethod.DELETE));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.TRANSPORT, ActionType.TERMINATE),
                new NssmfUrlInfo(EXTERNAL_TN_TERMINATE_URL, HttpMethod.DELETE));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.CORE, ActionType.TERMINATE),
                new NssmfUrlInfo(EXTERNAL_CN_TERMINATE_URL, HttpMethod.DELETE));
        urlInfoMap.put(generateKey(ExecutorType.INTERNAL, null, ActionType.TERMINATE),
                new NssmfUrlInfo(INTERNAL_TERMINATE_URL, HttpMethod.DELETE));

        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.ACCESS, ActionType.MODIFY),
                new NssmfUrlInfo(EXTERNAL_AN_MODIFY_URL, HttpMethod.PUT));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.CORE, ActionType.MODIFY),
                new NssmfUrlInfo(EXTERNAL_CN_ALLOCATE_URL, HttpMethod.PUT));
        urlInfoMap.put(generateKey(ExecutorType.INTERNAL, null, ActionType.MODIFY),
                new NssmfUrlInfo(INTERNAL_MODIFY_URL, HttpMethod.PUT));


        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.CORE, ActionType.QUERY_JOB_STATUS),
                new NssmfUrlInfo(EXTERNAL_QUERY_JOB_STATUS, HttpMethod.GET));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.ACCESS, ActionType.QUERY_JOB_STATUS),
                new NssmfUrlInfo(EXTERNAL_QUERY_JOB_STATUS, HttpMethod.GET));
        urlInfoMap.put(generateKey(ExecutorType.EXTERNAL, NetworkType.TRANSPORT, ActionType.QUERY_JOB_STATUS),
                new NssmfUrlInfo(EXTERNAL_QUERY_JOB_STATUS, HttpMethod.GET));

        urlInfoMap.put(generateKey(ExecutorType.INTERNAL, null, ActionType.QUERY_SUB_NET_CAPABILITY),
                new NssmfUrlInfo(INTERNAL_QUERY_SUB_NET_CAPABILITY, HttpMethod.POST));
    }

    /**
     * get nssmf url info from consts
     * 
     * @param executorType {@link ExecutorType}
     * @param networkType {@link NetworkType}
     * @param actionType {@link ActionType}
     * @return {@link NssmfUrlInfo}
     */
    public static NssmfUrlInfo getNssmfUrlInfo(ExecutorType executorType, NetworkType networkType,
            ActionType actionType) {

        return urlInfoMap.get(generateKey(executorType, networkType, actionType));
    }

    private static String generateKey(ExecutorType executorType, NetworkType networkType, ActionType actionType) {
        if (ExecutorType.EXTERNAL.equals(executorType)) {
            return executorType.name() + "_" + networkType.name() + "_" + actionType.name();
        }
        return executorType.name() + "_" + actionType.name();
    }


}
