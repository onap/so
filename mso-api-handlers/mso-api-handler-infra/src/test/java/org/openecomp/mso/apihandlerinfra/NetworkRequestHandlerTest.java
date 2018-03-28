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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;

import mockit.Mocked;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.openecomp.mso.apihandler.common.CamundaClient;
import org.openecomp.mso.apihandlerinfra.networkbeans.NetworkRequest;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.NetworkRecipe;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.InfraRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

public class NetworkRequestHandlerTest {
	private static final String REQ_XML = "<network-request xmlns=\"http://org.openecomp/mso/infra/network-request/v1\"> <request-info> <request-id>e1fc3ed3-31e5-48a8-913b-23184c1e9443</request-id><action>CREATE</action> <source>VID</source> <service-instance-id>e1fc3ed3-31e5-48a8-913b-23184c1e9443</service-instance-id></request-info> <network-inputs> <network-id>e1fc3ed3-31e5-48a8-913b-23184c1e9443</network-id> <network-name>nwInstanceName</network-name> <network-type>nwModelName</network-type><modelCustomizationId>e1fc3ed3-31e5-48a8-913b-23184c1e9443</modelCustomizationId> <aic-cloud-region>e1fc3ed3-31e5-48a8-913b-23184c1e9443</aic-cloud-region> <tenant-id>e1fc3ed3-31e5-48a8-913b-23184c1e9443</tenant-id><service-id>e1fc3ed3-31e5-48a8-913b-23184c1e9443</service-id> <backout-on-failure>false</backout-on-failure><sdncVersion>1802</sdncVersion><service-instance-id>e1fc3ed3-31e5-48a8-913b-23184c1e9443</service-instance-id></network-inputs> <network-params></network-params> </network-request>";

	private static MockUp<RequestsDatabase> mockRDB;
	private static MockUp<CatalogDatabase> mockCDB;
	private static MockUp<CamundaClient> mockCamundaClient;

	NetworkRequestHandler handler = null;
	UriInfo uriInfo = null;
	
	@Before
	public void setup() throws Exception{
		
		uriInfo = Mockito.mock(UriInfo.class);

		Class<?> clazz = NetworkRequestHandler.class;
		handler = (NetworkRequestHandler)clazz.newInstance();
		
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
			public InfraActiveRequests checkDuplicateByVnfName (String vnfName, String action, String requestType) {
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

		mockCDB = new MockUp<CatalogDatabase>() {
			@Mock
			public NetworkRecipe getNetworkRecipe (String networkType, String action, String serviceType) {
				final NetworkRecipe networkRecipe = new NetworkRecipe();
				networkRecipe.setOrchestrationUri("test/vnf");
				networkRecipe.setRecipeTimeout(180);
				return networkRecipe;
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

		mockCamundaClient = new MockUp<CamundaClient>() {
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

	}

	@AfterClass
	public static void tearDown() {
		mockRDB.tearDown();
		mockCDB.tearDown();
		mockCamundaClient.tearDown();
	}
	
	@Test
	public void manageVnfRequestTest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                     @Mocked Session session){
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession(); result = session;
        }};
		Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/test"));
		Response resp = handler.manageNetworkRequest(REQ_XML, "v2");
		assertTrue(null != resp);
	}

	@Test
	public void manageVnfRequestTestV1(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                       @Mocked Session session){
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession(); result = session;
        }};
		Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/test"));
		Response resp = handler.manageNetworkRequest(REQ_XML, "v1");
		assertTrue(null != resp);
	}
	
	@Test
	public void manageVnfRequestTestV3(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                       @Mocked Session session){
        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession(); result = session;
        }};
		Mockito.when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/test"));
		Response resp = handler.manageNetworkRequest(REQ_XML, "v3");
		assertTrue(null != resp);
	}
	
	@Test
	public void manageVnfRequestTestInvalidVersion(){
		Response resp = handler.manageNetworkRequest(REQ_XML, "v249");
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
		Response resp = handler.manageNetworkRequest(REQ_XML, "v2");
		assertTrue(null != resp);
	}
	
	@Test
	public void fillNetworkRequestTestV1(){
		NetworkRequest qr = new NetworkRequest();
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
		handler.fillNetworkRequest(qr, ar, "v1");
		String param = (String)qr.getNetworkParams();
		assertTrue(param.equals("test"));
	}
	@Test
	public void fillNetworkRequestTestV2(){
		NetworkRequest qr = new NetworkRequest();
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
		handler.fillNetworkRequest(qr, ar, "v2");
		String param = (String)qr.getNetworkParams();
		assertTrue(param.equals("test"));
	}
	@Test
	public void fillNetworkRequestTestV3(){
		NetworkRequest qr = new NetworkRequest();
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
		handler.fillNetworkRequest(qr, ar, "v3");
		String param = (String)qr.getNetworkParams();
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
		Response resp = handler.queryFilters("networkType", "serviceType", "aicNodeClli", "tenantId", "v1");
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
