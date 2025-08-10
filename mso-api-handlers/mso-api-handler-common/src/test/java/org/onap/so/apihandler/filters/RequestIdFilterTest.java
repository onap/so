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

package org.onap.so.apihandler.filters;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandlerinfra.exceptions.DuplicateRequestIdException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.RequestError;
import org.onap.so.serviceinstancebeans.ServiceException;
import org.slf4j.MDC;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class RequestIdFilterTest {

    @Mock
    private ContainerRequestContext mockContext;

    @Mock
    private RequestsDbClient requestsDbClient;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    @Spy
    private RequestIdFilter requestIdFilter;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String REQUEST_ID = "32807a28-1a14-4b88-b7b3-2950918aa769";
    private ObjectMapper mapper = new ObjectMapper();

    private RequestError getRequestError() {
        RequestError requestError = new RequestError();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        ServiceException serviceException = new ServiceException();
        serviceException.setMessageId("SVC0002");
        serviceException.setText(
                "RequestId: 32807a28-1a14-4b88-b7b3-2950918aa769 already exists in the RequestDB InfraActiveRequests table");
        requestError.setServiceException(serviceException);
        return requestError;
    }

    @Test
    public void filterTestInfra() throws IOException {
        String error = mapper.writeValueAsString(getRequestError());
        String requestId = REQUEST_ID;
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);

        // ExpectedRecord InfraActiveRequests
        InfraActiveRequests infraActiveRequests = new InfraActiveRequests();
        infraActiveRequests.setRequestId(REQUEST_ID);

        doReturn(infraActiveRequests).when(requestsDbClient).getInfraActiveRequestbyRequestId(requestId);
        doReturn(error).when(requestIdFilter).createRequestError(REQUEST_ID, "InfraActiveRequests");
        doReturn("/onap/so/infra/serviceInstantiation/v7/serviceInstances").when(uriInfo).getPath();
        doReturn(uriInfo).when(mockContext).getUriInfo();

        thrown.expect(DuplicateRequestIdException.class);
        thrown.expectMessage("HTTP 400 Bad Request");


        requestIdFilter.filter(mockContext);
    }


    @Test
    public void filterTestInfraSkipRequestIdLookup() throws IOException {
        String requestId = REQUEST_ID;
        MDC.put(ONAPLogConstants.MDCs.REQUEST_ID, requestId);

        // ExpectedRecord InfraActiveRequests
        InfraActiveRequests infraActiveRequests = new InfraActiveRequests();
        infraActiveRequests.setRequestId(REQUEST_ID);

        doReturn("onap/so/infra/orchestrationRequests/v7/" + REQUEST_ID).when(uriInfo).getPath();
        doReturn(uriInfo).when(mockContext).getUriInfo();

        verify(requestsDbClient, never()).getInfraActiveRequestbyRequestId(REQUEST_ID);

        requestIdFilter.filter(mockContext);
    }


    @Test
    public void createRequestErrorTest() throws IOException {
        RequestError requestError = getRequestError();
        String result = requestIdFilter.createRequestError(REQUEST_ID, "InfraActiveRequests");
        RequestError resultingError = mapper.readValue(result, RequestError.class);

        assertThat(resultingError, sameBeanAs(requestError));
    }
}
