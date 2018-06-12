package org.openecomp.mso.client;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.utils.TargetEntity;

@RunWith(MockitoJUnitRunner.class)
public class RestClientTest {

	
	@Mock
	private RestProperties props;
	
	
	@Test
	public void retries() throws Exception {
		RestClient spy = buildSpy();
		RestRequest mockCallable = mock(RestRequest.class);
		when(mockCallable.call()).thenThrow(new WebApplicationException(new SocketTimeoutException()));
		doReturn(mockCallable).when(spy).buildRequest(any(String.class), any(Object.class));
		try {
			spy.get();
		} catch (Exception e) {
			//we expect an exception, ignore it
		}
		verify(mockCallable, times(3)).call();
		
	}
	
	@Test
	public void exceptionDoNotRetry() throws Exception {
		RestClient spy = buildSpy();
		RestRequest mockCallable = mock(RestRequest.class);
		when(mockCallable.call()).thenThrow(new WebApplicationException(new NotFoundException()));
		doReturn(mockCallable).when(spy).buildRequest(any(String.class), any(Object.class));
		try {
			spy.get();
		} catch (Exception e) {
			//we expect an exception, ignore it
		}
		verify(mockCallable, times(1)).call();
		
	}
	private RestClient buildSpy() throws MalformedURLException, IllegalArgumentException, UriBuilderException {
		RestClient client = new HttpClient(UriBuilder.fromUri("http://localhost/test").build().toURL(), "application/json", TargetEntity.BPMN);
		
		return spy(client);
	}
}
