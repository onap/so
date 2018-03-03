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

package org.openecomp.mso.apihandlerinfra;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import mockit.Mock;
import mockit.MockUp;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

public class VolumeRequestHandlerTest {
	VolumeRequestHandler handler = null;
	
    UriInfo uriInfo = null;
	
	@Before
	public void setup() throws Exception{
		
		uriInfo = Mockito.mock(UriInfo.class);
		Class<?> clazz = VolumeRequestHandler.class;
		handler = (VolumeRequestHandler)clazz.newInstance();
		
		Field f1 = handler.getClass().getDeclaredField("uriInfo");
		
		f1.setAccessible(true);
        f1.set(handler, uriInfo);
	}
	
	@Test
	public void manageVnfRequestTest(){
		Response resp = handler.manageVolumeRequest("<name>Test</name>", "v2");
		assertTrue(null != resp);
	}
	
	@Test
	public void manageVnfRequest2TestV1InvalidRequestData(){
		Mockito.when(uriInfo.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/test"));
		
		new MockUp<MsoPropertiesUtils>() {
			@Mock
			public synchronized final boolean getNoPropertiesState() {
				return false;
			}
		};
		String request = "{\"result\":\"success\"}";
		Response resp = handler.manageVolumeRequest(request, "v1");
		assertTrue(null != resp);
	}
	
	@Test
	public void manageVnfRequest2TestV1(){
		Mockito.when(uriInfo.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/test"));
		
		new MockUp<MsoPropertiesUtils>() {
			@Mock
			public synchronized final boolean getNoPropertiesState() {
				return false;
			}
		};
		String request = "<volume-request xmlns=\"http://org.openecomp/mso/infra/volume-request/v1\"><request-info><action>CREATE</action><request-status>COMPLETE</request-status><status-message>message</status-message><progress>10001</progress><start-time>1970-01-01 02:47:00.1</start-time><end-time>1970-01-01 05:33:40.1</end-time><source>test</source></request-info><volume-inputs><service-id>299392</service-id></volume-inputs></volume-request>";
		Response resp = handler.manageVolumeRequest(request, "v1");
		assertTrue(null != resp);
	}
	
	@Test
	public void manageVnfRequest2TestV2(){
		Mockito.when(uriInfo.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/test"));
		
		new MockUp<MsoPropertiesUtils>() {
			@Mock
			public synchronized final boolean getNoPropertiesState() {
				return false;
			}
		};
		String request = "<volume-request xmlns=\"http://org.openecomp/mso/infra/volume-request/v2\"><request-info><action>CREATE</action><request-status>COMPLETE</request-status><status-message>message</status-message><progress>10001</progress><start-time>1970-01-01 02:47:00.1</start-time><end-time>1970-01-01 05:33:40.1</end-time><source>test</source></request-info><volume-inputs><service-id>299392</service-id></volume-inputs></volume-request>";
		Response resp = handler.manageVolumeRequest(request, "v2");
		assertTrue(null != resp);
	}
	@Test
	public void manageVnfRequest2TestV3(){
		Mockito.when(uriInfo.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/test"));
		
		new MockUp<MsoPropertiesUtils>() {
			@Mock
			public synchronized final boolean getNoPropertiesState() {
				return false;
			}
		};
		String request = "<volume-request xmlns=\"http://org.openecomp/mso/infra/volume-request/v3\"><request-info><action>CREATE</action><request-status>COMPLETE</request-status><status-message>message</status-message><progress>10001</progress><start-time>1970-01-01 02:47:00.1</start-time><end-time>1970-01-01 05:33:40.1</end-time><source>test</source></request-info><volume-inputs><service-id>299392</service-id></volume-inputs></volume-request>";
		Response resp = handler.manageVolumeRequest(request, "v3");
		assertTrue(null != resp);
	}
	@Test
	public void manageVnfRequest2TestInvalidVersion(){
		Mockito.when(uriInfo.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/test"));
		
		new MockUp<MsoPropertiesUtils>() {
			@Mock
			public synchronized final boolean getNoPropertiesState() {
				return false;
			}
		};
		String request = "<volume-request xmlns=\"http://org.openecomp/mso/infra/volume-request/v1\"><request-info><action>CREATE</action><request-status>COMPLETE</request-status><status-message>message</status-message><progress>10001</progress><start-time>1970-01-01 02:47:00.1</start-time><end-time>1970-01-01 05:33:40.1</end-time><source>test</source></request-info><volume-inputs><service-id>299392</service-id></volume-inputs></volume-request>";
		Response resp = handler.manageVolumeRequest(request, "v33");
		assertTrue(null != resp);
	}
	
	@Test
	public void queryFiltersTest(){
		new MockUp<RequestsDatabase>() {
			@Mock
			public List <InfraActiveRequests> getRequestListFromInfraActive (String queryAttributeName,
                    String queryValue,
                    String requestType) {
				List <InfraActiveRequests> list = new ArrayList<>();
				InfraActiveRequests req = new InfraActiveRequests();
				req.setAaiServiceId("299392");
				req.setAction("CREATE");
				req.setRequestStatus("COMPLETE");
				req.setProgress(10001L);
				req.setSource("test");
				req.setStartTime(new Timestamp(10020100));
				req.setEndTime(new Timestamp(20020100));
				req.setStatusMessage("message");
				list.add(req);
				return list;
			}
		};
		Response resp = handler.queryFilters("vnfType", "serviceType", "aic", "19929293", "288393923", "test", "v1");
		assertTrue(resp.getEntity().toString() != null);
	}
	@Test
	public void getRequestTestV3(){
		new MockUp<RequestsDatabase>() {
			@Mock
			public InfraActiveRequests getRequestFromInfraActive (String requestId, String requestType) {
				InfraActiveRequests req = new InfraActiveRequests();
				req.setAaiServiceId("299392");
				req.setAction("CREATE");
				req.setRequestStatus("COMPLETE");
				req.setProgress(10001L);
				req.setSource("test");
				req.setStartTime(new Timestamp(10020100));
				req.setEndTime(new Timestamp(20020100));
				req.setStatusMessage("message");
				return req;
			}
		};
		Response resp = handler.getRequest("399293", "v3");
		assertTrue(resp.getEntity().toString() != null);
	}
	@Test
	public void getRequestTestV2(){
		new MockUp<RequestsDatabase>() {
			@Mock
			public InfraActiveRequests getRequestFromInfraActive (String requestId, String requestType) {
				InfraActiveRequests req = new InfraActiveRequests();
				req.setAaiServiceId("299392");
				req.setAction("CREATE");
				req.setRequestStatus("COMPLETE");
				req.setProgress(10001L);
				req.setSource("test");
				req.setStartTime(new Timestamp(10020100));
				req.setEndTime(new Timestamp(20020100));
				req.setStatusMessage("message");
				return req;
			}
		};
		Response resp = handler.getRequest("399293", "v2");
		assertTrue(resp.getEntity().toString() != null);
	}
}
