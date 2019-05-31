package org.onap.so.openstack.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.openstack.exceptions.MsoException;
import com.woorea.openstack.heat.model.Stack;

@RunWith(MockitoJUnitRunner.class)
public class StackStatusHandlerTest {

    @InjectMocks
    StackStatusHandler statusHandler;

    @Mock
    RequestsDbClient requestDBClient;

    @Test
    public final void recordExists_Test() throws MsoException, IOException {
        RequestProcessingData requestProcessingData = new RequestProcessingData();
        requestProcessingData.setValue("testMe");

        doReturn(requestProcessingData).when(requestDBClient)
                .getRequestProcessingDataBySoRequestIdAndNameAndGrouping(null, "id", "stackName");
        Stack latestStack = new Stack();
        latestStack.setId("id");
        latestStack.setStackName("stackName");
        latestStack.setStackStatus("CREATE_COMPLETE");
        latestStack.setStackStatusReason("Stack Finished");

        statusHandler.updateStackStatus(latestStack);
        Mockito.verify(requestDBClient, times(1)).updateRequestProcessingData(requestProcessingData);
        assertNotEquals("testMe", requestProcessingData.getValue());
    }

    @Test
    public final void record_Not_Exists_Test() throws MsoException, IOException {
        ArgumentCaptor<RequestProcessingData> requestCaptor = ArgumentCaptor.forClass(RequestProcessingData.class);
        doReturn(null).when(requestDBClient).getRequestProcessingDataBySoRequestIdAndNameAndGrouping(null, "id",
                "stackName");
        Stack latestStack = new Stack();
        latestStack.setId("id");
        latestStack.setStackName("stackName");
        latestStack.setStackStatus("CREATE_COMPLETE");
        latestStack.setStackStatusReason("Stack Finished");
        statusHandler.updateStackStatus(latestStack);
        Mockito.verify(requestDBClient, times(1)).saveRequestProcessingData(requestCaptor.capture());
        RequestProcessingData actualRequest = requestCaptor.getValue();
        assertEquals("id", actualRequest.getGroupingId());
        assertEquals("StackInformation", actualRequest.getTag());
        assertEquals("stackName", actualRequest.getName());
        assertNotNull(actualRequest.getValue());

    }


}
