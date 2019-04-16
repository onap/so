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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.transaction.Transactional;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.sdc.api.notification.IArtifactInfo;

@Transactional
public class WorkflowResourceTest {

    private WorkflowResource workflowResource = new WorkflowResource();

    private static final String TEST_CSAR = "src/test/resources/resource-examples/WorkflowBpmn/service-CxSvc-csar.csar";
    private Path tempDirectoryPath;

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

}
