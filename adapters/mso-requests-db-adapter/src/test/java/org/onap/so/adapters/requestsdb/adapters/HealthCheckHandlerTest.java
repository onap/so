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

package org.onap.so.adapters.requestsdb.adapters;

import static org.junit.Assert.*;
import java.util.Map;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.adapters.requestsdb.application.MSORequestDBApplication;
import org.onap.so.adapters.requestsdb.application.TestAppender;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ch.qos.logback.classic.spi.ILoggingEvent;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = MSORequestDBApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HealthCheckHandlerTest {
	
	@LocalServerPort
	private int port;

	TestRestTemplate restTemplate = new TestRestTemplate();

	HttpHeaders headers = new HttpHeaders();

	
	@Test
	public void testHealthcheck() throws JSONException {
	    TestAppender.events.clear();
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/manage/health"),
				HttpMethod.GET, entity, String.class);
		
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatusCode().value());
        for(ILoggingEvent logEvent : TestAppender.events)
            if(logEvent.getLoggerName().equals("org.onap.so.logging.spring.interceptor.LoggingInterceptor") &&
            		logEvent.getMarker() != null && logEvent.getMarker().getName().equals("ENTRY")
                    ){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INSTANCE_UUID));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INVOCATION_ID));
                assertEquals("",mdc.get(ONAPLogConstants.MDCs.PARTNER_NAME));
                assertEquals("/manage/health",mdc.get(ONAPLogConstants.MDCs.SERVICE_NAME));
                assertEquals("INPROGRESS",mdc.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
            }else if(logEvent.getLoggerName().equals("org.onap.so.logging.spring.interceptor.LoggingInterceptor") &&
            		logEvent.getMarker() != null &&  logEvent.getMarker()!= null && logEvent.getMarker().getName().equals("EXIT")){
                Map<String,String> mdc = logEvent.getMDCPropertyMap();
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.REQUEST_ID));
                assertNotNull(mdc.get(ONAPLogConstants.MDCs.INVOCATION_ID));
                assertEquals("200",mdc.get(ONAPLogConstants.MDCs.RESPONSE_CODE));
                assertEquals("",mdc.get(ONAPLogConstants.MDCs.PARTNER_NAME));
                assertEquals("/manage/health",mdc.get(ONAPLogConstants.MDCs.SERVICE_NAME));
                assertEquals("COMPLETED",mdc.get(ONAPLogConstants.MDCs.RESPONSE_STATUS_CODE));
            }
        TestAppender.events.clear();
	}
	
	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
}
