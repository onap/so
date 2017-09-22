package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask;

import org.apache.commons.lang3.StringUtils;
import org.onap.msb.sdk.httpclient.RestServiceCreater;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.builder.NetworkRpcInputEntityBuilder;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkRpcInputEntity;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity.NetworkRpcOutputEntity;
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
    private static final int DEFAULT_MSB_Port = 10081;
    private RequestsDatabase requestsDB = RequestsDatabase.getInstance();

    private String serviceId;
    private String operationId;
    private String resourceTemplateUUID;


    private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);

    public boolean excute(String msbIp,
                       int msbPort,
                       Map<String, String> inputs,
                       String serviceId_i,
                       String operationId_i,
                       String resourceTemplateUUID_i){
        serviceId = serviceId_i;
        operationId = operationId_i;
        resourceTemplateUUID = resourceTemplateUUID_i;
        GenericResourceApi genericResourceApiClient = getGenericResourceApiClient(msbIp, msbPort);
        updateProgress(RequestsDbConstant.Status.PROCESSING, null, "10", "execute begin!");
        return sendRestrequestAndHandleResponse(inputs, genericResourceApiClient);
    }

    public boolean sendRestrequestAndHandleResponse(Map<String, String> inputs, GenericResourceApi genericResourceApiClient){
        updateProgress(null, null, "40", "sendRestrequestAndHandleResponse begin!");
        NetworkRpcInputEntityBuilder builder = new NetworkRpcInputEntityBuilder();
        NetworkRpcInputEntity body = builder.build(inputs);
        updateProgress(null, null, "50", "RequestBody build finished!");
        NetworkRpcOutputEntity networkRpcOutputEntiy = null;
        try {
            networkRpcOutputEntiy = genericResourceApiClient.postNetworkTopologyPeration(body).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
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
            msbPort = DEFAULT_MSB_Port;
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

    private void saveOutput(NetworkRpcOutputEntity output) {

    }
}
