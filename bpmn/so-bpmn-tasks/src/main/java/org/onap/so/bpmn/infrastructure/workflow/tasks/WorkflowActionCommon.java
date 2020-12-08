/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;

import java.util.List;
import java.util.Map;

public class WorkflowActionCommon {

    static final String USER_PARAM_SERVICE = "service";
    static final String CREATE_INSTANCE = "createInstance";
    static final String FABRIC_CONFIGURATION = "FabricConfiguration";
    static final String WORKFLOW_ACTION_ERROR_MESSAGE = "WorkflowActionErrorMessage";

    static List<Map<String, Object>> getListOfUserParams(ServiceInstancesRequest sIRequest) {
        return sIRequest.getRequestDetails().getRequestParameters().getUserParams();
    }
}
