package org.openecomp.mso.apihandlerinfra;


import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

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
