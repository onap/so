/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask;

import org.apache.commons.lang3.StringUtils;
import org.onap.msb.sdk.httpclient.RestServiceCreater;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.HeaderUtil;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.NetworkRpcInputEntityBuilder;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationInputEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationOutputEntity;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;

import java.util.Map;

/**
 * Created by 10112215 on 2017/9/21.
 */
public class SdncUnderlayVpnOperationClient {

    private static final String DEFAULT_MSB_IP = "127.0.0.1";
    private static final int DEFAULT_MSB_PORT = 10081;
    private RequestsDatabase requestsDB = RequestsDatabase.getInstance();

    private String serviceId;
    private String operationId;
    private String resourceTemplateUUID;


    private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);

    public boolean excute(String msbIp,
                       int msbPort,
                       Map<String, String> inputs,
                       String iServiceID,
                       String iOperationID,
                       String resourceTemplateUUID_i){
        serviceId = iServiceID;
        operationId = iOperationID;
        resourceTemplateUUID = resourceTemplateUUID_i;
        GenericResourceApi genericResourceApiClient = getGenericResourceApiClient(msbIp, msbPort);
        updateProgress(RequestsDbConstant.Status.PROCESSING, null, "10", "execute begin!");
        return sendRestrequestAndHandleResponse(inputs, genericResourceApiClient);
    }

    public boolean sendRestrequestAndHandleResponse(Map<String, String> inputs, GenericResourceApi genericResourceApiClient){
        updateProgress(null, null, "40", "sendRestrequestAndHandleResponse begin!");
        NetworkRpcInputEntityBuilder builder = new NetworkRpcInputEntityBuilder();
        RpcNetworkTopologyOperationInputEntity body = builder.build(null, inputs);
        updateProgress(null, null, "50", "RequestBody build finished!");
        RpcNetworkTopologyOperationOutputEntity networkRpcOutputEntiy = null;
        try {
            networkRpcOutputEntiy = genericResourceApiClient.postNetworkTopologyOperation
                    (HeaderUtil.DefaulAuth ,body).execute().body();
        } catch (Exception e) {
            logger.debug("Exception: ", e);
            updateProgress(RequestsDbConstant.Status.ERROR, null, null, "sendRestrequestAndHandleResponse exception:" + e.getMessage());
            return false;
        }
        updateProgress(null, null, "90", "sendRestrequestAndHandleResponse finished!");
        saveOutput(networkRpcOutputEntiy);
        updateProgress(RequestsDbConstant.Status.FINISHED, null, RequestsDbConstant.Progress.ONE_HUNDRED, "execute finished!");
        return true;
    }

    private GenericResourceApi getGenericResourceApiClient(String msbIp, int msbPort) {
        if (StringUtils.isBlank(msbIp)) {
            msbIp = DEFAULT_MSB_IP;
        }
        if (msbPort <= 0) {
            msbPort = DEFAULT_MSB_PORT;
        }
        MSBServiceClient msbClient = new MSBServiceClient(msbIp, msbPort);
        RestServiceCreater restServiceCreater = new RestServiceCreater(msbClient);
        return restServiceCreater.createService(GenericResourceApi.class);
    }

    public void updateProgress(String status,
                               String errorCode,
                               String progress,
                               String statusDescription) {
        ResourceOperationStatus resourceOperationStatus = requestsDB.getResourceOperationStatus(serviceId, operationId, resourceTemplateUUID);
        if (!StringUtils.isBlank(status)) {
            resourceOperationStatus.setStatus(status);
        }
        if (!StringUtils.isBlank(errorCode)) {
            resourceOperationStatus.setErrorCode(errorCode);
        }
        if (!StringUtils.isBlank(progress)) {
            resourceOperationStatus.setProgress(progress);
        }
        if (!StringUtils.isBlank(statusDescription)) {
            resourceOperationStatus.setStatusDescription(statusDescription);
        }
        requestsDB.updateResOperStatus(resourceOperationStatus);
    }

    private void saveOutput() {
        // Not implemented.
    }
}
