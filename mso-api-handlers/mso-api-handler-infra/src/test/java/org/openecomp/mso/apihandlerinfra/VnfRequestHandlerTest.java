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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import mockit.Mock;
import mockit.MockUp;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.apihandler.common.CamundaClient;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.apihandlerinfra.vnfbeans.VnfRequest;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VnfRecipe;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.InfraRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class VnfRequestHandlerTest {
	private static MockUp<RequestsDatabase> mockRDB;
	private static MockUp<VnfMsoInfraRequest> mockMsoRequest;
	private static MockUp<CatalogDatabase> mockCDB;
	private static MockUp<CamundaClient> mockCamudaClient;
//	private static MockUp<RequestClientFactory> mockCamudaClient;
	VnfRequestHandler handler = null;
	UriInfo uriInfo = null;

	private static final String manageVnfRequest = "<vnf-request xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\"><request-info><request-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</request-id><action>CREATE_VF_MODULE</action><source>VID</source><!-- new 1610 field --><service-instance-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</service-instance-id></request-info><vnf-inputs><!-- not in use in 1610 --><vnf-name>vnfName</vnf-name><vnf-type>vnfType</vnf-type><vnf-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</vnf-id><volume-group-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</volume-group-id><vf-module-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</vf-module-id><vf-module-name>vfModuleName</vf-module-name><vf-module-model-name>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</vf-module-model-name><model-customization-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</model-customization-id><asdc-service-model-version>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</asdc-service-model-version><aic-cloud-region>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</aic-cloud-region><tenant-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</tenant-id><service-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</service-id><backout-on-failure>false</backout-on-failure><service-instance-id>43b34d6d-1ab2-4c7a-a3a0-5471306550c5</service-instance-id></vnf-inputs><vnf-params>\t\t\t\t</vnf-params></vnf-request>";

	@Before
	public void setup() throws Exception{
		
		uriInfo = Mockito.mock(UriInfo.class);
		Class<?> clazz = VnfRequestHandler.class;
		handler = (VnfRequestHandler)clazz.newInstance();
		
		Field f1 = handler.getClass().getDeclaredField("uriInfo");
		
		f1.setAccessible(true);
        f1.set(handler, uriInfo);
	}

	@BeforeClass
	public static void setUp() throws Exception {
		MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
		msoPropertiesFactory.removeAllMsoProperties();
		msoPropertiesFactory.initializeMsoProperties(Constants.MSO_PROP_APIHANDLER_INFRA, "src/test/resources/mso.apihandler-infra.properties");

		mockRDB = new MockUp<RequestsDatabase>() {
			@Mock
			public InfraActiveRequests checkDuplicateByVnfId(String vnfId, String action, String requestType) {
				return null;
			}
			@Mock
			public int updateInfraStatus (String requestId, String requestStatus, long progress, String lastModifiedBy) {
				return 1;
			}

			@Mock
			public int updateInfraFinalStatus (String requestId, String requestStatus, String statusMessage, long progress, String responseBody, String lastModifiedBy) {
				return 1;
			}
		};

		mockMsoRequest = new MockUp<VnfMsoInfraRequest>() {
			@Mock
			public void createRequestRecord (Status status) {
				return;
			}
		};

		mockCDB = new MockUp<CatalogDatabase>() {
			@Mock
			public VnfRecipe getVfModuleRecipe(String vnfType, String vfModuleModelName, String action) {
				final VnfRecipe vnfRecipe = new VnfRecipe();
				vnfRecipe.setOrchestrationUri("test/vnf");
				vnfRecipe.setRecipeTimeout(180);
				return vnfRecipe;
			}

			@Mock
			public VfModule getVfModuleType(String type, String version) {
				final VfModule vfModule = new VfModule();
				return vfModule;
			}

			@Mock
			public VnfResource getVnfResource (String vnfType, String serviceVersion) {
				final VnfResource vnfResource = new VnfResource();
				return vnfResource;
			}
		};

		mockCamudaClient = new MockUp<CamundaClient>() {
			@Mock
			public HttpResponse post(String camundaReqXML, String requestId,
									 String requestTimeout, String schemaVersion, String serviceInstanceId, String action)
					throws ClientProtocolException, IOException {
				ProtocolVersion pv = new ProtocolVersion("HTTP",1,1);
				HttpResponse resp = new BasicHttpResponse(pv,200, "test response");
				BasicHttpEntity entity = new BasicHttpEntity();
				String body = "{\"response\":\"success\",\"message\":\"success\"}";
				InputStream instream = new ByteArrayInputStream(body.getBytes());
				entity.setContent(instream);
				resp.setEntity(entity);
				return resp;
			}
		};

		/*mockCamudaClient = new MockUp<RequestClientFactory>() {
			@Mock
			public RequestClient getRequestClient(String orchestrationURI, MsoJavaProperties props) throws IllegalStateException{
				RequestClient client = new CamundaClient();
				client.setUrl("/test/url");
				return client;
			}
		};*/

	}

	@AfterClass
	public static void tearDown() {
		mockRDB.tearDown();
		mockMsoRequest.tearDown();
		mockCDB.tearDown();
		mockCamudaClient.tearDown();
	}

	@Test
	public void manageVnfRequestTestV2(){
		Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/test"));
		Response resp = handler.manageVnfRequest(manageVnfRequest, "v2");
		assertTrue(null != resp);
	}

	@Test
	public void manageVnfRequestTestv1(){
		Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/test"));
		Response resp = handler.manageVnfRequest(manageVnfRequest, "v1");
		assertTrue(null != resp);
	}

	@Test
	public void manageVnfRequestTestv3(){
		Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/test"));
		Response resp = handler.manageVnfRequest(manageVnfRequest, "v3");
		assertTrue(null != resp);
	}
	@Test
	public void manageVnfRequestTestInvalidVersion(){
		Response resp = handler.manageVnfRequest(manageVnfRequest, "v30");
		assertTrue(null != resp);
	}
	
	@Test
	public void manageVnfRequest2Test(){
		Mockito.when(uriInfo.getRequestUri())
        .thenReturn(URI.create("http://localhost:8080/test"));
		
		new MockUp<MsoPropertiesUtils>() {
			@Mock
			public synchronized final boolean getNoPropertiesState() {
				return false;
			}
		};
		Response resp = handler.manageVnfRequest(manageVnfRequest, "v2");
		assertTrue(null != resp);
	}
	
	@Test
	public void fillVnfRequestTest(){
		VnfRequest qr = new VnfRequest();
		InfraRequests ar = new InfraRequests();
		ar.setVnfId("1003");
		ar.setVnfName("vnf");
		ar.setVnfType("vnt");
		ar.setTenantId("48889690");
		ar.setProvStatus("uuu");
		ar.setVolumeGroupName("volume");
		ar.setVolumeGroupId("38838");
		ar.setServiceType("vnf");
		ar.setAicNodeClli("djerfe");
		ar.setAaiServiceId("599499");
		ar.setAicCloudRegion("south");
		ar.setVfModuleName("m1");
		ar.setVfModuleId("39949");
		ar.setVfModuleModelName("test");
		ar.setAaiServiceId("37728");
		ar.setVnfParams("test");
		handler.fillVnfRequest(qr, ar, "v1");
		String param = (String)qr.getVnfParams();
		assertTrue(param.equals("test"));
	}
	
	@Test
	public void fillVnfRequestTestV2(){
		VnfRequest qr = new VnfRequest();
		InfraRequests ar = new InfraRequests();
		ar.setVnfId("1003");
		ar.setVnfName("vnf");
		ar.setVnfType("vnt");
		ar.setTenantId("48889690");
		ar.setProvStatus("uuu");
		ar.setVolumeGroupName("volume");
		ar.setVolumeGroupId("38838");
		ar.setServiceType("vnf");
		ar.setAicNodeClli("djerfe");
		ar.setAaiServiceId("599499");
		ar.setAicCloudRegion("south");
		ar.setVfModuleName("m1");
		ar.setVfModuleId("39949");
		ar.setVfModuleModelName("test");
		ar.setAaiServiceId("37728");
		ar.setVnfParams("test");
		handler.fillVnfRequest(qr, ar, "v2");
		String param = (String)qr.getVnfParams();
		assertTrue(param.equals("test"));
	}
	@Test
	public void fillVnfRequestTestV3(){
		VnfRequest qr = new VnfRequest();
		InfraRequests ar = new InfraRequests();
		ar.setVnfId("1003");
		ar.setVnfName("vnf");
		ar.setVnfType("vnt");
		ar.setTenantId("48889690");
		ar.setProvStatus("uuu");
		ar.setVolumeGroupName("volume");
		ar.setVolumeGroupId("38838");
		ar.setServiceType("vnf");
		ar.setAicNodeClli("djerfe");
		ar.setAaiServiceId("599499");
		ar.setAicCloudRegion("south");
		ar.setVfModuleName("m1");
		ar.setVfModuleId("39949");
		ar.setVfModuleModelName("test");
		ar.setAaiServiceId("37728");
		ar.setVnfParams("test");
		ar.setServiceInstanceId("38829");
		handler.fillVnfRequest(qr, ar, "v3");
		String param = (String)qr.getVnfParams();
		assertTrue(param.equals("test"));
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
		Response resp = handler.queryFilters("vnfType", "serviceType", "aicNodeClli", "tenantId", "volumeGroupId", "volumeGroupName", "vnfName", "v1");
		assertTrue(resp.getEntity().toString() != null);
	}
	
	@Test
	public void queryFiltersTestNullVnfType(){
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
		Response resp = handler.queryFilters(null, null, null, null, null, null, null, "v1");
		assertTrue(resp.getEntity().toString() != null);
	}
	
	@Test
	public void getRequestTest(){
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
		Response resp = handler.getRequest("388293", "v1");
		assertTrue(resp.getEntity().toString() != null);
	}

}
