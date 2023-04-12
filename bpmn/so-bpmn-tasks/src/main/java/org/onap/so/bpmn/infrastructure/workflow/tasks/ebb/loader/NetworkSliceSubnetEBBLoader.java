/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2022 DTAG
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.ebb.loader;

import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.bpmn.infrastructure.workflow.tasks.WorkflowType;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class NetworkSliceSubnetEBBLoader {

    private static final Logger logger = LoggerFactory.getLogger(VnfEBBLoader.class);

    private final BBInputSetupUtils bbInputSetupUtils;
    private final BBInputSetup bbInputSetup;
    private final WorkflowActionExtractResourcesAAI workflowActionUtils;
    private final ExceptionBuilder exceptionBuilder;

    NetworkSliceSubnetEBBLoader(BBInputSetupUtils bbInputSetupUtils, BBInputSetup bbInputSetup,
            WorkflowActionExtractResourcesAAI workflowActionUtils, ExceptionBuilder exceptionBuilder) {
        this.bbInputSetupUtils = bbInputSetupUtils;
        this.bbInputSetup = bbInputSetup;
        this.workflowActionUtils = workflowActionUtils;
        this.exceptionBuilder = exceptionBuilder;
    }


    public List<Resource> setNetworkSliceSubnetResource(String serviceId) {
        Resource networkSliceSubnetResource = new Resource(WorkflowType.NETWORK_SLICE_SUBNET, serviceId, false, null);
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(networkSliceSubnetResource);
        return resourceList;
    }
}

