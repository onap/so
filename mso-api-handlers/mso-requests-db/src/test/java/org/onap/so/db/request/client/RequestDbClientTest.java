package org.onap.so.db.request.client;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.filter.spring.SpringClientPayloadFilter;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.beans.RequestProcessingData;
import org.onap.so.logging.jaxrs.filter.SOSpringClientFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import uk.co.blackpepper.bowman.ClientFactory;
import uk.co.blackpepper.bowman.Configuration;


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
        requestsDbClient.getExternalRequestProcessingDataBySoRequestId("12345");
        URI uri = URI.create(UriBuilder
                .fromUri("host/requestProcessingData/search/findBySoRequestIdAndIsDataInternalOrderByGroupingIdDesc")
                .queryParam("SO_REQUEST_ID", "12345").queryParam("IS_INTERNAL_DATA", false).build().toString());
        Mockito.verify(restTemplate, times(1)).exchange(eq(uri), eq(HttpMethod.GET), isA(HttpEntity.class),
                eq(RequestProcessingData[].class));
    }

}
