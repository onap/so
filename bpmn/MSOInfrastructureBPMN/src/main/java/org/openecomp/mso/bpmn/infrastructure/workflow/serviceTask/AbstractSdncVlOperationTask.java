package org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.msb.sdk.httpclient.RestServiceCreater;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.bpmn.core.BaseTask;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;

import java.util.Map;

/**
 * Created by 10112215 on 2017/9/16.
 */
public abstract class AbstractSdncVlOperationTask extends BaseTask {

    private static final String DEFAULT_MSB_IP = "127.0.0.1";
    private static final int DEFAULT_MSB_Port = 10081;
    private static final String SDCADAPTOR_INPUTS = "SDCADAPTOR_INPUTS";
    private RequestsDatabase requestsDB = RequestsDatabase.getInstance();


    private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);

    @Override
    public void execute(DelegateExecution execution) {
        GenericResourceApi genericResourceApiClient = getGenericResourceApiClient(execution);
        updateProgress(execution, RequestsDbConstant.Status.PROCESSING, null, "10", "execute begin!");
        Map<String, String> inputs = (Map<String, String>) execution.getVariable(SDCADAPTOR_INPUTS);
        updateProgress(execution, null, null, "30", "getGenericResourceApiClient finished!");
        try {
            sendRestrequestAndHandleResponse(execution, inputs, genericResourceApiClient);
            execution.setVariable("SDNCA_SuccessIndicator", true);
            updateProgress(execution, RequestsDbConstant.Status.FINISHED, null, RequestsDbConstant.Progress.ONE_HUNDRED, "execute finished!");
        } catch (Exception e) {
            e.printStackTrace();
            execution.setVariable("SDNCA_SuccessIndicator", false);
        }
    }

    public abstract void sendRestrequestAndHandleResponse(DelegateExecution execution,
                                                          Map<String, String> inputs,
                                                          GenericResourceApi genericResourceApiClient) throws Exception;

    public void updateProgress(DelegateExecution execution,
                               String status,
                               String errorCode,
                               String progress,
                               String statusDescription) {
        String serviceId = (String) execution.getVariable("serviceId");
        String operationId = (String) execution.getVariable("operationId");
        String resourceTemplateUUID = (String) execution.getVariable("resourceTemplateUUID");
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

    private GenericResourceApi getGenericResourceApiClient(DelegateExecution execution) {
        updateProgress(execution, null, null, "20", "getGenericResourceApiClient begin!");
        String msbIp = getString(execution, "MSB_IP", DEFAULT_MSB_IP);
        int msbPort = getInteger(execution, "MSB_Port", DEFAULT_MSB_Port);
        MSBServiceClient msbClient = new MSBServiceClient(msbIp, msbPort);
        RestServiceCreater restServiceCreater = new RestServiceCreater(msbClient);
        return restServiceCreater.createService(GenericResourceApi.class);
    }

    private String getString(DelegateExecution execution, String name, String defaultValue) {
        String vlaue = (String) execution.getVariable(name);
        try {
            if (!StringUtils.isBlank(vlaue)) {
                return vlaue;
            }
        } catch (Exception e) {
            System.out.println(e);
            logger.error(MessageEnum.GENERAL_EXCEPTION, " getMsbIp catch exception: ", "", this.getTaskName(), MsoLogger.ErrorCode.UnknownError, e.getClass().toString());
        } finally {
            return defaultValue;
        }
    }

    private Integer getInteger(DelegateExecution execution, String name, Integer defaultValue) {
        Integer vlaue = (Integer) execution.getVariable(name);
        try {
            if (vlaue != null && vlaue instanceof Integer) {
                return vlaue;
            }
        } catch (Exception e) {
            System.out.println(e);
            logger.error(MessageEnum.GENERAL_EXCEPTION, " getMsbIp catch exception: ", "", this.getTaskName(), MsoLogger.ErrorCode.UnknownError, e.getClass().toString());
        } finally {
            return defaultValue;
        }
    }

    public String getProcessKey(DelegateExecution execution) {
        return execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getKey();
    }
}
