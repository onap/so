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
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.serviceinstancebeans.InstanceReferences;
import org.onap.so.serviceinstancebeans.Request;
import org.onap.so.serviceinstancebeans.RequestStatus;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationRequestsUnitTest {

    @Spy
    private OrchestrationRequests orchestrationRequests;

    private static final String REQUEST_ID = "7cb9aa56-dd31-41e5-828e-d93027d4ebba";
    private static final String SERVICE_INSTANCE_ID = "7cb9aa56-dd31-41e5-828e-d93027d4ebbb";
    private static final String ORIGINAL_REQUEST_ID = "8f2d38a6-7c20-465a-bd7e-075645f1394b";
    private static final String SERVICE = "service";
    private InfraActiveRequests iar;
    boolean includeCloudRequest = false;

    @Before
    public void setup() {
        iar = new InfraActiveRequests();
        iar.setRequestScope(SERVICE);
        iar.setRequestId(REQUEST_ID);
        iar.setServiceInstanceId(SERVICE_INSTANCE_ID);
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

        Request result = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest);
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

        Request result = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest);
        assertThat(result, sameBeanAs(expected));
    }

}
