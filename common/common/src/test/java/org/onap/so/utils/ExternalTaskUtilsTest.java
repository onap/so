package org.onap.so.utils;

import static org.junit.Assert.assertEquals;
import org.camunda.bpm.client.task.ExternalTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class ExternalTaskUtilsTest {

    @Mock
    private ExternalTask externalTask;

    @Mock
    private Environment mockenv;

    @InjectMocks
    private ExternalTaskUtils externalTaskUtilsAnony = new ExternalTaskUtils(RetrySequenceLevel.LONG) {

    };

    @Test
    public void retry_sequence_calculation_Test() {
        Mockito.when(mockenv.getProperty("mso.workflow.topics.retryMultiplier", "6000")).thenReturn("6000");
        long firstRetry = externalTaskUtilsAnony.calculateRetryDelay(8);
        assertEquals(6000L, firstRetry);
        long secondRetry = externalTaskUtilsAnony.calculateRetryDelay(7);
        assertEquals(6000L, secondRetry);
        long thirdRetry = externalTaskUtilsAnony.calculateRetryDelay(6);
        assertEquals(12000L, thirdRetry);
        long fourthRetry = externalTaskUtilsAnony.calculateRetryDelay(5);
        assertEquals(18000L, fourthRetry);
        long fifthRetry = externalTaskUtilsAnony.calculateRetryDelay(4);
        assertEquals(30000L, fifthRetry);
        long sixRetry = externalTaskUtilsAnony.calculateRetryDelay(3);
        assertEquals(48000L, sixRetry);
        long seventhRetry = externalTaskUtilsAnony.calculateRetryDelay(2);
        assertEquals(78000L, seventhRetry);
        long eigthRetry = externalTaskUtilsAnony.calculateRetryDelay(1);
        assertEquals(120000L, eigthRetry);
    }

    @Test
    public void retry_sequence_Test() {
        Mockito.when(mockenv.getProperty("mso.workflow.topics.retryMultiplier", "6000")).thenReturn("6000");
        long firstRetry = externalTaskUtilsAnony.calculateRetryDelay(8);
        assertEquals(6000L, firstRetry);
        long secondRetry = externalTaskUtilsAnony.calculateRetryDelay(7);
        assertEquals(6000L, secondRetry);
        long thirdRetry = externalTaskUtilsAnony.calculateRetryDelay(6);
        assertEquals(12000L, thirdRetry);
        long fourthRetry = externalTaskUtilsAnony.calculateRetryDelay(5);
        assertEquals(18000L, fourthRetry);
        long fifthRetry = externalTaskUtilsAnony.calculateRetryDelay(4);
        assertEquals(30000L, fifthRetry);
        long sixRetry = externalTaskUtilsAnony.calculateRetryDelay(3);
        assertEquals(48000L, sixRetry);
        long seventhRetry = externalTaskUtilsAnony.calculateRetryDelay(2);
        assertEquals(78000L, seventhRetry);
        long eigthRetry = externalTaskUtilsAnony.calculateRetryDelay(1);
        assertEquals(120000L, eigthRetry);
    }

}
