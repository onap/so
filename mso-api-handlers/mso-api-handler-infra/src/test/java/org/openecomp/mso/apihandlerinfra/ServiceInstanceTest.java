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


import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openecomp.mso.apihandler.common.CamundaClient;
import org.openecomp.mso.apihandler.common.RequestClient;
import org.openecomp.mso.apihandler.common.RequestClientFactory;
import org.openecomp.mso.db.catalog.CatalogDatabase;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.ServiceRecipe;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.requestsdb.InfraActiveRequests;
import org.openecomp.mso.requestsdb.RequestsDatabase;

import mockit.Mock;
import mockit.MockUp;

public class ServiceInstanceTest {

	/*** Create Service Instance Test Cases ***/
	
	@Test
	public void createServiceInstanceInvalidModelInfo(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v5");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid model-info is specified") != -1);
	}
	
	@Test
	public void createServiceInstanceNormalDuplicate(){
		new MockUp<RequestsDatabase>() {
            @Mock
            public InfraActiveRequests checkInstanceNameDuplicate (HashMap<String,String> instanceIdMap, String instanceName, String requestScope) {
                return new InfraActiveRequests();
            }
        };
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Locked instance - This service (testService) already has a request being worked with a status of null (RequestId - null). The existing request must finish or be cleaned up before proceeding.") != -1);
	}
	
	@Test
	public void createServiceInstanceTestDBException(){
		new MockUp<RequestsDatabase>() {
            @Mock
            public InfraActiveRequests checkInstanceNameDuplicate (HashMap<String,String> instanceIdMap, String instanceName, String requestScope) {
                return null;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName (String defaultServiceModelName) {
            	Service serviceRecord = new Service();
            	serviceRecord.setModelUUID("2883992993");
            	return serviceRecord;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID (String uuid,String action) {
            	ServiceRecipe recipe =new ServiceRecipe();
            	recipe.setOrchestrationUri("/test/mso");
            	recipe.setRecipeTimeout(1000);
            	return recipe;
            }
        };
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Exception while creating record in DB null") != -1);
	}
	
	@Test
	public void createServiceInstanceTestBpmnFail(){
		new MockUp<RequestsDatabase>() {
            @Mock
            public InfraActiveRequests checkInstanceNameDuplicate (HashMap<String,String> instanceIdMap, String instanceName, String requestScope) {
                return null;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName (String defaultServiceModelName) {
            	Service serviceRecord = new Service();
            	serviceRecord.setModelUUID("2883992993");
            	return serviceRecord;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID (String uuid,String action) {
            	ServiceRecipe recipe =new ServiceRecipe();
            	recipe.setOrchestrationUri("/test/mso");
            	recipe.setRecipeTimeout(1000);
            	return recipe;
            }
        };
        
        new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord (Status status, Action action) {
            	return;
            }
        };
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Failed calling bpmn properties") != -1);
	}
	
	@Test(expected = Exception.class)
	public void createServiceInstanceTest200Http(){
		new MockUp<RequestsDatabase>() {
            @Mock
            public InfraActiveRequests checkInstanceNameDuplicate (HashMap<String,String> instanceIdMap, String instanceName, String requestScope) {
                return null;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName (String defaultServiceModelName) {
            	Service serviceRecord = new Service();
            	serviceRecord.setModelUUID("2883992993");
            	return serviceRecord;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID (String uuid,String action) {
            	ServiceRecipe recipe =new ServiceRecipe();
            	recipe.setOrchestrationUri("/test/mso");
            	recipe.setRecipeTimeout(1000);
            	return recipe;
            }
        };
        
        new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord (Status status, Action action) {
            	return;
            }
        };
        
        new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI, MsoJavaProperties props) throws IllegalStateException{
            	RequestClient client = new CamundaClient();
            	client.setUrl("/test/url");
            	return client;
            }
        };
        
        new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
        			int recipeTimeout, String requestAction, String serviceInstanceId,
        			String vnfId, String vfModuleId, String volumeGroupId, String networkId,
        			String serviceType, String vnfType, String vfModuleType, String networkType,
        			String requestDetails, String recipeParamXsd){ 
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
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
	}
	
	@Test
	public void createServiceInstanceTest500Http(){
		new MockUp<RequestsDatabase>() {
            @Mock
            public InfraActiveRequests checkInstanceNameDuplicate (HashMap<String,String> instanceIdMap, String instanceName, String requestScope) {
                return null;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName (String defaultServiceModelName) {
            	Service serviceRecord = new Service();
            	serviceRecord.setModelUUID("2883992993");
            	return serviceRecord;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID (String uuid,String action) {
            	ServiceRecipe recipe =new ServiceRecipe();
            	recipe.setOrchestrationUri("/test/mso");
            	recipe.setRecipeTimeout(1000);
            	return recipe;
            }
        };
        
        new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord (Status status, Action action) {
            	return;
            }
        };
        
        new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI, MsoJavaProperties props) throws IllegalStateException{
            	RequestClient client = new CamundaClient();
            	client.setUrl("/test/url");
            	return client;
            }
        };
        
        new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
        			int recipeTimeout, String requestAction, String serviceInstanceId,
        			String vnfId, String vfModuleId, String volumeGroupId, String networkId,
        			String serviceType, String vnfType, String vfModuleType, String networkType,
        			String requestDetails, String recipeParamXsd){ 
            	ProtocolVersion pv = new ProtocolVersion("HTTP",1,1);
            	HttpResponse resp = new BasicHttpResponse(pv,500, "test response");
            	BasicHttpEntity entity = new BasicHttpEntity();
            	String body = "{\"response\":\"success\",\"message\":\"success\"}";
            	InputStream instream = new ByteArrayInputStream(body.getBytes());
            	entity.setContent(instream);
            	resp.setEntity(entity);
            	return resp;
            }
        };
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Request Failed due to BPEL error with HTTP Status") != -1);
	}
	
	@Test
	public void createServiceInstanceTestVnfModelType(){
		new MockUp<RequestsDatabase>() {
            @Mock
            public InfraActiveRequests checkInstanceNameDuplicate (HashMap<String,String> instanceIdMap, String instanceName, String requestScope) {
                return null;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName (String defaultServiceModelName) {
            	Service serviceRecord = new Service();
            	serviceRecord.setModelUUID("2883992993");
            	return serviceRecord;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID (String uuid,String action) {
            	ServiceRecipe recipe =new ServiceRecipe();
            	recipe.setOrchestrationUri("/test/mso");
            	recipe.setRecipeTimeout(1000);
            	return recipe;
            }
        };
        
        new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord (Status status, Action action) {
            	return;
            }
        };
        
        new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI, MsoJavaProperties props) throws IllegalStateException{
            	RequestClient client = new CamundaClient();
            	client.setUrl("/test/url");
            	return client;
            }
        };
        
        new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
        			int recipeTimeout, String requestAction, String serviceInstanceId,
        			String vnfId, String vfModuleId, String volumeGroupId, String networkId,
        			String serviceType, String vnfType, String vfModuleType, String networkType,
        			String requestDetails, String recipeParamXsd){ 
            	ProtocolVersion pv = new ProtocolVersion("HTTP",1,1);
            	HttpResponse resp = new BasicHttpResponse(pv,500, "test response");
            	BasicHttpEntity entity = new BasicHttpEntity();
            	String body = "{\"response\":\"success\",\"message\":\"success\"}";
            	InputStream instream = new ByteArrayInputStream(body.getBytes());
            	entity.setContent(instream);
            	resp.setEntity(entity);
            	return resp;
            }
        };
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"vnf\",\"modelName\":\"serviceModel\",\"modelCustomizationName\":\"test\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v5");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("No valid modelVersionId is specified") != -1);
	}
	
	@Test
	public void createServiceInstanceTestNullHttpResp(){
		new MockUp<RequestsDatabase>() {
            @Mock
            public InfraActiveRequests checkInstanceNameDuplicate (HashMap<String,String> instanceIdMap, String instanceName, String requestScope) {
                return null;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public Service getServiceByModelName (String defaultServiceModelName) {
            	Service serviceRecord = new Service();
            	serviceRecord.setModelUUID("2883992993");
            	return serviceRecord;
            }
        };
        new MockUp<CatalogDatabase>() {
            @Mock
            public ServiceRecipe getServiceRecipeByModelUUID (String uuid,String action) {
            	ServiceRecipe recipe =new ServiceRecipe();
            	recipe.setOrchestrationUri("/test/mso");
            	recipe.setRecipeTimeout(1000);
            	return recipe;
            }
        };
        
        new MockUp<MsoRequest>() {
            @Mock
            public void createRequestRecord (Status status, Action action) {
            	return;
            }
        };
        
        new MockUp<RequestClientFactory>() {
            @Mock
            public RequestClient getRequestClient(String orchestrationURI, MsoJavaProperties props) throws IllegalStateException{
            	RequestClient client = new CamundaClient();
            	client.setUrl("/test/url");
            	return client;
            }
        };
        
        new MockUp<CamundaClient>() {
            @Mock
            public HttpResponse post(String requestId, boolean isBaseVfModule,
        			int recipeTimeout, String requestAction, String serviceInstanceId,
        			String vnfId, String vfModuleId, String volumeGroupId, String networkId,
        			String serviceType, String vnfType, String vfModuleType, String networkType,
        			String requestDetails, String recipeParamXsd){ 
            	return null;
            }
        };
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("bpelResponse is null") != -1);
	}
	
	@Test
	public void createServiceInstanceNormalNullDBFatch(){
		new MockUp<RequestsDatabase>() {
            @Mock
            private List<InfraActiveRequests> executeInfraQuery (List <Criterion> criteria, Order order) {
                return Collections.EMPTY_LIST;
            }
        };
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Recipe could not be retrieved from catalog DB null") != -1);
	}
	
	
	@Test
	public void createServiceInstanceInvalidModelVersionId(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v5");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid modelVersionId is specified") != -1);
	}
	
	@Test
	public void createServiceInstanceNullInstanceName(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid instanceName is specified") != -1);
	}
	
	
	@Test
	public void createServiceInstanceNullModelInfo(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": false}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid model-info is specified") != -1);
	}
	
	@Test
	public void createServiceInstanceInvalidModelInvariantId(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": false},\"modelInfo\":{\"modelInvariantId\": \"1234\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid modelType is specified") != -1);
	}
	
	@Test
	public void createServiceInstanceNullModelVersion(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": false},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid modelType is specified") != -1);
	}
	
	
	@Test
	public void createServiceInstanceNullModelType(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": false},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid modelType is specified") != -1);
	}
	
	@Test
	public void createServiceInstanceInvalidModelType(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": false},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"testmodel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Mapping of request to JSON object failed.") != -1);
	}
	
	@Test
	public void createServiceInstanceNullModelName(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": false},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid modelName is specified") != -1);
	}
	
	@Test
	public void createServiceInstanceInvalidVersionForAutoBuildVfModules(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": true},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  AutoBuildVfModule is not valid in the v2 version") != -1);
	}
	
	@Test
	public void createServiceInstanceNullRequestParameter(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": false},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid subscriptionServiceType is specified") != -1);
	}
	
	@Test
	public void createServiceInstanceNullSubscriptionType(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"},\"requestParameters\": { \"autoBuildVfModules\": false},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respBody = resp.getEntity().toString();
		assertTrue(respBody.indexOf("Error parsing request.  No valid subscriptionServiceType is specified") != -1);
	}
	
	@Test
	public void createServiceInstanceAnbormalInvalidJson(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"name\":\"test\"}";
		Response resp = instance.createServiceInstance(requestJson, "v2");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Mapping of request to JSON object failed") != -1);
	}
	
	/*** Activate Service Instance Test Cases ***/
	
	@Test
	public void activateServiceInstanceAnbormalInvalidJson(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"name\":\"test\"}";
		Response resp = instance.activateServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Mapping of request to JSON object failed") != -1);
	}
	
	@Test
	public void activateServiceInstanceInvalidModelVersionId(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d37\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.activateServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Error parsing request.  No valid modelVersionId in relatedInstance is specified") != -1);
	}
	
	@Test
	public void activateServiceInstanceInvalidServiceInstanceId(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d37\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.activateServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Error parsing request.  No valid serviceInstanceId matching the serviceInstanceId in request URI is specified") != -1);
	}
	
	@Test
	public void activateServiceInstanceTestNormal(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.activateServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("SVC2000") != -1);
	}
	
	/*** Deactivate Service Instance Test Cases ***/
	
	@Test
	public void deactivateServiceInstanceAnbormalInvalidJson(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"name\":\"test\"}";
		Response resp = instance.deactivateServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Mapping of request to JSON object failed") != -1);
	}
	
	@Test
	public void deactivateServiceInstanceInvalidModelVersionId(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d37\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.deactivateServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Error parsing request.  No valid modelVersionId in relatedInstance is specified") != -1);
	}
	
	@Test
	public void deactivateServiceInstanceInvalidServiceInstanceId(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d37\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.deactivateServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Error parsing request.  No valid serviceInstanceId matching the serviceInstanceId in request URI is specified") != -1);
	}
	
	@Test
	public void deactivateServiceInstanceTestNormal(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.deactivateServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("SVC2000") != -1);
	}
	
	/*** Delete Service Instance Test Cases ***/
	
	@Test
	public void deleteServiceInstanceAnbormalInvalidJson(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"name\":\"test\"}";
		Response resp = instance.deleteServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Mapping of request to JSON object failed") != -1);
	}
	
	@Test
	public void deleteServiceInstanceInvalidModelVersionId(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d37\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.deleteServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Error parsing request.  No valid modelVersionId is specified") != -1);
	}
	
	@Test
	public void deleteServiceInstanceInvalidServiceInstanceId(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d37\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\"}}}";
		Response resp = instance.deleteServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Error parsing request.  No valid modelVersionId is specified") != -1);
	}
	
	@Test
	public void deleteServiceInstanceTestNormal(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"288393\",\"modelCustomizationId\":\"389823213\"}}}";
		Response resp = instance.deleteServiceInstance(requestJson, "v5","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("SVC2000") != -1);
	}
	
	/*** Create Vnf Instance Test Cases ***/
	
	@Test
	public void createVNFInstanceTestInvalidCloudConfiguration(){
		ServiceInstances instance = new ServiceInstances();
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"288393\",\"modelCustomizationId\":\"389823213\"}}}";
		Response resp = instance.createVnfInstance(requestJson, "v3","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Error parsing request.  No valid cloudConfiguration is specified") != -1);
	}
	
	@Test
	public void createVNFInstanceTestInvalidIcpCloudRegionId(){
		ServiceInstances instance = new ServiceInstances();
		String s = "\"cloudConfiguration\":{}";
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"cloudConfiguration\":{}, \"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"288393\",\"modelCustomizationId\":\"389823213\"}}}";
		Response resp = instance.createVnfInstance(requestJson, "v3","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Error parsing request.  No valid lcpCloudRegionId is specified") != -1);
	}
	
	@Test
	public void createVNFInstanceTestInvalidTenantId(){
		ServiceInstances instance = new ServiceInstances();
		String s = "\"cloudConfiguration\":{}";
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"cloudConfiguration\":{\"lcpCloudRegionId\":\"2993841\"}, \"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"288393\",\"modelCustomizationId\":\"389823213\"}}}";
		Response resp = instance.createVnfInstance(requestJson, "v3","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("Error parsing request.  No valid tenantId is specified") != -1);
	}
	
	@Test
	public void createVNFInstanceTestNormal(){
		ServiceInstances instance = new ServiceInstances();
		String s = "\"cloudConfiguration\":{}";
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"cloudConfiguration\":{\"lcpCloudRegionId\":\"2993841\",\"tenantId\":\"2910032\"}, \"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"288393\",\"modelCustomizationId\":\"389823213\"}}}";
		Response resp = instance.createVnfInstance(requestJson, "v3","557ea944-c83e-43cf-9ed7-3a354abd6d34");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("SVC2000") != -1);
	}
	
	/*** Replace Vnf Instance Test Cases ***/
	@Test
	public void replaceVNFInstanceTestNormal(){
		ServiceInstances instance = new ServiceInstances();
		String s = "\"cloudConfiguration\":{}";
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"cloudConfiguration\":{\"lcpCloudRegionId\":\"2993841\",\"tenantId\":\"2910032\"}, \"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"288393\",\"modelCustomizationId\":\"389823213\"}}}";
		Response resp = instance.replaceVnfInstance(requestJson, "v3","557ea944-c83e-43cf-9ed7-3a354abd6d34","557ea944-c83e-43cf-9ed7-3a354abd6d93");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("SVC2000") != -1);
	}
	
	/*** Update Vnf Instance Test Cases ***/
	
	@Test
	public void updateVNFInstanceTestNormal(){
		ServiceInstances instance = new ServiceInstances();
		String s = "\"cloudConfiguration\":{}";
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"cloudConfiguration\":{\"lcpCloudRegionId\":\"2993841\",\"tenantId\":\"2910032\"}, \"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"288393\",\"modelCustomizationId\":\"389823213\"}}}";
		Response resp = instance.updateVnfInstance(requestJson, "v3","557ea944-c83e-43cf-9ed7-3a354abd6d34","557ea944-c83e-43cf-9ed7-3a354abd6d93");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("SVC2000") != -1);
	}
	
	/*** Update Vnf Instance Test Cases ***/
	
	@Test
	public void deleteVNFInstanceTestNormal(){
		ServiceInstances instance = new ServiceInstances();
		String s = "\"cloudConfiguration\":{}";
		String requestJson = "{\"serviceInstanceId\":\"1882939\","
				+"\"vnfInstanceId\":\"1882938\","
				+"\"networkInstanceId\":\"1882937\","
				+"\"volumeGroupInstanceId\":\"1882935\","
				+"\"vfModuleInstanceId\":\"1882934\","
				+ "\"requestDetails\": {\"cloudConfiguration\":{\"lcpCloudRegionId\":\"2993841\",\"tenantId\":\"2910032\"}, \"relatedInstanceList\" :[{\"relatedInstance\":{\"instanceName\":\"testInstance\",\"instanceId\":\"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"4839499\"}}}],\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\",\"instanceName\": \"testService\"},\"requestParameters\": { \"autoBuildVfModules\": false,\"subscriptionServiceType\": \"test\"},\"modelInfo\":{\"modelInvariantId\": \"557ea944-c83e-43cf-9ed7-3a354abd6d34\",\"modelVersion\":\"v2\",\"modelType\":\"service\",\"modelName\":\"serviceModel\",\"modelVersionId\":\"288393\",\"modelCustomizationId\":\"389823213\"}}}";
		Response resp = instance.deleteVnfInstance(requestJson, "v3","557ea944-c83e-43cf-9ed7-3a354abd6d34","557ea944-c83e-43cf-9ed7-3a354abd6d93");
		String respStr = resp.getEntity().toString();
		assertTrue(respStr.indexOf("SVC2000") != -1);
	}
}
