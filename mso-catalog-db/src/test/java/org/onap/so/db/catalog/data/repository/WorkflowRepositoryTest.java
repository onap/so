/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * Foundation. ================================================================================ Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so.db.catalog.data.repository;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.onap.so.db.catalog.BaseTest;
import org.onap.so.db.catalog.beans.Workflow;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowRepositoryTest extends BaseTest {

    @Autowired
    private WorkflowRepository workflowRepository;

    @Test
    public void findByArtifactUuid_ValidUuid_ExpectedOutput() {
        Workflow workflow = workflowRepository.findByArtifactUUID("5b0c4322-643d-4c9f-b184-4516049e99b1");

        assertEquals("artifactName", "testingWorkflow.bpmn", workflow.getArtifactName());
    }

    @Test
    public void findByVnfResourceModelUUIDTest() {
        List<Workflow> workflows =
                workflowRepository.findWorkflowByVnfModelUUID("ff2ae348-214a-11e7-93ae-92361f002671");

        Assert.assertTrue(workflows != null);
        Assert.assertTrue(workflows.size() != 0);

        Assert.assertTrue("testingWorkflow.bpmn".equals(workflows.get(0).getArtifactName()));
    }

    @Test
    public void findBySourceTest() {
        List<Workflow> workflows = workflowRepository.findBySource("sdc");

        Assert.assertTrue(workflows != null);
        Assert.assertTrue(workflows.size() != 0);

        Assert.assertTrue("testingWorkflow.bpmn".equals(workflows.get(0).getArtifactName()));
    }

    @Test
    public void findByResourceTargetTest() {
        List<Workflow> workflows = workflowRepository.findByResourceTarget("pnf");

        Assert.assertTrue(workflows != null);
        Assert.assertTrue(workflows.size() == 1);

        Assert.assertTrue("DummyPnfWorkflow".equals(workflows.get(0).getArtifactName()));
    }

}
