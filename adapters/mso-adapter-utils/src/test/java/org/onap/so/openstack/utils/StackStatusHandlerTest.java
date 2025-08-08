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

package org.onap.so.openstack.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import com.woorea.openstack.heat.model.Stack;

@RunWith(MockitoJUnitRunner.class)
public class StackStatusHandlerTest {

    @InjectMocks
    StackStatusHandler statusHandler;

    @Mock
    RequestsDbClient requestDBClient;

    private String getRequestId() {
        return UUID.randomUUID().toString();
    }

    @Test
    public final void recordExists_Test() {
        RequestProcessingData requestProcessingData = new RequestProcessingData();
        requestProcessingData.setValue("testMe");

        String requestId = getRequestId();
        doReturn(requestProcessingData).when(requestDBClient)
                .getRequestProcessingDataBySoRequestIdAndNameAndGrouping(requestId, "stackName", "id");
        Stack latestStack = new Stack();
        latestStack.setId("id");
        latestStack.setStackName("stackName");
        latestStack.setStackStatus("CREATE_COMPLETE");
        latestStack.setStackStatusReason("Stack Finished");

        statusHandler.updateStackStatus(latestStack, requestId);
        Mockito.verify(requestDBClient, times(1)).updateRequestProcessingData(requestProcessingData);
        assertNotEquals("testMe", requestProcessingData.getValue());
    }

    @Test
    public final void record_Not_Exists_Test() {
        String requestId = getRequestId();
        ArgumentCaptor<RequestProcessingData> requestCaptor = ArgumentCaptor.forClass(RequestProcessingData.class);
        doReturn(null).when(requestDBClient).getRequestProcessingDataBySoRequestIdAndNameAndGrouping(requestId,
                "stackName", "id");
        Stack latestStack = new Stack();
        latestStack.setId("id");
        latestStack.setStackName("stackName");
        latestStack.setStackStatus("CREATE_COMPLETE");
        latestStack.setStackStatusReason("Stack Finished");
        statusHandler.updateStackStatus(latestStack, requestId);
        Mockito.verify(requestDBClient, times(1)).saveRequestProcessingData(requestCaptor.capture());
        RequestProcessingData actualRequest = requestCaptor.getValue();
        assertEquals("id", actualRequest.getGroupingId());
        assertEquals("StackInformation", actualRequest.getTag());
        assertEquals("stackName", actualRequest.getName());
        assertNotNull(actualRequest.getValue());

    }


}
