/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.asdc.installer.bpmn;

import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.junit.Test;
import org.onap.so.asdc.BaseTest;
import org.onap.so.db.catalog.beans.VnfResourceWorkflow;
import org.onap.so.db.catalog.beans.Workflow;
import org.onap.so.db.catalog.beans.WorkflowActivitySpecSequence;
import org.onap.so.db.catalog.data.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowResourceTest extends BaseTest {

    @Autowired
    private WorkflowResource workflowResource;

    @Autowired
    private WorkflowRepository workflowRepo;

    @Test
    public void getActivityNameList_Test() throws Exception {
        String bpmnContent = new String(Files
                .readAllBytes(Paths.get("src/test/resources/resource-examples/WorkflowBpmn/TestBpmnFromSDC.bpmn")));
        List<String> activityNames = workflowResource.getActivityNameList(bpmnContent);
        assertEquals("VNFSetInMaintFlagActivity", activityNames.get(0));
    }

    @Test
    public void getWorkflowNameStandard_Test() {
        String workflowName = workflowResource.getWorkflowNameFromArtifactName("TestWF2-1_0.bpmn");
        assertEquals("TestWF2", workflowName);
    }

    @Test
    public void getWorkflowNameNoVersion_Test() {
        String workflowName = workflowResource.getWorkflowNameFromArtifactName("TestWF2.bpmn");
        assertEquals("TestWF2", workflowName);
    }

    @Test
    public void getWorkflowNameNoSuffix_Test() {
        String workflowName = workflowResource.getWorkflowNameFromArtifactName("TestWF2-1_0");
        assertEquals("TestWF2", workflowName);
    }

    @Test
    public void getWorkflowVersionStandard_Test() {
        Double workflowVersion = workflowResource.getWorkflowVersionFromArtifactName("TestWF2-1_0.bpmn");
        assertTrue(workflowVersion == 1.0);
    }

    @Test
    public void getWorkflowVersionNoVersion_Test() {
        Double workflowVersion = workflowResource.getWorkflowVersionFromArtifactName("TestWF2.bpmn");
        assertNull(workflowVersion);
    }

    @Test
    public void getWorkflowVersionNoSuffix_Test() {
        Double workflowVersion = workflowResource.getWorkflowVersionFromArtifactName("TestWF2-1_0");
        assertTrue(workflowVersion == 1.0);
    }

    @Transactional
    @Test
    public void installWorkflowResource_Test() {
        Workflow workflow = new Workflow();

        workflow.setArtifactChecksum("12345");
        workflow.setArtifactName("myTestWorkflow1");
        workflow.setArtifactUUID("a5c59864-8407-4175-8060-de8ff95eab41");
        workflow.setBody("my body");
        workflow.setDescription("testing workflow");
        workflow.setName("myTestWorkflow1");
        workflow.setResourceTarget("vnf");
        workflow.setSource("sdc");
        workflow.setTimeoutMinutes(5000);
        workflow.setOperationName("myTestWorkflow");
        workflow.setVersion(1.0);

        VnfResourceWorkflow vnfResourceWorkflow = new VnfResourceWorkflow();
        vnfResourceWorkflow.setVnfResourceModelUUID("ff2ae348-214a-11e7-93ae-92361f002671");

        vnfResourceWorkflow.setWorkflow(workflow);
        List<VnfResourceWorkflow> vnfResourceWorkflows = new ArrayList<VnfResourceWorkflow>();
        vnfResourceWorkflows.add(vnfResourceWorkflow);

        workflow.setVnfResourceWorkflow(vnfResourceWorkflows);

        WorkflowActivitySpecSequence wfss1 = new WorkflowActivitySpecSequence();
        wfss1.setActivitySpecId(1);
        wfss1.setWorkflow(workflow);
        wfss1.setSeqNo(1);

        List<WorkflowActivitySpecSequence> wfss = new ArrayList<WorkflowActivitySpecSequence>();
        wfss.add(wfss1);
        workflow.setWorkflowActivitySpecSequence(wfss);

        workflowRepo.save(workflow);
        assertNotNull(workflow);

    }

}
