package org.onap.so.db.request.client;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


@RunWith(MockitoJUnitRunner.class)
public class RequestDbClientTest {
    @Spy
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

    @Test
    public void updateInfraActiveRequestsTest() {
        InfraActiveRequests request = new InfraActiveRequests();
        request.setRequestId("1");
        URI uri = URI.create("/infraActiveRequests/1");
        requestsDbClient.patchInfraActiveRequests(request);
        Mockito.verify(restTemplate, times(1)).exchange(eq(uri), eq(HttpMethod.PATCH), isA(HttpEntity.class),
                eq(String.class));
    }

    @Test
    public void testGetRequestProcessingDataBySoRequestIdAndIsInternalData() {
        Mockito.doReturn("host").when(requestsDbClient).getEndpoint();
        URI uri = URI.create(UriBuilder
                .fromUri("host/requestProcessingData/search/findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc")
                .queryParam("SO_REQUEST_ID", "12345").queryParam("IS_INTERNAL_DATA", false).build().toString());
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), isA(HttpEntity.class),
                isA(ParameterizedTypeReference.class))).thenReturn(
                        new ResponseEntity<List<RequestProcessingData>>(new ArrayList<RequestProcessingData>(),
                                HttpStatus.NOT_FOUND));
        assertTrue(requestsDbClient.getExternalRequestProcessingDataBySoRequestId("12345").isEmpty());


    }

}
