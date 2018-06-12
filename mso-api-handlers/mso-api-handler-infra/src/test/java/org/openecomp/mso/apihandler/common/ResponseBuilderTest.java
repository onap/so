package org.openecomp.mso.apihandler.common;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.openecomp.mso.apihandlerinfra.BaseTest;
import org.openecomp.mso.apihandlerinfra.exceptions.ApiException;
import org.springframework.beans.factory.annotation.Autowired;

public class ResponseBuilderTest extends BaseTest {

	@Autowired
	private ResponseBuilder builder;
	
    @Test
    public void testBuildResponseResponse () throws ApiException {
    	
    	String requestId = null;
    	String apiVersion = "1";
    	String jsonResponse = "Successfully started the process";
        
        Response response = builder.buildResponse(HttpStatus.SC_ACCEPTED, requestId, jsonResponse, apiVersion);
        
        assertEquals(202, response.getStatus());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("1.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        
    }
    
    @Test
    public void testBuildResponseVersion () throws ApiException {
    	
    	String requestId = "123456-67889";
    	String apiVersion = "v5";
    	String jsonResponse = "Successfully started the process";       
        
        Response response = builder.buildResponse(HttpStatus.SC_CREATED, requestId, jsonResponse, apiVersion);
        
        assertEquals(201, response.getStatus());
        assertEquals("application/json", response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        assertEquals("0", response.getHeaders().get("X-MinorVersion").get(0));
        assertEquals("0", response.getHeaders().get("X-PatchVersion").get(0));
        assertEquals("5.0.0", response.getHeaders().get("X-LatestVersion").get(0));
        
    }
    
}
