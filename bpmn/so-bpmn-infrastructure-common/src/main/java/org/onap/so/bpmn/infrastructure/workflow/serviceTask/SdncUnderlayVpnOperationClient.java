/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.workflow.serviceTask;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.onap.msb.sdk.httpclient.RestServiceCreater;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.HeaderUtil;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.builder.NetworkRpcInputEntityBuilder;
import org.onap.so.bpmn.infrastructure.workflow.serviceTask.client.entity.RpcNetworkTopologyOperationInputEntity;
import org.onap.so.db.request.beans.ResourceOperationStatus;
import org.onap.so.db.request.beans.ResourceOperationStatusId;
import org.onap.so.requestsdb.RequestsDbConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SdncUnderlayVpnOperationClient {

    private static final String DEFAULT_MSB_IP = "127.0.0.1";
    private static final int DEFAULT_MSB_PORT = 10081;

    private static Logger logger = LoggerFactory.getLogger(SdncUnderlayVpnOperationClient.class);

    public boolean excute(String msbIp,
                       int msbPort,
                       Map<String, String> inputs,
                       String iServiceID,
                       String iOperationID,
                       String resourceTemplateUUID_i){
    	ResourceOperationStatusId id = new ResourceOperationStatusId(iServiceID, iOperationID, resourceTemplateUUID_i);
        GenericResourceApi genericResourceApiClient = getGenericResourceApiClient(msbIp, msbPort);
        updateProgress(id, RequestsDbConstant.Status.PROCESSING, null, "10", "execute begin!");
        return sendRestrequestAndHandleResponse(id, inputs, genericResourceApiClient);
    }

    public boolean sendRestrequestAndHandleResponse(ResourceOperationStatusId id, Map<String, String> inputs, GenericResourceApi genericResourceApiClient){
        updateProgress(id, null, null, "40", "sendRestrequestAndHandleResponse begin!");
        NetworkRpcInputEntityBuilder builder = new NetworkRpcInputEntityBuilder();
        RpcNetworkTopologyOperationInputEntity body = builder.build(null, inputs);
        updateProgress(id, null, null, "50", "RequestBody build finished!");
        //RpcNetworkTopologyOperationOutputEntity networkRpcOutputEntiy = null;
        try {
            genericResourceApiClient.postNetworkTopologyOperation(HeaderUtil.DefaulAuth ,body).execute().body();
        } catch (Exception e) {
            logger.debug("Exception: ", e);
            updateProgress(id, RequestsDbConstant.Status.ERROR, null, null, "sendRestrequestAndHandleResponse exception:" + e.getMessage());
            return false;
        }
        updateProgress(id, null, null, "90", "sendRestrequestAndHandleResponse finished!");
        updateProgress(id, RequestsDbConstant.Status.FINISHED, null, RequestsDbConstant.Progress.ONE_HUNDRED, "execute finished!");
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

    public void updateProgress(ResourceOperationStatusId id, String status,
                               String errorCode,
                               String progress,
                               String statusDescription) {
    	
    	
        ResourceOperationStatus resourceOperationStatus = new ResourceOperationStatus();//rosRepo.getOne(id);
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
        //rosRepo.save(resourceOperationStatus);
    }

    private void saveOutput() {
        // Not implemented.
    }
}
