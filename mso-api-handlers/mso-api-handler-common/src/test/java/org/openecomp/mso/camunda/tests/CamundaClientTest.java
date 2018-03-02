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

package org.openecomp.mso.camunda.tests;


import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.apihandler.common.CommonConstants;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.properties.MsoJavaProperties;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;


/**
 * This class implements test methods of Camunda Beans.
 *
 *
 */
public class CamundaClientTest {



    @Mock
    private HttpClient mockHttpClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void tesCamundaPost() throws JsonGenerationException,
    JsonMappingException, IOException {


        String responseBody ="{\"links\":[{\"method\":\"GET\",\"href\":\"http://localhost:9080/engine-rest/process-instance/2047c658-37ae-11e5-9505-7a1020524153\",\"rel\":\"self\"}],\"id\":\"2047c658-37ae-11e5-9505-7a1020524153\",\"definitionId\":\"dummy:10:73298961-37ad-11e5-9505-7a1020524153\",\"businessKey\":null,\"caseInstanceId\":null,\"ended\":true,\"suspended\":false}";

        HttpResponse mockResponse = createResponse(200, responseBody);
        mockHttpClient = Mockito.mock(HttpClient.class);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class)))
        .thenReturn(mockResponse);

        String reqXML = "<xml>test</xml>";
        String orchestrationURI = "/engine-rest/process-definition/key/dummy/start";

        MsoJavaProperties props = new MsoJavaProperties();
        props.setProperty(CommonConstants.CAMUNDA_URL, "http://localhost:8089");

        RequestClient requestClient = RequestClientFactory.getRequestClient(orchestrationURI, props);
        requestClient.setClient(mockHttpClient);
        HttpResponse response = requestClient.post(reqXML, "reqId", "timeout", "version", null, null);


        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(requestClient.getType(), CommonConstants.CAMUNDA);
        assertEquals(statusCode, HttpStatus.SC_OK);

        props.setProperty (CommonConstants.CAMUNDA_AUTH, "ABCD1234");
        requestClient = RequestClientFactory.getRequestClient(orchestrationURI, props);
        requestClient.setClient(mockHttpClient);
        response = requestClient.post(null, "reqId", null, null, null, null);
        assertEquals(requestClient.getType(), CommonConstants.CAMUNDA);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    private HttpResponse createResponse(int respStatus,
                                        String respBody) {
        HttpResponse response = new BasicHttpResponse(
                                                      new BasicStatusLine(
                                                                          new ProtocolVersion("HTTP", 1, 1), respStatus, ""));
        response.setStatusCode(respStatus);
        try {
            response.setEntity(new StringEntity(respBody));
            response.setHeader("Content-Type", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }





}
