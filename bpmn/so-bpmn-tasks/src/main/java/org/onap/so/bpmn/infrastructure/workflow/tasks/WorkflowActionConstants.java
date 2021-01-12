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

public final class WorkflowActionConstants {

    private WorkflowActionConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String USER_PARAM_SERVICE = "service";
    public static final String CREATE_INSTANCE = "createInstance";
    public static final String UPGRADE_INSTANCE = "upgradeInstance";
    public static final String FABRIC_CONFIGURATION = "FabricConfiguration";
    public static final String WORKFLOW_ACTION_ERROR_MESSAGE = "WorkflowActionErrorMessage";
    static final String SERVICE = "Service";
    static final String CONTROLLER = "Controller";
    public static final String NETWORKCOLLECTION = "NetworkCollection";
    static final String CONFIGURATION = "Configuration";
    public static final String ASSIGNINSTANCE = "assignInstance";
    static final String REPLACEINSTANCE = "replaceInstance";
    static final String VOLUMEGROUP = "VolumeGroup";
    static final String REPLACEINSTANCERETAINASSIGNMENTS = "replaceInstanceRetainAssignments";
}
