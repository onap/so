package org.onap.so.db.request.client;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import java.net.URI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;


@RunWith(MockitoJUnitRunner.class)
public class RequestDbClientTest {

    @InjectMocks
    protected RequestsDbClient requestsDbClient;

    @Mock
    protected RestTemplate restTemplate;

    @Test
    public void updateRequestProcessingDataTest() {
        RequestProcessingData requestProcessingData = new RequestProcessingData();
        requestProcessingData.setId(1);
        URI uri = URI.create("/requestProcessingData/1");
        requestsDbClient.updateRequestProcessingData(requestProcessingData);
        Mockito.verify(restTemplate, times(1)).put(eq(uri), isA(HttpEntity.class));
    }
}
