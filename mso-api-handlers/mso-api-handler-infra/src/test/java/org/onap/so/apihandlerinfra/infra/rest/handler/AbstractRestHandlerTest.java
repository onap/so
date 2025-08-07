/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra.infra.rest.handler;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.Mockito.doReturn;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.serviceinstancebeans.RequestReferences;
import org.onap.so.serviceinstancebeans.ServiceInstancesResponse;

@RunWith(MockitoJUnitRunner.class)
public class AbstractRestHandlerTest {

    @Spy
    AbstractRestHandler restHandler;

    @Mock
    ContainerRequestContext mockRequestContext;

    @Test
    public void test_createResponse() throws MalformedURLException {
        ServiceInstancesResponse expectedResponse = new ServiceInstancesResponse();
        RequestReferences requestReferences = new RequestReferences();
        URL selfLinkURL = new URL("http://localhost:8080/v1");
        requestReferences.setInstanceId("instanceId");
        requestReferences.setRequestId("requestId");
        requestReferences.setRequestSelfLink(selfLinkURL);
        expectedResponse.setRequestReferences(requestReferences);

        doReturn("http://localhost:8080/v1").when(restHandler).getRequestUri(mockRequestContext);
        doReturn(Optional.of(selfLinkURL)).when(restHandler).buildSelfLinkUrl("http://localhost:8080/v1", "requestId");
        ServiceInstancesResponse actualResponse =
                restHandler.createResponse("instanceId", "requestId", mockRequestContext);
        assertThat(actualResponse, sameBeanAs(expectedResponse));
    }

    @Test
    public void test_buildSelfLinkUrl() throws MalformedURLException {
        String initialLink = "http://some.domain.com:30277/onap/so/infra/serviceInstantiation/v7/serviceInstances";
        String requestId = "4d0437c3-ee48-4361-a4f7-e1613c82493a";
        Optional<URL> expectedLink = Optional.of(new URL(
                "http://some.domain.com:30277/onap/so/infra/orchestrationRequests/v7/4d0437c3-ee48-4361-a4f7-e1613c82493a"));
        Optional<URL> resultURL = restHandler.buildSelfLinkUrl(initialLink, requestId);

        assertThat(resultURL, sameBeanAs(expectedLink));
    }
}
