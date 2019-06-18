/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.InstanceReferences;
import org.onap.so.serviceinstancebeans.Request;
import org.onap.so.serviceinstancebeans.RequestStatus;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationRequestsUnitTest {
    @Mock
    private RequestsDbClient requestDbClient;
    @Mock
    private MsoRequest msoRequest;
    @Mock
    private ResponseBuilder builder;
    @Mock
    private Response response;
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    @Spy
    private OrchestrationRequests orchestrationRequests;

    private static final String REQUEST_ID = "7cb9aa56-dd31-41e5-828e-d93027d4ebba";
    private static final String SERVICE_INSTANCE_ID = "7cb9aa56-dd31-41e5-828e-d93027d4ebbb";
    private static final String ORIGINAL_REQUEST_ID = "8f2d38a6-7c20-465a-bd7e-075645f1394b";
    private static final String SERVICE = "service";
    private static final String EXT_SYSTEM_ERROR_SOURCE = "external system error source";
    private InfraActiveRequests iar;
    boolean includeCloudRequest = false;
    boolean extSystemErrorSource = false;
    private static final String SERVICE_INSTANCE_NAME = "serviceInstanceName";
    private static final String VNF_ID = "00032ab7-na18-42e5-965d-8ea592502017";
    private static final String VFMODULE_ID = "00032ab7-na18-42e5-965d-8ea592502016";
    private static final String NETWORK_ID = "00032ab7-na18-42e5-965d-8ea592502015";
    private static final String VOLUME_GROUP_ID = "00032ab7-na18-42e5-965d-8ea592502014";
    private static final String VNF_NAME = "vnfName";
    private static final String VFMODULE_NAME = "vfModuleName";
    private static final String NETWORK_NAME = "networkName";
    private static final String VOLUME_GROUP_NAME = "volumeGroupName";
    private static final String VERSION = "v7";
    List<org.onap.so.db.request.beans.RequestProcessingData> requestProcessingData = new ArrayList<>();

    @Before
    public void setup() {
        iar = new InfraActiveRequests();
        iar.setRequestScope(SERVICE);
        iar.setRequestId(REQUEST_ID);
        iar.setServiceInstanceId(SERVICE_INSTANCE_ID);
        when(requestDbClient.getInfraActiveRequestbyRequestId(Mockito.eq(REQUEST_ID))).thenReturn(iar);
        when(requestDbClient.getRequestProcessingDataBySoRequestId(Mockito.eq(REQUEST_ID)))
                .thenReturn(requestProcessingData);

        when(builder.buildResponse(Mockito.eq(HttpStatus.SC_OK), Mockito.eq(REQUEST_ID), any(Object.class),
                any(String.class))).thenReturn(response);
    }

    @Test
    public void mapInfraActiveRequestToRequestWithOriginalRequestIdTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setOriginalRequestId(ORIGINAL_REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        iar.setOriginalRequestId(ORIGINAL_REQUEST_ID);

        Request result =
                orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest, extSystemErrorSource);
        assertThat(result, sameBeanAs(expected));
    }

    @Test
    public void mapInfraActiveRequestToRequestOriginalRequestIdNullTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        Request result =
                orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest, extSystemErrorSource);
        assertThat(result, sameBeanAs(expected));
    }

    @Test
    public void mapExtSystemErrorSourceToRequestFalseTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        extSystemErrorSource = false;
        includeCloudRequest = false;

        Request actual =
                orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest, extSystemErrorSource);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapExtSystemErrorSourceToRequestTrueTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setExtSystemErrorSource(EXT_SYSTEM_ERROR_SOURCE);

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        extSystemErrorSource = true;
        includeCloudRequest = false;
        iar.setExtSystemErrorSource(EXT_SYSTEM_ERROR_SOURCE);

        Request actual =
                orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest, extSystemErrorSource);
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapExtSystemErrorSourceToRequestMethodInvokedTest() throws ApiException, IOException {
        extSystemErrorSource = true;
        includeCloudRequest = false;
        orchestrationRequests.getOrchestrationRequest(REQUEST_ID, VERSION, includeCloudRequest, extSystemErrorSource);

        verify(orchestrationRequests, times(1)).mapExtSystemErrorSourceToRequest(Mockito.eq(iar), Mockito.any(),
                Mockito.eq(extSystemErrorSource));
    }

    @Test
    public void requestStatusExtSystemErrorSourceTest() {
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setExtSystemErrorSource(EXT_SYSTEM_ERROR_SOURCE);
        assertThat(requestStatus.getExtSystemErrorSource(), is(equalTo(EXT_SYSTEM_ERROR_SOURCE)));
    }
}
