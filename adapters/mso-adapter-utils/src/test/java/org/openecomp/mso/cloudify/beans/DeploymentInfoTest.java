/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
package org.openecomp.mso.cloudify.beans;

import org.junit.Test;
import org.openecomp.mso.cloudify.v3.model.Execution;

import java.util.HashMap;

public class DeploymentInfoTest {

    DeploymentInfo deploymentInfo = new DeploymentInfo();

    @Test
    public void getId() throws Exception {
        deploymentInfo.getId();
    }

    @Test
    public void setId() throws Exception {
        deploymentInfo.setId("test");
    }

    @Test
    public void getStatus() throws Exception {
        deploymentInfo.getStatus();
    }

    @Test
    public void setStatus() throws Exception {
        deploymentInfo.setStatus(DeploymentStatus.CREATED);
    }

    @Test
    public void getOutputs() throws Exception {
        deploymentInfo.getOutputs();
    }

    @Test
    public void setOutputs() throws Exception {
        deploymentInfo.setOutputs(new HashMap<>());
    }

    @Test
    public void getInputs() throws Exception {
        deploymentInfo.getInputs();
    }

    @Test
    public void setInputs() throws Exception {
        deploymentInfo.setInputs(new HashMap<>());
    }

    @Test
    public void getActionStatus() throws Exception {
        deploymentInfo.getActionStatus();
    }

    @Test
    public void getErrorMessage() throws Exception {
        deploymentInfo.getErrorMessage();
    }

    @Test
    public void saveExecutionStatus() throws Exception {
        deploymentInfo.saveExecutionStatus(new Execution());
    }

}