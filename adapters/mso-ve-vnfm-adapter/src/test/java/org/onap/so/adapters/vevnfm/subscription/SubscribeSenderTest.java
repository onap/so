package org.onap.so.adapters.vevnfm.subscription;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubscribeSenderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SubscribeSender sender;

    @Test
    public void testSuccess() {
        final ResponseEntity<String> response = ResponseEntity.status(HttpStatus.CREATED).body("{}");
        assertTrue(testingSend(response));
    }

    @Test
    public void testFailure() {
        final ResponseEntity<String> response = new ResponseEntity(HttpStatus.BAD_REQUEST);
        assertFalse(testingSend(response));
    }

    private boolean testingSend(final ResponseEntity<String> response) {
        // given
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(response);

        final LccnSubscriptionRequest request = new LccnSubscriptionRequest();

        // when
        final boolean done = sender.send(request);

        // then
        verify(restTemplate, times(1))
                .exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));

        return done;
    }
}
