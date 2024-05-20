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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import jakarta.transaction.Transactional;
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

@Transactional
public class BpmnInstallerTest {

    private BpmnInstaller bpmnInstaller = new BpmnInstaller();

    private static final String TEST_CSAR = "src/test/resources/resource-examples/WorkflowBpmn/service-CxSvc-csar.csar";
    private Path tempDirectoryPath;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void init() throws Exception {
        System.setProperty("mso.config.path", folder.getRoot().toString());
        // we need to have this directory created for InstallBPMN test success
        tempDirectoryPath = Paths.get(folder.getRoot().toString(), "ASDC");
        Files.createDirectories(tempDirectoryPath);
    }

    @AfterClass
    public static void cleanup() {
        System.clearProperty("mso.config.path");
    }

    @Test
    public void buildMimeMultiPart_Test() throws Exception {
        Path tempFilePath = Paths.get(tempDirectoryPath.toAbsolutePath().toString(), "TestBB.bpmn");
        Files.createFile(tempFilePath);
        HttpEntity entity = bpmnInstaller.buildMimeMultipart("TestBB.bpmn", "");
        String mimeMultipartBodyFilePath = "src/test/resources" + "/mime-multipart-body.txt";

        File mimeMultipartBody = new File(mimeMultipartBodyFilePath);
        InputStream expectedContent = new FileInputStream(mimeMultipartBody);

        assertThat(IOUtils.contentEquals(expectedContent, entity.getContent()));

        IOUtils.closeQuietly(expectedContent);
    }

    @Test
    public void installBpmn_Test() throws Exception {
        BpmnInstaller bpmnInstallerSpy = spy(bpmnInstaller);
        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, ""));
        HttpClient httpClient = mock(HttpClient.class);
        doReturn(response).when(httpClient).execute(any(HttpPost.class));
        bpmnInstallerSpy.installBpmn(TEST_CSAR);
        verify(bpmnInstallerSpy, times(1)).sendDeploymentRequest(anyString(), anyString());
    }

    @Test
    public void containsWorkflowsSuccess() {
        boolean result = bpmnInstaller.containsWorkflows(TEST_CSAR);
        assertTrue(result);
    }

    @Test
    public void containsWorkflowsFailure() {
        boolean result = bpmnInstaller.containsWorkflows("DOESNOTEXIST.csar");
        assertFalse(result);
    }
}
