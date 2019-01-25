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

package org.onap.so.apihandler.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class CamundaTaskClientTest {

	@Mock
	private Environment env;
    private CamundaTaskClient testedObject = new CamundaTaskClient();
    private HttpClient httpClientMock;
    private static final String JSON_REQUEST = "{\"value1\": \"aaa\",\"value2\": \"bbb\"}";
    private static final String URL_SCHEMA = "http";
    private static final String HOST = "testhost";
    private static final int PORT = 1234;
    private static final String URL_PATH = "/requestMethodSuccessful";
    private static final String URL = URL_SCHEMA + "://" + HOST + ":" + PORT + URL_PATH;
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";

    @Before
    public void init() {
    	when(env.getProperty(eq(CommonConstants.CAMUNDA_AUTH))).thenReturn("E8E19DD16CC90D2E458E8FF9A884CC0452F8F3EB8E321F96038DE38D5C1B0B02DFAE00B88E2CF6E2A4101AB2C011FC161212EE");
        when(env.getProperty(eq(CommonConstants.ENCRYPTION_KEY_PROP))).thenReturn("aa3871669d893c7fb8abbcda31b88b4f");
        testedObject = new CamundaTaskClient();
        httpClientMock = mock(HttpClient.class);
        testedObject.setClient(httpClientMock);
        testedObject.setUrl(URL);
    }

    @Test
    public void postMethodSuccessful() throws IOException {
        ArgumentCaptor<HttpPost> httpPostCaptor = ArgumentCaptor.forClass(HttpPost.class);
        testedObject.post(JSON_REQUEST);
        verify(httpClientMock).execute(httpPostCaptor.capture());
        checkUri(httpPostCaptor.getValue());
        assertThat(httpPostCaptor.getValue().getEntity().getContentType().getValue()).
                isEqualTo(CommonConstants.CONTENT_TYPE_JSON);
        assertThat(getJsonFromEntity(httpPostCaptor.getValue().getEntity())).isEqualTo(JSON_REQUEST);
    }

    @Test
    public void postMethodSuccessfulWithCredentials() throws IOException {
        ArgumentCaptor<HttpPost> httpPostCaptor = ArgumentCaptor.forClass(HttpPost.class);
        testedObject.setProps(env);
        testedObject.post(JSON_REQUEST);
        verify(httpClientMock).execute(httpPostCaptor.capture());
        assertThat(httpPostCaptor.getValue().getHeaders(AUTHORIZATION_HEADER_NAME)).isNotEmpty();
        Assert.assertEquals("Basic YXBpaEJwbW46Y2FtdW5kYS1SMTUxMiE=",httpPostCaptor.getValue().getHeaders(AUTHORIZATION_HEADER_NAME)[0].getValue());
    }

    @Test
    public void getMethodSuccessful() throws IOException {
        ArgumentCaptor<HttpGet> httpGetCaptor = ArgumentCaptor.forClass(HttpGet.class);
        testedObject.get();
        verify(httpClientMock).execute(httpGetCaptor.capture());
        checkUri(httpGetCaptor.getValue());
    }

    @Test
    public void getMethodSuccessfulWithCredentials() throws IOException {
        ArgumentCaptor<HttpGet> httpGetCaptor = ArgumentCaptor.forClass(HttpGet.class);
        testedObject.setUrl(URL);
        testedObject.setProps(env);
        testedObject.get();
        verify(httpClientMock).execute(httpGetCaptor.capture());
        assertThat(httpGetCaptor.getValue().getHeaders(AUTHORIZATION_HEADER_NAME)).isNotEmpty();
        Assert.assertEquals("Basic YXBpaEJwbW46Y2FtdW5kYS1SMTUxMiE=",httpGetCaptor.getValue().getHeaders(AUTHORIZATION_HEADER_NAME)[0].getValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void postMethodUnsupported() {
        testedObject.post("", "", "", "", "", "");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void postMethodUnsupported2() {
        testedObject.post(new RequestClientParameter.Builder().build());
    }

    private void checkUri(HttpRequestBase httpRequestBase) {
        assertThat(httpRequestBase.getURI().getScheme()).isEqualTo(URL_SCHEMA);
        assertThat(httpRequestBase.getURI().getHost()).isEqualTo(HOST);
        assertThat(httpRequestBase.getURI().getPort()).isEqualTo(PORT);
        assertThat(httpRequestBase.getURI().getPath()).isEqualTo(URL_PATH);
    }

    private String getJsonFromEntity(HttpEntity httpEntity) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(httpEntity.getContent()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

}