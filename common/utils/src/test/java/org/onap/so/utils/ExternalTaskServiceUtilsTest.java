package org.onap.so.utils;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.camunda.bpm.client.ExternalTaskClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExternalTaskServiceUtilsTest {

    @Spy
    @InjectMocks
    private ExternalTaskServiceUtils utils = new ExternalTaskServiceUtils();

    @Mock
    private ExternalTaskClient actualClient1;

    @Mock
    private ExternalTaskClient actualClient2;

    @Mock
    private ExternalTaskClient actualClient3;

    @Mock
    private ExternalTaskClient actualClient4;

    @Test
    public void testCheckActiveClients() {
        Set<ExternalTaskClient> taskClients = ConcurrentHashMap.newKeySet();
        taskClients.add(actualClient1);
        taskClients.add(actualClient2);
        taskClients.add(actualClient3);
        taskClients.add(actualClient4);
        when(utils.getClients()).thenReturn(taskClients);
        when(actualClient1.isActive()).thenReturn(false);
        when(actualClient2.isActive()).thenReturn(true);
        when(actualClient3.isActive()).thenReturn(false);
        when(actualClient4.isActive()).thenReturn(true);
        utils.checkAllClientsActive();
        verify(actualClient1, times(1)).isActive();
        verify(actualClient2, times(1)).isActive();
        verify(actualClient3, times(1)).isActive();
        verify(actualClient4, times(1)).isActive();
        verify(actualClient1, times(1)).start();
        verify(actualClient3, times(1)).start();
    }

}
