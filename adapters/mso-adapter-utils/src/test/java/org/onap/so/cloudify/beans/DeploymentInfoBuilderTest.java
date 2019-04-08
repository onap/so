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

import static org.assertj.core.api.Assertions.assertThat;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.onap.so.cloudify.v3.model.Execution;

public class DeploymentInfoBuilderTest {

    private static final String ERROR_MESSAGE = "something went wrong";
    private static final String INSTALL_WORKFLOW_ID = "install";
    private static final String UNINSTALL_WORKFLOW_ID = "uninstall";

    @Test
    public void shouldConstructDeploymentInfo_withBasicValues() {
        DeploymentInfo deploymentInfo = new DeploymentInfoBuilder().withId("id").withStatus(DeploymentStatus.CREATED)
                .withDeploymentOutputs(ImmutableMap.of()).withDeploymentInputs(ImmutableMap.of())
                .withActionStatus("started").withLastAction(INSTALL_WORKFLOW_ID).withErrorMessage(ERROR_MESSAGE)
                .build();

        assertThat(deploymentInfo.getId()).isEqualTo("id");
        assertThat(deploymentInfo.getStatus()).isEqualTo(DeploymentStatus.CREATED);
        assertThat(deploymentInfo.getOutputs()).isEqualTo(ImmutableMap.of());
        assertThat(deploymentInfo.getInputs()).isEqualTo(ImmutableMap.of());
        assertThat(deploymentInfo.getActionStatus()).isEqualTo("started");
        assertThat(deploymentInfo.getLastAction()).isEqualTo(INSTALL_WORKFLOW_ID);
        assertThat(deploymentInfo.getErrorMessage()).isEqualTo(ERROR_MESSAGE);
    }

    @Test
    public void shouldConstructDeploymentInfo_withCreateDeploymentStatus_fromNullExecution() {
        DeploymentInfo deploymentInfo = new DeploymentInfoBuilder().fromExecution(null).build();

        assertThat(deploymentInfo.getStatus()).isEqualTo(DeploymentStatus.CREATED);
    }

    @Test
    public void shouldConstructDeploymentInfo_withInstalledDeploymentStatus_fromTerminatedExecution() {
        String workflowIdLastAction = INSTALL_WORKFLOW_ID;
        String status = "terminated";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.INSTALLED;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withFailedDeploymentStatus_fromFailedInstallExecution() {
        String workflowIdLastAction = INSTALL_WORKFLOW_ID;
        String status = "failed";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.FAILED;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withInstallingDeploymentStatus_fromStartedExecution() {
        String workflowIdLastAction = INSTALL_WORKFLOW_ID;
        String status = "started";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.INSTALLING;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withInstallingDeploymentStatus_fromPendingExecution() {
        String workflowIdLastAction = INSTALL_WORKFLOW_ID;
        String status = "pending";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.INSTALLING;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withUnknownDeploymentStatus_fromUnmappableExecution() {
        String workflowIdLastAction = INSTALL_WORKFLOW_ID;
        String status = "strangeStatus";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.UNKNOWN;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withCreatedDeploymentStatus_fromTerminatedExecution() {
        String workflowIdLastAction = UNINSTALL_WORKFLOW_ID;
        String status = "terminated";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.CREATED;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withFailedDeploymentStatus_fromFailedUninstallExecution() {
        String workflowIdLastAction = UNINSTALL_WORKFLOW_ID;
        String status = "failed";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.FAILED;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withUninstallingDeploymentStatus_fromStartedUninstallExecution() {
        String workflowIdLastAction = UNINSTALL_WORKFLOW_ID;
        String status = "started";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.UNINSTALLING;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withUninstallingDeploymentStatus_fromPendingUninstallExecution() {
        String workflowIdLastAction = UNINSTALL_WORKFLOW_ID;
        String status = "pending";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.UNINSTALLING;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withUnknownDeploymentStatus_fromUnmappableUninstallExecution() {
        String workflowIdLastAction = UNINSTALL_WORKFLOW_ID;
        String status = "strangeStatus";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.UNKNOWN;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldConstructDeploymentInfo_withUnknownDeploymentStatus_forUnknownExecutionWorkflowId() {
        String workflowIdLastAction = "strangeWorkflowIdLastAction";
        String status = "strangeStatus";
        DeploymentStatus expectedDeploymentStatus = DeploymentStatus.UNKNOWN;
        verifyDeploymentInfoConstruction(workflowIdLastAction, status, expectedDeploymentStatus);
    }

    @Test
    public void shouldSetEmptyOutputsMapWhenInputIsNull() {
        DeploymentInfo deploymentInfo = new DeploymentInfoBuilder().withDeploymentOutputs(null).build();
        assertThat(deploymentInfo.getOutputs()).isEmpty();
    }

    private void verifyDeploymentInfoConstruction(String workflowIdLastAction, String actionStatus,
            DeploymentStatus expectedDeploymentStatus) {

        Execution execution = new Execution();
        execution.setWorkflowId(workflowIdLastAction);
        execution.setStatus(actionStatus);
        execution.setError(ERROR_MESSAGE);
        DeploymentInfo deploymentInfo = new DeploymentInfoBuilder().fromExecution(execution).build();

        assertThat(deploymentInfo.getLastAction()).isEqualTo(workflowIdLastAction);
        assertThat(deploymentInfo.getActionStatus()).isEqualTo(actionStatus);
        assertThat(deploymentInfo.getErrorMessage()).isEqualTo(ERROR_MESSAGE);
        assertThat(deploymentInfo.getStatus()).isEqualTo(expectedDeploymentStatus);
    }
}
