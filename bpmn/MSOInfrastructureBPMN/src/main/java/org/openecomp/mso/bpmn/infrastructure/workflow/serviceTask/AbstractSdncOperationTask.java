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
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.json.JSONObject;
import org.onap.msb.sdk.httpclient.RestServiceCreater;
import org.onap.msb.sdk.httpclient.msb.MSBServiceClient;
import org.openecomp.mso.bpmn.core.BaseTask;
import org.openecomp.mso.bpmn.core.PropertyConfiguration;
import org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.GenericResourceApi;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.RequestsDbConstant;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 10112215 on 2017/9/16.
 */
public abstract class AbstractSdncOperationTask extends BaseTask {

    private static final String DEFAULT_MSB_IP = "127.0.0.1";
    private static final int DEFAULT_MSB_Port = 10081;
    private static final String SDCADAPTOR_INPUTS = "resourceParameters";
    private RequestsDatabase requestsDB = RequestsDatabase.getInstance();


    private static MsoLogger logger = MsoLogger.getMsoLogger(MsoLogger.Catalog.GENERAL);

    @Override
    public void execute(DelegateExecution execution) {
        GenericResourceApi genericResourceApiClient = getGenericResourceApiClient(execution);
        updateProgress(execution, RequestsDbConstant.Status.PROCESSING, null, "10", "execute begin!");
        Map<String, String> inputs = getInputs(execution);
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

    protected Map<String, String> getInputs(DelegateExecution execution) {
        Map<String, String> inputs = new HashMap<>();
        String json = (String) execution.getVariable(SDCADAPTOR_INPUTS);
        JSONObject jsonObject = new JSONObject(json);
        JSONObject paras = jsonObject.getJSONObject("additionalParamForNs");
        paras.keySet().stream().forEach(key -> inputs.put(key, paras.getString((String) key)));
        return inputs;
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
        Map<String, String> properties = PropertyConfiguration.getInstance().getProperties("mso.bpmn.urn.properties");
        String msbIp = getString(properties, "msb.address", DEFAULT_MSB_IP);
        int msbPort = Integer.valueOf(getString(properties, "msb.port", String.valueOf(DEFAULT_MSB_Port)));
        MSBServiceClient msbClient = new MSBServiceClient(msbIp, msbPort);
        RestServiceCreater restServiceCreater = new RestServiceCreater(msbClient);
        return restServiceCreater.createService(GenericResourceApi.class);
    }

    private String getString(Map<String, String> properties, String name, String defaultValue) {
        String vlaue = properties.get(name);
        try {
            if (!StringUtils.isBlank(vlaue)) {
                return vlaue;
            }
        } catch (Exception e) {
            System.out.println(e);
            logger.error(MessageEnum.GENERAL_EXCEPTION, " getMsbIp catch exception: ", "", this.getTaskName(), MsoLogger.ErrorCode.UnknownError, e.getClass().toString());
        }
        return defaultValue;
    }

    private Integer getInteger(DelegateExecution execution, String name, Integer defaultValue) {
        Integer vlaue = (Integer) execution.getVariable(name);
        try {
            if (vlaue != null) {
                return vlaue;
            }
        } catch (Exception e) {
            System.out.println(e);
            logger.error(MessageEnum.GENERAL_EXCEPTION, " getMsbIp catch exception: ", "", this.getTaskName(), MsoLogger.ErrorCode.UnknownError, e.getClass().toString());
        }
        return defaultValue;
    }

    public String getProcessKey(DelegateExecution execution) {
        return execution.getProcessEngineServices().getRepositoryService().getProcessDefinition(execution.getProcessDefinitionId()).getKey();
    }
}
