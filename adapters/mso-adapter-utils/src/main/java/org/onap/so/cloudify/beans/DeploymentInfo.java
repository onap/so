/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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

package org.onap.so.cloudify.beans;

import java.util.Map;

/*
 * This Java bean class relays Heat stack status information to ActiveVOS processes.
 *
 * This bean is returned by all Heat-specific adapter operations (create, query, delete)
 */

public final class DeploymentInfo {

    private final String id;
    private final DeploymentStatus status;
    private final Map<String, Object> outputs;
    private final Map<String, Object> inputs;
    private final String lastAction;
    private final String actionStatus;
    private final String errorMessage;

    DeploymentInfo(String id, DeploymentStatus deploymentStatus, Map<String, Object> deploymentOutputs,
            Map<String, Object> deploymentInputs, String lastAction, String actionStatus, String errorMessage) {

        this.id = id;
        this.status = deploymentStatus;
        this.outputs = deploymentOutputs;
        this.inputs = deploymentInputs;
        this.lastAction = lastAction;
        this.actionStatus = actionStatus;
        this.errorMessage = errorMessage;
    }

    public String getId() {
        return id;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public String getLastAction() {
        return lastAction;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "DeploymentInfo {" + "id='" + id + '\'' + ", inputs='" + inputs + '\'' + ", outputs='" + outputs + '\''
                + ", lastAction='" + lastAction + '\'' + ", status='" + status + '\'' + ", errorMessage='"
                + errorMessage + '\'' + '}';
    }

}

