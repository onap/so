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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.apihandler.common.CamundaClient;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.properties.MsoDatabaseException;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.OperationStatus;
import org.openecomp.mso.requestsdb.RequestsDatabase;

import mockit.Mock;
import mockit.MockUp;

public class E2EServiceInstancesTest {

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
					String volumeGroupId, String networkId, String serviceType,
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
					String volumeGroupId, String networkId, String serviceType,
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
					String volumeGroupId, String networkId, String serviceType,
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
					String volumeGroupId, String networkId, String serviceType,
					String vnfType, String vfModuleType, String networkType,
					String requestDetails, String recipeParamXsd) {
				HttpResponse resp = null;
				return resp;
			}
		};

		E2EServiceInstances instance = new E2EServiceInstances();
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
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
            .contains("Mapping of request to JSON object failed.  No content to map to Object due to end of input"));
	}

	@Test
	public void deleteE2EServiceInstanceTestNormal() {
		E2EServiceInstances instance = new E2EServiceInstances();
		String request = "{\"globalSubscriberId\":\"299392392\",\"serviceType\":\"VoLTE\"}";
		Response resp = instance.deleteE2EServiceInstance(request, "v3",
				"12345678");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.contains("SVC1000"));
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
					String volumeGroupId, String networkId, String serviceType,
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
		Response resp = instance.updateE2EServiceInstance(request, "v3", "12345");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("success") != -1);
	}
	
	@Test
	public void updateE2EServiceInstanceTestChkStatusFalse() {
		new MockUp<RequestsDatabase>() {
			@Mock
			public OperationStatus getOperationStatusByServiceId(
					String serviceID) {
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
					String volumeGroupId, String networkId, String serviceType,
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
		String request = "{\"service\":{\"name\":\"so_test4\",\"description\":\"so_test2\",\"serviceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561519\",\"templateId\":\"592f9437-a9c0-4303-b9f6-c445bb7e9814\",\"parameters\":{\"globalSubscriberId\":\"123457\",\"subscriberName\":\"Customer1\",\"serviceType\":\"voLTE\",\"templateName\":\"voLTE Service:1.0\",\"resources\":[{\"resourceName\":\"vIMS\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-vBAS-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}},{\"vnfProfileId\":\"zte-vMME-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad0\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"vEPC\",\"resourceDefId\":\"61c3e96e-0970-4871-b6e0-3b6de7561516\",\"resourceId\":\"62c3e96e-0970-4871-b6e0-3b6de7561512\",\"nsParameters\":{\"locationConstraints\":[{\"vnfProfileId\":\"zte-CSCF-1.0\",\"locationConstraints\":{\"vimId\":\"4050083f-465f-4838-af1e-47a545222ad1\"}}],\"additionalParamForNs\":{}}},{\"resourceName\":\"underlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561513\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561514\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}},{\"resourceName\":\"overlayvpn\",\"resourceDefId\":\"60c3e96e-0970-4871-b6e0-3b6de7561517\",\"resourceId\":\"60c3e96e-0970-4871-b6e0-3b6de7561518\",\"nsParameters\":{\"locationConstraints\":[],\"additionalParamForNs\":{\"externalDataNetworkName\":\"Flow_out_net\",\"m6000_mng_ip\":\"181.18.20.2\",\"externalCompanyFtpDataNetworkName\":\"Flow_out_net\",\"externalPluginManageNetworkName\":\"plugin_net_2014\",\"externalManageNetworkName\":\"mng_net_2017\",\"sfc_data_network\":\"sfc_data_net_2016\",\"NatIpRange\":\"210.1.1.10-210.1.1.20\",\"location\":\"4050083f-465f-4838-af1e-47a545222ad0\",\"sdncontroller\":\"9b9f02c0-298b-458a-bc9c-be3692e4f35e\"}}}]}}}";
		Response resp = instance.updateE2EServiceInstance(request, "v3", "12345");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("SVC2000") != -1);
	}
}
