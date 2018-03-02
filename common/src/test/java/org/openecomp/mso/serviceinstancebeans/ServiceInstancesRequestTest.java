package org.openecomp.mso.serviceinstancebeans;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceInstancesRequestTest {

	@Test
	public void serializeServiceInstance() {
		String incomingRequest = "{\"requestDetails\":{\"modelInfo\":{\"modelInvariantUuid\":\"9647dfc4-2083-11e7-93ae-92361f002672\",\"modelType\":\"configuration\",\"modelName\":\"MSO-Configuration\",\"modelVersion\":\"1.0\",\"modelUuid\":\"36a3a8ea-49a6-4ac8-b06c-89a545444455\",\"modelCustomizationUuid\":\"68dc9a92-214c-11e7-93ae-92361f002671\"},\"requestInfo\":{\"source\":\"VID\",\"instanceName\":\"port_mirror_config_m830-01\",\"suppressRollback\":false,\"requestorId\":\"mdg1215\"},\"relatedInstanceList\":[{\"relatedInstance\":{\"instanceId\":\"956121e0-542d-4b30-b8c7-be611e3c8f13\",\"modelInfo\":{\"modelInvariantUuid\":\"9647dfc4-2083-11e7-93ae-92361f002671\",\"modelType\":\"service\",\"modelName\":\"MSOTADevInfra_vSAMP10a_Service\",\"modelVersion\":\"1.0\",\"modelUuid\":\"5df8b6de-2083-11e7-93ae-92361f002671\"}}},{\"relatedInstance\":{\"instanceId\":\"956121e0-542d-4b30-b8c7-be611e3c8f11\",\"modelInfo\":{\"modelInvariantUuid\":\"36a3a8ea-49a6-4ac8-b06c-89a545444456\",\"modelType\":\"vnf\",\"modelName\":\"testvnf\",\"modelVersion\":\"1.0\",\"modelUuid\":\"956121e0-542d-4b30-b8c7-be611e3c8f14\",\"modelCustomizationUuid\":\"68dc9a92-214c-11e7-93ae-92361f002676\"},\"instanceDirection\":\"source\"}},{\"relatedInstance\":{\"instanceId\":\"956121e0-542d-4b30-b8c7-be611e3c8f12\",\"modelInfo\":{\"modelInvariantUuid\":\"36a3a8ea-49a6-4ac8-b06c-89a545444477\",\"modelType\":\"vnf\",\"modelName\":\"svProbe vnf model name\",\"modelVersion\":\"1.0\",\"modelUuid\":\"36a3a8ea-49a6-4ac8-b06c-89a545444488\",\"modelCustomizationUuid\":\"68dc9a92-214c-11e7-93ae-92361f002672\"},\"instanceDirection\":\"destination\"}}],\"cloudConfiguration\":{\"lcpCloudRegionId\":\"mdt1\"},\"requestParameters\":{\"subscriptionServiceType\":\"MSO-dev-service-type\",\"userParams\":[{\"name\":\"someUserParam\",\"value\":\"someValue\"}],\"aLaCarte\":false,\"autoBuildVfModules\":false,\"cascadeDelete\":false,\"usePreload\":true,\"rebuildVolumeGroups\":false}},\"serviceInstanceId\":\"956121e0-542d-4b30-b8c7-be611e3c8f13\"}";
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			ServiceInstancesRequest request = mapper.readValue(incomingRequest, ServiceInstancesRequest.class);
			System.out.println("Java Object: " + request);
		} catch (Exception e) {
			System.out.println("Caught Exception " + e.getMessage());
			 Assert.fail("Caught error on object serialization");
		}
	}
}
