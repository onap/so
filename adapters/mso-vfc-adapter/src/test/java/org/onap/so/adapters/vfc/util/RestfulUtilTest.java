/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (c) 2019 Samsung. All rights reserved.
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

package org.onap.so.adapters.vfc.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vfc.model.RestfulResponse;
import org.springframework.http.HttpStatus;
import javax.ws.rs.HttpMethod;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestfulUtilTest {

    @InjectMocks
    @Spy
    private RestfulUtil restfulUtil;

    @Mock
    private HttpClient client;

    private HttpEntity httpEntity;
    private HttpResponse httpResponse;
    private StatusLine statusLine;
    private Header httpResponseHeader;

    @Before
    public void setUp() {
        httpEntity = mock(HttpEntity.class);
        httpResponse = mock(HttpResponse.class);
        statusLine = mock(StatusLine.class);
        httpResponseHeader = mock(Header.class);
    }

    private void sendInit() throws IOException {

        Header[] headerList = new BasicHeader[2];
        headerList[0] = new BasicHeader("Content-Type", "application/json");
        headerList[1] = new BasicHeader("cache-control", "no-cache");
        doReturn("https://testHost/").when(restfulUtil).getMsbHost();

        when(statusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpResponse.getAllHeaders()).thenReturn(headerList);
    }

    @Test
    public void sendGet() throws Exception {

        sendInit();

        ByteArrayInputStream responseStream = new ByteArrayInputStream(new String("GET").getBytes());
        when(client.execute(any(HttpGet.class))).thenReturn(httpResponse);
        when(httpEntity.getContent()).thenReturn(responseStream);

        RestfulResponse restfulResponse = restfulUtil.send("test", HttpMethod.GET, "some request content");

        assertEquals(HttpStatus.OK.value(), restfulResponse.getStatus());
        assertEquals("GET", restfulResponse.getResponseContent());

    }

    @Test
    public void sendPost() throws Exception {

        sendInit();


        ByteArrayInputStream responseStream = new ByteArrayInputStream(new String("POST").getBytes());
        when(client.execute(any(HttpPost.class))).thenReturn(httpResponse);
        when(httpEntity.getContent()).thenReturn(responseStream);

        RestfulResponse restfulResponse = restfulUtil.send("test", HttpMethod.POST, "some request content");

        assertEquals(HttpStatus.OK.value(), restfulResponse.getStatus());
        assertEquals("POST", restfulResponse.getResponseContent());

    }

    @Test
    public void sendPut() throws Exception {

        sendInit();

        ByteArrayInputStream responseStream = new ByteArrayInputStream(new String("PUT").getBytes());
        when(client.execute(any(HttpPut.class))).thenReturn(httpResponse);
        when(httpEntity.getContent()).thenReturn(responseStream);

        RestfulResponse restfulResponse = restfulUtil.send("test", HttpMethod.PUT, "some request content");

        assertEquals(HttpStatus.OK.value(), restfulResponse.getStatus());
        assertEquals("PUT", restfulResponse.getResponseContent());

    }

    @Test
    public void sendDelete() throws Exception {

        sendInit();

        ByteArrayInputStream responseStream = new ByteArrayInputStream(new String("DELETE").getBytes());
        when(client.execute(any(HttpDelete.class))).thenReturn(httpResponse);
        when(httpEntity.getContent()).thenReturn(responseStream);

        RestfulResponse restfulResponse = restfulUtil.send("test", HttpMethod.DELETE, "some request content");

        assertEquals(HttpStatus.OK.value(), restfulResponse.getStatus());
        assertEquals("DELETE", restfulResponse.getResponseContent());

    }

    @Test
    public void sendOptions() throws Exception {

        doReturn("https://testHost/").when(restfulUtil).getMsbHost();

        RestfulResponse restfulResponse = restfulUtil.send("test", HttpMethod.OPTIONS, "some request content");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), restfulResponse.getStatus());
        assertEquals("Error processing request to VFC", restfulResponse.getResponseContent());

    }

}
