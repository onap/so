/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client.grm;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import ch.qos.logback.classic.spi.ILoggingEvent;

import org.apache.log4j.MDC;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.client.grm.beans.OperationalInfo;
import org.onap.so.client.grm.beans.Property;
import org.onap.so.client.grm.beans.ServiceEndPoint;
import org.onap.so.client.grm.beans.ServiceEndPointList;
import org.onap.so.client.grm.beans.ServiceEndPointLookupRequest;
import org.onap.so.client.grm.beans.ServiceEndPointRequest;
import org.onap.so.client.grm.beans.Version;
import org.onap.so.client.grm.exceptions.GRMClientCallFailed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.onap.so.utils.TestAppender;

public class GRMClientTest {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(47389));
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private static final String uuidRegex = "(?i)^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-5][0-9a-f]{3}-?[089ab][0-9a-f]{3}-?[0-9a-f]{12}$";
	
	@BeforeClass
	public static void setUp() throws Exception {
		System.setProperty("mso.config.path", "src/test/resources");
	}
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testFind() throws Exception {
        TestAppender.events.clear();
		String endpoints = getFileContentsAsString("__files/grm/endpoints.json");
		wireMockRule.stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/findRunning"))
			.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody(endpoints)));

		MDC.put(ONAPLogConstants.MDCs.SERVICE_NAME, "/test");
		GRMClient client = new GRMClient();
		ServiceEndPointList sel = client.findRunningServices("TEST.ECOMP_PSL.*", 1, "TEST");
		List<ServiceEndPoint> list = sel.getServiceEndPointList();
		assertEquals(3, list.size());
		
		boolean foundInvoke = false;
		boolean foundInvokeReturn = false;
        for(ILoggingEvent logEvent : TestAppender.events)
            if(logEvent.getLoggerName().equals("org.onap.so.logging.jaxrs.filter.JaxRsClientLogging") &&
                    logEvent.getMarker().getName().equals("INVOKE")
                    ){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INVOCATION_ID));
                assertEquals("GRM",mdc.get("TargetEntity"));
                assertEquals("INPROGRESS",mdc.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
                foundInvoke=true;
            }else if(logEvent.getLoggerName().equals("org.onap.so.logging.jaxrs.filter.JaxRsClientLogging") &&
                    logEvent.getMarker()!= null && logEvent.getMarker().getName().equals("INVOKE_RETURN")){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INVOCATION_ID));
                assertEquals("200",mdc.get(ONAPLogConstants.MDCs.RESPONSE_CODE));
                assertEquals("COMPLETED",mdc.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
                foundInvokeReturn=true;
            }
        
        if(!foundInvoke)
            fail("INVOKE Marker not found");
        
        if(!foundInvokeReturn)
            fail("INVOKE RETURN Marker not found");
        
        verify(postRequestedFor(urlEqualTo("/GRMLWPService/v1/serviceEndPoint/findRunning"))
                .withHeader(ONAPLogConstants.Headers.INVOCATION_ID.toString(), matching(uuidRegex))
                        .withHeader(ONAPLogConstants.Headers.REQUEST_ID.toString(), matching(uuidRegex))
                                .withHeader(ONAPLogConstants.Headers.PARTNER_NAME.toString(), equalTo("SO")));
        TestAppender.events.clear();
	}
	
	@Test 
	public void testFindFail() throws Exception {
		
		wireMockRule.stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/findRunning"))
			.willReturn(aResponse()
				.withStatus(400)
				.withHeader("Content-Type", MediaType.APPLICATION_JSON)
				.withBody("")));
		
		GRMClient client = new GRMClient();
		thrown.expect(GRMClientCallFailed.class);
		client.findRunningServices("TEST.ECOMP_PSL.*", 1, "TEST");
	}
	
	@Test
	public void testAddFail() throws Exception {
		wireMockRule.stubFor(post(urlPathEqualTo("/GRMLWPService/v1/serviceEndPoint/add"))
				.willReturn(aResponse()
					.withStatus(404)
					.withHeader("Content-Type", MediaType.APPLICATION_JSON)
					.withBody("test")));
		ServiceEndPointRequest request = new ServiceEndPointRequest();
		GRMClient client = new GRMClient();
		thrown.expect(GRMClientCallFailed.class);
		client.addServiceEndPoint(request);
	}

	@Test
	public void testBuildServiceEndPointLookupRequest() {
		GRMClient client = new GRMClient();
		ServiceEndPointLookupRequest request = client.buildServiceEndPointlookupRequest("TEST.ECOMP_PSL.Inventory", 1, "DEV");
		assertEquals("TEST.ECOMP_PSL.Inventory", request.getServiceEndPoint().getName());
		assertEquals(Integer.valueOf(1), Integer.valueOf(request.getServiceEndPoint().getVersion().getMajor()));
		assertEquals("DEV", request.getEnv());
		
	}
	
	protected String getFileContentsAsString(String fileName) {
		String content = "";
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			File file = new File(classLoader.getResource(fileName).getFile());
			content = new String(Files.readAllBytes(file.toPath()));
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exception encountered reading " + fileName + ". Error: " + e.getMessage());
		}
		return content;
	}
}
