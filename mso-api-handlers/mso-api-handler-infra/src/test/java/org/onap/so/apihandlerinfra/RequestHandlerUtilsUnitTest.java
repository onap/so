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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.db.request.beans.InfraActiveRequests;

@RunWith(MockitoJUnitRunner.class)
public class RequestHandlerUtilsUnitTest {

    @Spy
    private RequestHandlerUtils requestHandler;

    private static final String CURRENT_REQUEST_ID = "eca3a1b1-43ab-457e-ab1c-367263d148b4";
    private static final String SERVICE_INSTANCE_ID = "00032ab7-na18-42e5-965d-8ea592502018";
    private final Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
    private String requestUri =
            "http:localhost:6746/onap/so/infra/orchestrationRequests/v7/00032ab7-na18-42e5-965d-8ea592502019/resume";
    private InfraActiveRequests infraActiveRequest = new InfraActiveRequests();
    private InfraActiveRequests currentActiveRequest = new InfraActiveRequests();
    private InfraActiveRequests currentActiveRequestIARNull = new InfraActiveRequests();

    public String getRequestBody(String request) throws IOException {
        request = "src/test/resources/ResumeOrchestrationRequest" + request;
        return new String(Files.readAllBytes(Paths.get(request)));
    }

    @Before
    public void setup() throws IOException {
        setInfraActiveRequest();
        setCurrentActiveRequest();
        setCurrentActiveRequestNullInfraActive();
    }

    private void setInfraActiveRequest() throws IOException {
        infraActiveRequest.setTenantId("tenant-id");
        infraActiveRequest.setRequestBody(getRequestBody("/RequestBody.json"));
        infraActiveRequest.setAicCloudRegion("cloudRegion");
        infraActiveRequest.setRequestScope("service");
        infraActiveRequest.setServiceInstanceId(SERVICE_INSTANCE_ID);
        infraActiveRequest.setServiceInstanceName("serviceInstanceName");
        infraActiveRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        infraActiveRequest.setRequestAction(Action.createInstance.toString());
        infraActiveRequest.setServiceType("serviceType");
    }

    private void setCurrentActiveRequest() throws IOException {
        currentActiveRequest.setRequestId(CURRENT_REQUEST_ID);
        currentActiveRequest.setSource("VID");
        currentActiveRequest.setStartTime(startTimeStamp);
        currentActiveRequest.setTenantId("tenant-id");
        currentActiveRequest.setRequestBody(getRequestBody("/RequestBody.json"));
        currentActiveRequest.setAicCloudRegion("cloudRegion");
        currentActiveRequest.setRequestScope("service");
        currentActiveRequest.setServiceInstanceId(SERVICE_INSTANCE_ID);
        currentActiveRequest.setServiceInstanceName("serviceInstanceName");
        currentActiveRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        currentActiveRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        currentActiveRequest.setRequestAction(Action.createInstance.toString());
        currentActiveRequest.setRequestUrl(requestUri);
        currentActiveRequest.setRequestorId("xxxxxx");
        currentActiveRequest.setProgress(new Long(5));
    }

    private void setCurrentActiveRequestNullInfraActive() throws IOException {
        currentActiveRequestIARNull.setRequestId(CURRENT_REQUEST_ID);
        currentActiveRequestIARNull.setSource("VID");
        currentActiveRequestIARNull.setStartTime(startTimeStamp);
        currentActiveRequestIARNull.setRequestStatus(Status.IN_PROGRESS.toString());
        currentActiveRequestIARNull.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        currentActiveRequestIARNull.setRequestUrl(requestUri);
        currentActiveRequestIARNull.setRequestorId("xxxxxx");
        currentActiveRequestIARNull.setProgress(new Long(5));
    }


    @Test
    public void createNewRecordCopyFromInfraActiveRequestTest() {
        InfraActiveRequests result = requestHandler.createNewRecordCopyFromInfraActiveRequest(infraActiveRequest,
                CURRENT_REQUEST_ID, startTimeStamp, "VID", requestUri, "xxxxxx");
        assertThat(currentActiveRequest, sameBeanAs(result));
    }

    @Test
    public void createNewRecordCopyFromInfraActiveRequestNullIARTest() {
        InfraActiveRequests result = requestHandler.createNewRecordCopyFromInfraActiveRequest(null, CURRENT_REQUEST_ID,
                startTimeStamp, "VID", requestUri, "xxxxxx");
        assertThat(currentActiveRequestIARNull, sameBeanAs(result));
    }
}
