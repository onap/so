/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.installer.bpmn.BpmnInstaller;
import org.springframework.beans.factory.annotation.Autowired;

@Transactional
public class BpmnInstallerTest extends BaseTest {
	
    @Autowired
    private BpmnInstaller bpmnInstaller;
    
    @Before
    public void init() throws Exception {
    	System.setProperty("mso.config.path", "src/test/resources");
    }
    
    @Test
    public void buildMimeMultiPart_Test() throws Exception {
    	
    	HttpEntity entity = bpmnInstaller.buildMimeMultipart("TestBB.bpmn");    	
    	String mimeMultipartBodyFilePath = System.getProperty("mso.config.path") + "/mime-multipart-body.txt";
    	
    	File mimeMultipartBody = new File(mimeMultipartBodyFilePath);
    	InputStream expectedContent = new FileInputStream(mimeMultipartBody);
    	
    	assertThat(IOUtils.contentEquals(expectedContent, entity.getContent()));    
    }

    @Test
    public void installBpmn_Test() throws Exception {
    	HttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, ""));
    	HttpClient httpClient = mock(HttpClient.class);
    	String csarPath = System.getProperty("mso.config.path") + "/resource-examples/WorkflowBpmn/service-CxSvc-csar.csar";
    	doReturn(response).when(httpClient).execute(any(HttpPost.class));
    	bpmnInstaller.installBpmn(csarPath);
    }
   
}
