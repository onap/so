/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 Nokia.
 * ============================================================================= Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.so.cloudify.beans;

import java.util.HashMap;
import java.util.Map;
import org.onap.so.cloudify.v3.model.Execution;

public final class DeploymentInfoBuilder {

    private String id = "";
    private DeploymentStatus deploymentStatus = DeploymentStatus.NOTFOUND;
    private Map<String, Object> deploymentOutputs = new HashMap<>();
    private Map<String, Object> deploymentInputs = new HashMap<>();
    private String lastAction;
    private String actionStatus;
    private String errorMessage;

    public DeploymentInfoBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public DeploymentInfoBuilder withStatus(DeploymentStatus deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
        return this;
    }

    public DeploymentInfoBuilder withDeploymentOutputs(Map<String, Object> deploymentOutputs) {
        if (deploymentOutputs != null) {
            this.deploymentOutputs = deploymentOutputs;
        }
        return this;
    }

    public DeploymentInfoBuilder withDeploymentInputs(Map<String, Object> deploymentInputs) {
        this.deploymentInputs = deploymentInputs;
        return this;
    }

    public DeploymentInfoBuilder withLastAction(String lastAction) {
        this.lastAction = lastAction;
        return this;
    }

    public DeploymentInfoBuilder withActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
        return this;
    }

    public DeploymentInfoBuilder withErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public DeploymentInfoBuilder fromExecution(Execution execution) {
        if (execution != null) {
            this.lastAction = execution.getWorkflowId();
            this.actionStatus = execution.getStatus();
            this.errorMessage = execution.getError();

            // Compute the status based on the last workflow
            if (("install").equals(lastAction)) {
                if (("terminated").equals(actionStatus)) {
                    this.deploymentStatus = DeploymentStatus.INSTALLED;
                } else if (("failed").equals(actionStatus)) {
                    this.deploymentStatus = DeploymentStatus.FAILED;
                } else if (("started").equals(actionStatus) || ("pending").equals(actionStatus)) {
                    this.deploymentStatus = DeploymentStatus.INSTALLING;
                } else {
                    this.deploymentStatus = DeploymentStatus.UNKNOWN;
                }
            } else if (("uninstall").equals(lastAction)) {
                if (("terminated").equals(actionStatus)) {
                    this.deploymentStatus = DeploymentStatus.CREATED;
                } else if (("failed").equals(actionStatus)) {
                    this.deploymentStatus = DeploymentStatus.FAILED;
                } else if (("started").equals(actionStatus) || ("pending").equals(actionStatus)) {
                    this.deploymentStatus = DeploymentStatus.UNINSTALLING;
                } else {
                    this.deploymentStatus = DeploymentStatus.UNKNOWN;
                }
            } else {
                // Could have more cases in the future for different actions.
                this.deploymentStatus = DeploymentStatus.UNKNOWN;
            }
        } else {
            this.deploymentStatus = DeploymentStatus.CREATED;
        }

        return this;
    }

    public DeploymentInfo build() {
        return new DeploymentInfo(id, deploymentStatus, deploymentOutputs, deploymentInputs, lastAction, actionStatus,
                errorMessage);
    }
}
