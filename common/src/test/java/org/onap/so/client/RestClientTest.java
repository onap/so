/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.utils.TargetEntity;

@RunWith(MockitoJUnitRunner.class)
public class RestClientTest {


    private final HttpClientFactory httpClientFactory = new HttpClientFactory();
    @Mock
    private RestProperties props;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void retries() throws Exception {
        RestClient spy = buildSpy();
        doThrow(new WebApplicationException(new SocketTimeoutException())).when(spy).buildRequest(any(String.class),
                ArgumentMatchers.isNull());
        try {
            spy.get();
        } catch (Exception e) {
            // ignore this exception for this test
        }
        verify(spy, times(3)).buildRequest(any(String.class), ArgumentMatchers.isNull());

    }

    @Test
    public void exceptionDoNotRetry() throws Exception {
        RestClient spy = buildSpy();
        doThrow(new WebApplicationException(new NotFoundException())).when(spy).buildRequest(any(String.class),
                ArgumentMatchers.isNull());
        try {
            spy.get();
        } catch (Exception e) {
            // we expect an exception, ignore it
        }
        verify(spy, times(1)).buildRequest(any(String.class), ArgumentMatchers.isNull());

    }

    private RestClient buildSpy() throws MalformedURLException, IllegalArgumentException, UriBuilderException {
        RestClient client = httpClientFactory.newJsonClient(UriBuilder.fromUri("http://localhost/test").build().toURL(),
                TargetEntity.BPMN);

        return spy(client);
    }
}
