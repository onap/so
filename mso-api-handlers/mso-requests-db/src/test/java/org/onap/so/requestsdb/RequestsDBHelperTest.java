package org.onap.so.requestsdb;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;

@RunWith(MockitoJUnitRunner.class)
public class RequestsDBHelperTest {

    @InjectMocks
    private RequestsDBHelper requestsDBHelper;

    @Mock
    private RequestsDbClient requestsDbClient;

    @Test
    public void updateInfraSuccessCompletion() {

        when(requestsDbClient.getInfraActiveRequestbyRequestId(any())).thenReturn(new InfraActiveRequests());

        requestsDBHelper.updateInfraSuccessCompletion("messageText", "requestId", "operationalEnvId");

        ArgumentCaptor<InfraActiveRequests> infraActiveRequests = ArgumentCaptor.forClass(InfraActiveRequests.class);

        verify(requestsDbClient, times(1)).save(infraActiveRequests.capture());
        assertEquals("COMPLETE", infraActiveRequests.getValue().getRequestStatus());
        assertEquals("APIH", infraActiveRequests.getValue().getLastModifiedBy());
        assertEquals(Long.valueOf(100), infraActiveRequests.getValue().getProgress());
        assertEquals("SUCCESSFUL, operationalEnvironmentId - operationalEnvId; Success Message: messageText",
                infraActiveRequests.getValue().getStatusMessage());
        assertEquals("operationalEnvId", infraActiveRequests.getValue().getOperationalEnvId());
    }

    @Test
    public void updateInfraFailureCompletion() {

        when(requestsDbClient.getInfraActiveRequestbyRequestId(any())).thenReturn(new InfraActiveRequests());

        requestsDBHelper.updateInfraFailureCompletion("messageText", "requestId", "operationalEnvId");

        ArgumentCaptor<InfraActiveRequests> infraActiveRequests = ArgumentCaptor.forClass(InfraActiveRequests.class);
        verify(requestsDbClient, times(1)).save(infraActiveRequests.capture());
        assertEquals("FAILED", infraActiveRequests.getValue().getRequestStatus());
        assertEquals("APIH", infraActiveRequests.getValue().getLastModifiedBy());
        assertEquals(Long.valueOf(100), infraActiveRequests.getValue().getProgress());
        assertEquals("FAILURE, operationalEnvironmentId - operationalEnvId; Error message: messageText",
                infraActiveRequests.getValue().getStatusMessage());
        assertEquals("operationalEnvId", infraActiveRequests.getValue().getOperationalEnvId());

    }
}
