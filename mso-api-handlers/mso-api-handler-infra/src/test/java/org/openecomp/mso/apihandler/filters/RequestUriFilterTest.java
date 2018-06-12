/* ============LICENSE_END=========================================================
 */

package org.openecomp.mso.apihandler.filters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.BaseTest;



public class RequestUriFilterTest extends BaseTest{
	
	@Test
	public void filterTest() throws IOException {
		RequestUriFilter URIFilter = new RequestUriFilter();
		URI baseURI = URI.create("http://localhost:58879/");
		String requestURI = "onap/so/infra/serviceInstances/v5";
		
		ContainerRequestContext mockContext = mock(ContainerRequestContext.class);
		UriInfo mockInfo = mock(UriInfo.class);
	
		when(mockContext.getUriInfo()).thenReturn(mockInfo);
		when(mockInfo.getBaseUri()).thenReturn(baseURI);
		when(mockInfo.getPath()).thenReturn(requestURI);
	
		
		URIFilter.filter(mockContext);
		assertEquals("http://localhost:58879/onap/so/infra/serviceInstantiation/v5/serviceInstances", URIFilter.getRequestUri());
	}
}