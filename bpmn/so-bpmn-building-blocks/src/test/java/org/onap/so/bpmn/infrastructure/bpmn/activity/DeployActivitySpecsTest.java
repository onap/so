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

package org.onap.so.bpmn.infrastructure.bpmn.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;


public class DeployActivitySpecsTest extends BaseBPMNTest {
    private static final String RESULT_STRING = "HTTP/1.1 404 ";


    @Test
    public void DeployActivitySpecsMain_Test() throws Exception {
        String HOSTNAME = createURLWithPort("");
        ProtocolVersion protocolVersion = new ProtocolVersion("", 1, 1);
        HttpResponse response = new BasicHttpResponse(protocolVersion, 1, "");
        response.setStatusCode(404);
        response.setStatusLine(protocolVersion, 1, "");
        HttpClient clientMock = mock(HttpClient.class);
        when(clientMock.execute(any(HttpPost.class))).thenReturn(response);
        String[] args = new String[] {HOSTNAME};
        DeployActivitySpecs.main(args);
        assertNotNull(HOSTNAME);
    }

    @Test
    @Ignore
    public void DeployActivitySpec_Test() throws Exception {
        String HOSTNAME = createURLWithPort("");
        ProtocolVersion protocolVersion = new ProtocolVersion("", 1, 1);
        HttpResponse response = new BasicHttpResponse(protocolVersion, 1, "");
        response.setStatusCode(404);
        response.setStatusLine(protocolVersion, 1, "");
        HttpClient clientMock = mock(HttpClient.class);
        when(clientMock.execute(any(HttpPost.class))).thenReturn(response);;
        String result = DeployActivitySpecs.deployActivitySpec(HOSTNAME, "VNFQuiesceTrafficActivitySpec.json");
        assertEquals(result, RESULT_STRING);
    }
}
