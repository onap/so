/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.mso.apihandler.common.CamundaClient;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.db.AbstractSessionFactoryManager;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.properties.MsoDatabaseException;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.openecomp.mso.requestsdb.OperationStatus;
import org.openecomp.mso.requestsdb.RequestsDatabase;
import org.openecomp.mso.serviceinstancebeans.ServiceInstancesRequest;

import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;

public class E2EServiceInstancesTest {

    private final String compareModelsRequest = "{" +
            "\"globalSubscriberId\": \"60c3e96e-0970-4871-b6e0-3b6de7561519\"," +
            "\"serviceType\": \"vnf\"," +
            "\"modelInvariantIdTarget\": \"60c3e96e-0970-4871-b6e0-3b6de1234567\"," +
            "\"modelVersionIdTarget\": \"modelVersionIdTarget\"" +
            "}";

    private final String scaleRequest = "{\"service\":{\"serviceType\":\"example-service-type\",\"globalSubscriberId\":\"test_custormer\",\"resources\":[{\"resourceInstanceId\":\"ns111\",\"scaleType\":\"SCALE_NS\",\"scaleNsData\":{\"scaleNsByStepsData\":{\"numberOfSteps\":\"4\",\"aspectId\":\"TIC_EDGE_HW\",\"scalingDirection\":\"UP\"}}}],\"serviceInstanceName\":\"XXXX\"}}";

	String jsonBody = "{" +
			"\"service\": {" +
			"\"name\": \"so_test4\"," +
			"\"description\": \"so_test2\"," +
			"\"serviceInvariantUuid\": \"60c3e96e-0970-4871-b6e0-3b6de7561519\"," +
			"\"serviceUuid\": \"592f9437-a9c0-4303-b9f6-c445bb7e9814\"," +
			"\"globalSubscriberId\": \"123457\"," +
			"\"serviceType\": \"voLTE\"," +
			"\"parameters\": {" +
			"\"resources\": [" +
			"{" +
			"\"resourceName\": \"vIMS\"," +
			"\"resourceInvariantUuid\": \"60c3e96e-0970-4871-b6e0-3b6de7561516\"," +
			"\"resourceUuid\": \"60c3e96e-0970-4871-b6e0-3b6de7561512\"," +
			"\"parameters\": {" +
			"\"locationConstraints\": [" +
			"{" +
			"\"vnfProfileId\": \"zte-vBAS-1.0\"," +
			"\"locationConstraints\": {" +
			"\"vimId\": \"4050083f-465f-4838-af1e-47a545222ad0\"" +
			"}" +
			"}," +
			"{" +
			"\"vnfProfileId\": \"zte-vMME-1.0\"," +
			"\"locationConstraints\": {" +
			"\"vimId\": \"4050083f-465f-4838-af1e-47a545222ad0\"" +
			"}" +
			"}" +
			"]" +
			"}" +
			"}," +
			"{" +
			"\"resourceName\": \"vEPC\"," +
			"\"resourceInvariantUuid\": \"61c3e96e-0970-4871-b6e0-3b6de7561516\"," +
			"\"resourceUuid\": \"62c3e96e-0970-4871-b6e0-3b6de7561512\"," +
			"\"parameters\": {" +
			"\"locationConstraints\": [" +
			"{" +
			"\"vnfProfileId\": \"zte-CSCF-1.0\"," +
			"\"locationConstraints\": {" +
			"\"vimId\": \"4050083f-465f-4838-af1e-47a545222ad1\"" +
			"}" +
			"}" +
			"]" +
			"}" +
			"}," +
			"{" +
			"\"resourceName\": \"underlayvpn\"," +
			"\"resourceInvariantUuid\": \"60c3e96e-0970-4871-b6e0-3b6de7561513\"," +
			"\"resourceUuid\": \"60c3e96e-0970-4871-b6e0-3b6de7561514\"," +
			"\"parameters\": {" +
			"\"locationConstraints\": []" +
			"}" +
			"}," +
			"{" +
			"\"resourceName\": \"overlayvpn\"," +
			"\"resourceInvariantUuid\": \"60c3e96e-0970-4871-b6e0-3b6de7561517\"," +
			"\"resourceUuid\": \"60c3e96e-0970-4871-b6e0-3b6de7561518\"," +
			"\"parameters\": {" +
			"\"locationConstraints\": []" +
			"}" +
			"}" +
			"]," +
			"\"requestInputs\": {" +
			"\"externalDataNetworkName\": \"Flow_out_net\"," +
			"\"m6000_mng_ip\": \"181.18.20.2\"," +
			"\"externalCompanyFtpDataNetworkName\": \"Flow_out_net\"," +
			"\"externalPluginManageNetworkName\": \"plugin_net_2014\"," +
			"\"externalManageNetworkName\": \"mng_net_2017\"," +
			"\"sfc_data_network\": \"sfc_data_net_2016\"," +
			"\"NatIpRange\": \"210.1.1.10-210.1.1.20\"," +
			"\"location\": \"4050083f-465f-4838-af1e-47a545222ad0\"," +
			"\"sdncontroller\": \"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"" +
			"}" +
			"}" +
			"}" +
			"}";

    @BeforeClass
    public static void setUp() throws Exception {

        MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
        msoPropertiesFactory.removeAllMsoProperties();
        msoPropertiesFactory.initializeMsoProperties(Constants.MSO_PROP_APIHANDLER_INFRA, "src/test/resources/mso.apihandler-infra.properties");
    }

	@Test
	public void createE2EServiceInstanceTestSuccess() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		new MockUp<CatalogDatabase>() {
			@Mock
			public Service getServiceByModelName(String modelName) {
				Service svc = new Service();
				return svc;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
					String action) {
				ServiceRecipe rec = new ServiceRecipe();
				return rec;
			}
		};

		new MockUp<RequestClientFactory>() {
			@Mock
			public RequestClient getRequestClient(String orchestrationURI,
					MsoJavaProperties props) throws IllegalStateException {
				RequestClient client = new CamundaClient();
				client.setUrl("/test/url");
				return client;
			}
		};

		new MockUp<CamundaClient>() {
			@Mock
			public HttpResponse post(String requestId, boolean isBaseVfModule,
					int recipeTimeout, String requestAction,
					String serviceInstanceId, String vnfId, String vfModuleId,
					String volumeGroupId, String networkId, String configurationId, String serviceType,
					String vnfType, String vfModuleType, String networkType,
					String requestDetails, String recipeParamXsd) {
				ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
				HttpResponse resp = new BasicHttpResponse(pv, 202,
						"test response");
				BasicHttpEntity entity = new BasicHttpEntity();
				String body = "{\"response\":\"success\",\"message\":\"success\"}";
				InputStream instream = new ByteArrayInputStream(body.getBytes());
				entity.setContent(instream);
				resp.setEntity(entity);
				return resp;
			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestBpelHTTPException() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		new MockUp<CatalogDatabase>() {
			@Mock
			public Service getServiceByModelName(String modelName) {
				Service svc = new Service();
				return svc;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
					String action) {
				ServiceRecipe rec = new ServiceRecipe();
				return rec;
			}
		};

		new MockUp<RequestClientFactory>() {
			@Mock
			public RequestClient getRequestClient(String orchestrationURI,
					MsoJavaProperties props) throws IllegalStateException {
				RequestClient client = new CamundaClient();
				client.setUrl("/test/url");
				return client;
			}
		};

		new MockUp<CamundaClient>() {
			@Mock
			public HttpResponse post(String requestId, boolean isBaseVfModule,
					int recipeTimeout, String requestAction,
					String serviceInstanceId, String vnfId, String vfModuleId,
					String volumeGroupId, String networkId, String configurationId, String serviceType,
					String vnfType, String vfModuleType, String networkType,
					String requestDetails, String recipeParamXsd) {
				ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
				HttpResponse resp = new BasicHttpResponse(pv, 500,
						"test response");
				BasicHttpEntity entity = new BasicHttpEntity();
				String body = "{\"response\":\"success\",\"message\":\"success\"}";
				InputStream instream = new ByteArrayInputStream(body.getBytes());
				entity.setContent(instream);
				resp.setEntity(entity);
				return resp;
			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestBpelHTTPExceptionWithNullREsponseBody() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		new MockUp<CatalogDatabase>() {
			@Mock
			public Service getServiceByModelName(String modelName) {
				Service svc = new Service();
				return svc;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
					String action) {
				ServiceRecipe rec = new ServiceRecipe();
				return rec;
			}
		};

		new MockUp<RequestClientFactory>() {
			@Mock
			public RequestClient getRequestClient(String orchestrationURI,
					MsoJavaProperties props) throws IllegalStateException {
				RequestClient client = new CamundaClient();
				client.setUrl("/test/url");
				return client;
			}
		};

		new MockUp<CamundaClient>() {
			@Mock
			public HttpResponse post(String requestId, boolean isBaseVfModule,
					int recipeTimeout, String requestAction,
					String serviceInstanceId, String vnfId, String vfModuleId,
					String volumeGroupId, String networkId, String configurationId, String serviceType,
					String vnfType, String vfModuleType, String networkType,
					String requestDetails, String recipeParamXsd) {
				ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
				HttpResponse resp = new BasicHttpResponse(pv, 500,
						"test response");
				BasicHttpEntity entity = new BasicHttpEntity();
				String body = "{\"response\":\"\",\"message\":\"success\"}";
				InputStream instream = new ByteArrayInputStream(body.getBytes());
				entity.setContent(instream);
				resp.setEntity(entity);
				return resp;
			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestNullBPELResponse() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		new MockUp<CatalogDatabase>() {
			@Mock
			public Service getServiceByModelName(String modelName) {
				Service svc = new Service();
				return svc;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
					String action) {
				ServiceRecipe rec = new ServiceRecipe();
				return rec;
			}
		};

		new MockUp<RequestClientFactory>() {
			@Mock
			public RequestClient getRequestClient(String orchestrationURI,
					MsoJavaProperties props) throws IllegalStateException {
				RequestClient client = new CamundaClient();
				client.setUrl("/test/url");
				return client;
			}
		};

		new MockUp<CamundaClient>() {
			@Mock
			public HttpResponse post(String requestId, boolean isBaseVfModule,
					int recipeTimeout, String requestAction,
					String serviceInstanceId, String vnfId, String vfModuleId,
					String volumeGroupId, String networkId, String configurationId, String serviceType,
					String vnfType, String vfModuleType, String networkType,
					String requestDetails, String recipeParamXsd) {
				HttpResponse resp = null;
				return resp;
			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestBPMNNullREsponse() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		new MockUp<CatalogDatabase>() {
			@Mock
			public Service getServiceByModelName(String modelName) {
				Service svc = new Service();
				return svc;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
					String action) {
				ServiceRecipe rec = new ServiceRecipe();
				return rec;
			}
		};

		new MockUp<RequestClientFactory>() {
			@Mock
			public RequestClient getRequestClient(String orchestrationURI,
					MsoJavaProperties props) throws IllegalStateException {
				RequestClient client = new CamundaClient();
				client.setUrl("/test/url");
				return client;
			}
		};

		new MockUp<CamundaClient>() {
			@Mock
			public HttpResponse post(String camundaReqXML, String requestId,
					String requestTimeout, String schemaVersion,
					String serviceInstanceId, String action) {
				HttpResponse resp = null;
				return resp;
			}
		};
		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestNullBpmn() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};

		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		new MockUp<CatalogDatabase>() {
			@Mock
			public Service getServiceByModelName(String modelName) {
				Service svc = new Service();
				return svc;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
					String action) {
				ServiceRecipe rec = new ServiceRecipe();
				return rec;
			}
		};
		E2EServiceInstances instance = new E2EServiceInstances();
        String request = jsonBody;
        ;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestNullReceipe() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestNullDBResponse() {

		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestInvalidRequest() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};

		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestEmptyDBQuery() {
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
		// assertTrue(true);
	}

	@Test
	public void createE2EServiceInstanceTestDBQueryFail() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceName(
					String serviceName) {
				OperationStatus operationStatus = new OperationStatus();
				return operationStatus;
			}
		};

		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

	@Test
	public void createE2EServiceInstanceTestForEmptyRequest() {

		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		E2EServiceInstances instance = new E2EServiceInstances();
		String request = "";
		Response resp = instance.createE2EServiceInstance(request, "v3");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr
            .contains("Mapping of request to JSON object failed.  No content to map due to end-of-input"));
	}

	@Test
	public void deleteE2EServiceInstanceTestNormal() {

        final MockUp<CatalogDatabase> mockCDB = new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) {
                Service svc = new Service();
                return svc;
            }

            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                ServiceRecipe rec = new ServiceRecipe();
                rec.setOrchestrationUri("/test/delE2E");
                rec.setRecipeTimeout(180);
                return rec;
            }
        };

        final MockUp<MsoRequest> mockMsoRequest = new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord(Status status, Action action) {
                return;
            }
        };

        final MockUp<CamundaClient> mockCmaundaClient = new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
                                     int recipeTimeout, String requestAction, String serviceInstanceId,
                                     String vnfId, String vfModuleId, String volumeGroupId, String networkId, String configurationId,
                                     String serviceType, String vnfType, String vfModuleType, String networkType,
                                     String requestDetails, String recipeParamXsd)
                    throws ClientProtocolException, IOException {
                ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
                HttpResponse resp = new BasicHttpResponse(pv, 200, "test response");
                BasicHttpEntity entity = new BasicHttpEntity();
                String body = "{\"response\":\"success\",\"message\":\"success\"}";
                InputStream instream = new ByteArrayInputStream(body.getBytes());
                entity.setContent(instream);
                resp.setEntity(entity);
                return resp;
            }
        };


		E2EServiceInstances instance = new E2EServiceInstances();
		String request = "{\"globalSubscriberId\":\"299392392\",\"serviceType\":\"VoLTE\"}";

        instance.deleteE2EServiceInstance(request, "v3", "12345678");
        mockCDB.tearDown();
        mockMsoRequest.tearDown();
        mockCmaundaClient.tearDown();

    }

    @Test
    public void deleteE2EServiceInstanceTestFail() {
        E2EServiceInstances instance = new E2EServiceInstances();
        String request = "{\"globalSubscriberId\":\"299392392\",\"serviceType\":\"VoLTE\"}";
        instance.deleteE2EServiceInstance(request, "v3", "12345678");
    }

    @Test
    public void deleteE2EServiceInstanceTestFailClient(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                       @Mocked Session session) {
        final MockUp<CatalogDatabase> mockCDB = new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) {
                Service svc = new Service();
                return svc;
            }

            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                ServiceRecipe rec = new ServiceRecipe();
                rec.setOrchestrationUri("/test/delE2E");
                rec.setRecipeTimeout(180);
                return rec;
            }
        };

        final MockUp<MsoRequest> mockMsoRequest = new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord(Status status, Action action) {
                return;
            }
        };

        E2EServiceInstances instance = new E2EServiceInstances();
        String request = "{\"globalSubscriberId\":\"299392392\",\"serviceType\":\"VoLTE\"}";

        instance.deleteE2EServiceInstance(request, "v3", "12345678");
        mockCDB.tearDown();
        mockMsoRequest.tearDown();

    }

    @Test
    public void deleteE2EServiceInstanceTestFailRecipe(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                       @Mocked Session session) {
        final MockUp<CatalogDatabase> mockCDB = new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) {
                Service svc = new Service();
                return svc;
            }

            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                ServiceRecipe rec = new ServiceRecipe();
                return rec;
            }
        };

        final MockUp<MsoRequest> mockMsoRequest = new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord(Status status, Action action) {
                return;
            }
        };

        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
        }};

        E2EServiceInstances instance = new E2EServiceInstances();
        String request = "{\"globalSubscriberId\":\"299392392\",\"serviceType\":\"VoLTE\"}";

        instance.deleteE2EServiceInstance(request, "v3", "12345678");
        mockCDB.tearDown();
        mockMsoRequest.tearDown();

    }

    @Test
    public void deleteE2EServiceInstanceTestFailModelName(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                          @Mocked Session session) {
        final MockUp<MsoRequest> mockMsoRequest = new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord(Status status, Action action) {
                return;
            }
        };

        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession();
            result = session;
        }};

        E2EServiceInstances instance = new E2EServiceInstances();
        String request = "{\"globalSubscriberId\":\"299392392\",\"serviceType\":\"VoLTE\"}";

        instance.deleteE2EServiceInstance(request, "v3", "12345678");
        mockMsoRequest.tearDown();

	}

	@Test
	public void getE2EServiceInstanceTest() {

		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatus(String serviceId,
					String operationId) {
				OperationStatus os = new OperationStatus();
				os.setOperation("");
				os.setOperationContent("");
				os.setOperationId("123456");
				os.setProgress("");
				os.setServiceId("12345");
				os.setServiceName("VoLTE");
				os.setReason("");
				os.setResult("");
				os.setUserId("");
				return os;
			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		Response resp = instance
				.getE2EServiceInstances("12345", "v3", "123456");

	}
	
	@Test
	public void updateE2EServiceInstanceTestNormal() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceId(
					String serviceID) {
				OperationStatus operationStatus = new OperationStatus();
				operationStatus.setProgress("100");
				operationStatus.setResult("finish");
				return operationStatus;
			}
		};
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		new MockUp<CatalogDatabase>() {
			@Mock
			public Service getServiceByModelName(String modelName) {
				Service svc = new Service();
				return svc;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
					String action) {
				ServiceRecipe rec = new ServiceRecipe();
				return rec;
			}
		};

		new MockUp<RequestClientFactory>() {
			@Mock
			public RequestClient getRequestClient(String orchestrationURI,
					MsoJavaProperties props) throws IllegalStateException {
				RequestClient client = new CamundaClient();
				client.setUrl("/test/url");
				return client;
			}
		};

		new MockUp<CamundaClient>() {
			@Mock
			public HttpResponse post(String requestId, boolean isBaseVfModule,
					int recipeTimeout, String requestAction,
					String serviceInstanceId, String vnfId, String vfModuleId,
					String volumeGroupId, String networkId, String configurationId, String serviceType,
					String vnfType, String vfModuleType, String networkType,
					String requestDetails, String recipeParamXsd) {
				ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
				HttpResponse resp = new BasicHttpResponse(pv, 202,
						"test response");
				BasicHttpEntity entity = new BasicHttpEntity();
				String body = "{\"response\":\"success\",\"message\":\"success\"}";
				InputStream instream = new ByteArrayInputStream(body.getBytes());
				entity.setContent(instream);
				resp.setEntity(entity);
				return resp;
			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.updateE2EServiceInstance(request, "v3", "12345");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("success"));
	}
	
	@Test
	public void updateE2EServiceInstanceTestChkStatusFalse() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceId(
					String serviceID) {
				OperationStatus operationStatus = new OperationStatus();
				operationStatus.setResult("processing");
				return operationStatus;
			}
		};
		new MockUp<E2EServiceInstances>() {
			@Mock
			private void createOperationStatusRecordForError(Action action,
					String requestId) throws MsoDatabaseException {

			}
		};
		new MockUp<CatalogDatabase>() {
			@Mock
			public Service getServiceByModelName(String modelName) {
				Service svc = new Service();
				return svc;
			}
		};

		new MockUp<CatalogDatabase>() {
			@Mock
			public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
					String action) {
				ServiceRecipe rec = new ServiceRecipe();
				return rec;
			}
		};

		new MockUp<RequestClientFactory>() {
			@Mock
			public RequestClient getRequestClient(String orchestrationURI,
					MsoJavaProperties props) throws IllegalStateException {
				RequestClient client = new CamundaClient();
				client.setUrl("/test/url");
				return client;
			}
		};

		new MockUp<CamundaClient>() {
			@Mock
			public HttpResponse post(String requestId, boolean isBaseVfModule,
					int recipeTimeout, String requestAction,
					String serviceInstanceId, String vnfId, String vfModuleId,
					String volumeGroupId, String networkId, String configurationId, String serviceType,
					String vnfType, String vfModuleType, String networkType,
					String requestDetails, String recipeParamXsd) {
				ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
				HttpResponse resp = new BasicHttpResponse(pv, 202,
						"test response");
				BasicHttpEntity entity = new BasicHttpEntity();
				String body = "{\"response\":\"success\",\"message\":\"success\"}";
				InputStream instream = new ByteArrayInputStream(body.getBytes());
				entity.setContent(instream);
				resp.setEntity(entity);
				return resp;
			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = jsonBody;
		Response resp = instance.updateE2EServiceInstance(request, "v3", "12345");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC2000"));
	}

    @Test
    public void updateE2EServiceInstanceExceptionMsoRequestTest() {
        new MockUp<RequestsDatabase>() {
            @Mock
            public OperationStatus getOperationStatusByServiceId(
                    String serviceID) {
                OperationStatus operationStatus = new OperationStatus();
                operationStatus.setProgress("100");
                operationStatus.setResult("finish");
                return operationStatus;
            }
        };
        new MockUp<E2EServiceInstances>() {
            @Mock
            private void createOperationStatusRecordForError(Action action,
                                                             String requestId) throws MsoDatabaseException {

            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) {
                Service svc = new Service();
                return svc;
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                ServiceRecipe rec = new ServiceRecipe();
                return rec;
            }
        };

        new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI,
                                                  MsoJavaProperties props) throws IllegalStateException {
                RequestClient client = new CamundaClient();
                client.setUrl("/test/url");
                return client;
            }
        };

        new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
                                     int recipeTimeout, String requestAction,
                                     String serviceInstanceId, String vnfId, String vfModuleId,
                                     String volumeGroupId, String networkId, String configurationId, String serviceType,
                                     String vnfType, String vfModuleType, String networkType,
                                     String requestDetails, String recipeParamXsd) {
                ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
                HttpResponse resp = new BasicHttpResponse(pv, 202,
                        "test response");
                BasicHttpEntity entity = new BasicHttpEntity();
                String body = "{\"response\":\"success\",\"message\":\"success\"}";
                InputStream instream = new ByteArrayInputStream(body.getBytes());
                entity.setContent(instream);
                resp.setEntity(entity);
                return resp;
            }
        };
        MockUp<MsoRequest> msoRequest = new MockUp<MsoRequest>() {
        	@Mock
            void parse (ServiceInstancesRequest sir, HashMap<String,String> instanceIdMap, Action action, String version, String originalRequestJSON) throws ValidationException {

        		throw new ValidationException("mock failure");
        	}
        };
        E2EServiceInstances instance = new E2EServiceInstances();
        String request = jsonBody;
        Response resp = instance.updateE2EServiceInstance(request, "v3", "12345");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        msoRequest.tearDown();
    }
    
    @Test
    public void updateE2EServiceInstanceExceptionCatalogDbTest() {
        new MockUp<RequestsDatabase>() {
            @Mock
            public OperationStatus getOperationStatusByServiceId(
                    String serviceID) {
                OperationStatus operationStatus = new OperationStatus();
                operationStatus.setProgress("100");
                operationStatus.setResult("finish");
                return operationStatus;
            }
        };
        new MockUp<E2EServiceInstances>() {
            @Mock
            private void createOperationStatusRecordForError(Action action,
                                                             String requestId) throws MsoDatabaseException {

            }
        };
        MockUp<CatalogDatabase> catalog = new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) throws Exception {
                throw new Exception();
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                ServiceRecipe rec = new ServiceRecipe();
                return rec;
            }
        };

        new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI,
                                                  MsoJavaProperties props) throws IllegalStateException {
                RequestClient client = new CamundaClient();
                client.setUrl("/test/url");
                return client;
            }
        };

        new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
                                     int recipeTimeout, String requestAction,
                                     String serviceInstanceId, String vnfId, String vfModuleId,
                                     String volumeGroupId, String networkId, String configurationId, String serviceType,
                                     String vnfType, String vfModuleType, String networkType,
                                     String requestDetails, String recipeParamXsd) {
                ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
                HttpResponse resp = new BasicHttpResponse(pv, 202,
                        "test response");
                BasicHttpEntity entity = new BasicHttpEntity();
                String body = "{\"response\":\"success\",\"message\":\"success\"}";
                InputStream instream = new ByteArrayInputStream(body.getBytes());
                entity.setContent(instream);
                resp.setEntity(entity);
                return resp;
            }
        };
       
        E2EServiceInstances instance = new E2EServiceInstances();
        String request = jsonBody;
        Response resp = instance.updateE2EServiceInstance(request, "v3", "12345");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
        catalog.tearDown();
    }
    
    @Test
    public void updateE2EServiceInstanceNullServiceTest() {
        new MockUp<RequestsDatabase>() {
            @Mock
            public OperationStatus getOperationStatusByServiceId(
                    String serviceID) {
                OperationStatus operationStatus = new OperationStatus();
                operationStatus.setProgress("100");
                operationStatus.setResult("finish");
                return operationStatus;
            }
        };
        new MockUp<E2EServiceInstances>() {
            @Mock
            private void createOperationStatusRecordForError(Action action,
                                                             String requestId) throws MsoDatabaseException {

            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) {
            	Service svc = new Service();
                return svc;
            }
        };

        MockUp<CatalogDatabase> catalog = new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                return null;
            }
        };

        new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI,
                                                  MsoJavaProperties props) throws IllegalStateException {
                RequestClient client = new CamundaClient();
                client.setUrl("/test/url");
                return client;
            }
        };

        new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
                                     int recipeTimeout, String requestAction,
                                     String serviceInstanceId, String vnfId, String vfModuleId,
                                     String volumeGroupId, String networkId, String configurationId, String serviceType,
                                     String vnfType, String vfModuleType, String networkType,
                                     String requestDetails, String recipeParamXsd) {
                ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
                HttpResponse resp = new BasicHttpResponse(pv, 202,
                        "test response");
                BasicHttpEntity entity = new BasicHttpEntity();
                String body = "{\"response\":\"success\",\"message\":\"success\"}";
                InputStream instream = new ByteArrayInputStream(body.getBytes());
                entity.setContent(instream);
                resp.setEntity(entity);
                return resp;
            }
        };
       
        E2EServiceInstances instance = new E2EServiceInstances();
        String request = jsonBody;
        Response resp = instance.updateE2EServiceInstance(request, "v3", "12345");
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), resp.getStatus());
        catalog.tearDown();
    }
    
    @Test
    public void updateE2EServiceInstanceRequestClientExceptionTest() {
        new MockUp<RequestsDatabase>() {
            @Mock
            public OperationStatus getOperationStatusByServiceId(
                    String serviceID) {
                OperationStatus operationStatus = new OperationStatus();
                operationStatus.setProgress("100");
                operationStatus.setResult("finish");
                return operationStatus;
            }
        };
        new MockUp<E2EServiceInstances>() {
            @Mock
            private void createOperationStatusRecordForError(Action action,
                                                             String requestId) throws MsoDatabaseException {

            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) {
            	Service svc = new Service();
                return svc;
            }
        };

        new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                ServiceRecipe rec = new ServiceRecipe();
                return rec;
            }
        };

        new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI,
                                                  MsoJavaProperties props) throws IllegalStateException {
                RequestClient client = new CamundaClient();
                client.setUrl("/test/url");
                return client;
            }
        };

        MockUp<CamundaClient> client = new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
                                     int recipeTimeout, String requestAction,
                                     String serviceInstanceId, String vnfId, String vfModuleId,
                                     String volumeGroupId, String networkId, String configurationId, String serviceType,
                                     String vnfType, String vfModuleType, String networkType,
                                     String requestDetails, String recipeParamXsd) throws Exception {
                throw new Exception();
            }
        };
       
        E2EServiceInstances instance = new E2EServiceInstances();
        String request = jsonBody;
        Response resp = instance.updateE2EServiceInstance(request, "v3", "12345");
        assertEquals(Response.Status.BAD_GATEWAY.getStatusCode(), resp.getStatus());
        client.tearDown();
    }
    
    @Test
    public void compareModelwithTargetVersionBadRequest(){

        E2EServiceInstances instance = new E2EServiceInstances();
        Response response = instance.compareModelwithTargetVersion("", "12345", "v3");

        assertNotNull(response);
        assertTrue(response.getEntity().toString().contains("Mapping of request to JSON object failed."));

    }
    @Test
    public void compareModelwithTargetVersionFailedBPMNCall(){

        new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
                 int recipeTimeout, String requestAction, String serviceInstanceId,
                 String vnfId, String vfModuleId, String volumeGroupId, String networkId, String configurationId,
                 String serviceType, String vnfType, String vfModuleType, String networkType,
                 String requestDetails, String recipeParamXsd)
                    throws ClientProtocolException, IOException {

                throw new ClientProtocolException();
            }
        };

        E2EServiceInstances instance = new E2EServiceInstances();
        Response response = instance.compareModelwithTargetVersion(compareModelsRequest, "12345", "v3");

        assertNotNull(response);
        assertTrue(response.getEntity().toString().contains("Failed calling bpmn"));

    }

    @Test
    public void compareModelwithTargetVersionSuccess(){

        new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
                                     int recipeTimeout, String requestAction, String serviceInstanceId,
                                     String vnfId, String vfModuleId, String volumeGroupId, String networkId, String configurationId,
                                     String serviceType, String vnfType, String vfModuleType, String networkType,
                                     String requestDetails, String recipeParamXsd)
                    throws ClientProtocolException, IOException {

                ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
                HttpResponse resp = new BasicHttpResponse(pv, 202,
                        "compareModelwithTargetVersion, test response");
                BasicHttpEntity entity = new BasicHttpEntity();
                String body = "{\"response\":\"success\",\"message\":\"success\"}";
                InputStream instream = new ByteArrayInputStream(body.getBytes());
                entity.setContent(instream);
                resp.setEntity(entity);

                return resp;
            }
        };

        E2EServiceInstances instance = new E2EServiceInstances();
        Response response = instance.compareModelwithTargetVersion(compareModelsRequest, "12345", "v3");

        assertNotNull(response);
        assertTrue(response.getStatus()==202);

    }

    @Test
    public void scaleE2EserviceInstancesTestFailInvalidRequest(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                               @Mocked Session session ) {

        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession(); result = session;
        }};

        E2EServiceInstances instance = new E2EServiceInstances();
        Response response = instance.scaleE2EServiceInstance(jsonBody, "v3", "12345");
    }

    @Test
    public void scaleE2EserviceInstancesTestNormal() {

        final MockUp<MsoRequest> mockMsoRequest = new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord(Status status, Action action) {
                return;
            }
        };

        final MockUp<CatalogDatabase> mockCDB = new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) {
                Service svc = new Service();
                return svc;
            }

            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                ServiceRecipe rec = new ServiceRecipe();
                rec.setOrchestrationUri("/test/delE2E");
                rec.setRecipeTimeout(180);
                return rec;
            }
        };

        final MockUp<RequestClientFactory> mockRequestClient = new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI,
                                                  MsoJavaProperties props) throws IllegalStateException {
                RequestClient client = new CamundaClient();
                client.setUrl("/test/url");
                return client;
            }
        };

        final MockUp<CamundaClient> mockCamundaClient = new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
                                     int recipeTimeout, String requestAction,
                                     String serviceInstanceId, String vnfId, String vfModuleId,
                                     String volumeGroupId, String networkId, String configurationId, String serviceType,
                                     String vnfType, String vfModuleType, String networkType,
                                     String requestDetails, String recipeParamXsd) {
                ProtocolVersion pv = new ProtocolVersion("HTTP", 1, 1);
                HttpResponse resp = new BasicHttpResponse(pv, 202,
                        "test response");
                BasicHttpEntity entity = new BasicHttpEntity();
                String body = "{\"response\":\"success\",\"message\":\"success\"}";
                InputStream instream = new ByteArrayInputStream(body.getBytes());
                entity.setContent(instream);
                resp.setEntity(entity);
                return resp;
            }
        };

        E2EServiceInstances instance = new E2EServiceInstances();
        Response response = instance.scaleE2EServiceInstance(scaleRequest, "v3", "12345");
        mockMsoRequest.tearDown();
        mockCDB.tearDown();
        mockRequestClient.tearDown();
        mockCamundaClient.tearDown();
    }

    @Test
    public void scaleE2EserviceInstancesTestFailCamundaClient(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                              @Mocked Session session) {

        new Expectations() {{
            sessionFactoryManager.getSessionFactory().openSession(); result = session;
        }};

        final MockUp<MsoRequest> mockMsoRequest = new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord(Status status, Action action) {
                return;
            }
        };

        final MockUp<CatalogDatabase> mockCDB = new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) {
                Service svc = new Service();
                return svc;
            }

            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                ServiceRecipe rec = new ServiceRecipe();
                rec.setOrchestrationUri("/test/delE2E");
                rec.setRecipeTimeout(180);
                return rec;
            }
        };

        final MockUp<RequestClientFactory> mockRequestClient = new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI,
                                                  MsoJavaProperties props) throws IllegalStateException {
                RequestClient client = new CamundaClient();
                client.setUrl("/test/url");
                return client;
            }
        };

        E2EServiceInstances instance = new E2EServiceInstances();
        Response response = instance.scaleE2EServiceInstance(scaleRequest, "v3", "12345");
        mockMsoRequest.tearDown();
        mockCDB.tearDown();
        mockRequestClient.tearDown();
    }

    @Test
    public void scaleE2EserviceInstancesTestFailRequestClient(@Mocked AbstractSessionFactoryManager sessionFactoryManager,
                                                              @Mocked Session session) {

        final MockUp<MsoRequest> mockMsoRequest = new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord(Status status, Action action) {
                return;
            }
        };

        final MockUp<CatalogDatabase> mockCDB = new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName(String modelName) {
                Service svc = new Service();
                return svc;
            }

            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID(String modelUUID,
                                                             String action) {
                ServiceRecipe rec = new ServiceRecipe();
                rec.setOrchestrationUri("/test/delE2E");
                rec.setRecipeTimeout(180);
                return rec;
            }
        };

        E2EServiceInstances instance = new E2EServiceInstances();
        Response response = instance.scaleE2EServiceInstance(scaleRequest, "v3", "12345");
        mockMsoRequest.tearDown();
        mockCDB.tearDown();
    }

}
