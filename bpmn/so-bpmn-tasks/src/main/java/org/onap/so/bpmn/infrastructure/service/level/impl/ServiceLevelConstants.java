/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.service.level.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ServiceLevelConstants {
    public static final String BPMN_REQUEST = "bpmnRequest";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    public static final String PNF_NAME = "pnfName";
    public static final String PNF = "pnf";
    public static final String VNF = "vnf";
    public static final String EMPTY_STRING = "";
    public static final String HEALTH_CHECK_WORKFLOW_TO_INVOKE = "healthCheckWorkflow";
    public static final String SOFTWARE_WORKFLOW_TO_INVOKE = "softwareUpgradeWorkflow";
    public static final String HEALTH_CHECK_OPERATION = "ResourceHealthCheck";
    public static final String SW_UP_OPERATION = "ResourceSoftwareUpgrade";
    public static final String CONTROLLER_STATUS = "ControllerStatus";
    public static final int ERROR_CODE = 601;

    // TODO GenericVNFHealthCheck and GenericVnfSoftwareUpgrade workflow names should be updated once the workflow is
    // implemented.
    public static final Map<String, String> DEFAULT_HEALTH_CHECK_WORKFLOWS =
            Map.of(PNF, "GenericPnfHealthCheck", VNF, "GenericVNFHealthCheck");

    public static final Map<String, String> DEFAULT_SOFTWARE_UP_WORKFLOWS =
            Map.of(PNF, "GenericPnfSoftwareUpgrade", VNF, "GenericVnfSoftwareUpgrade");

    // Maps operation name with workflows
    public static final Map<String, Map<String, String>> WORKFLOW_OPERATIONS_MAP = Map.of(HEALTH_CHECK_OPERATION,
            DEFAULT_HEALTH_CHECK_WORKFLOWS, SW_UP_OPERATION, DEFAULT_SOFTWARE_UP_WORKFLOWS);

    public static final List<String> VALID_CONTROLLER_SCOPE = Arrays.asList(PNF, VNF);



}
