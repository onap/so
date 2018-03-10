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
import org.openecomp.mso.bpmn.core.BaseTask;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.requestsdb.ResourceOperationStatus;

public class SdncUnderlayVpnPreprocessTask extends BaseTask {
    public static final String RESOURCE_OPER_TYPE = "resourceOperType";
    private RequestsDatabase requestsDB = RequestsDatabase.getInstance();

    @Override
    public void execute(DelegateExecution execution) {
        String operType = getOperType(execution);
        execution.setVariable(RESOURCE_OPER_TYPE, operType);
    }

    private String getOperType(DelegateExecution execution) {
        String serviceId = (String) execution.getVariable("serviceId");
        serviceId = StringUtils.isBlank(serviceId) ? (String) execution.getVariable("serviceInstanceId") : serviceId;
        String operationId = (String) execution.getVariable("operationId");
        String resourceTemplateUUID = (String) execution.getVariable("resourceUUID");
        resourceTemplateUUID = StringUtils.isBlank(resourceTemplateUUID) ? (String) execution.getVariable("resourceTemplateId") : resourceTemplateUUID;
        ResourceOperationStatus resourceOperationStatus = requestsDB.getResourceOperationStatus(serviceId, operationId, resourceTemplateUUID);
        return resourceOperationStatus.getOperType();
    }
}
