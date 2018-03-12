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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openecomp.mso.apihandler.common.ValidationException;
import org.openecomp.mso.serviceinstancebeans.ServiceInstancesRequest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class MsoRequestTest {
	private ObjectMapper mapper = new ObjectMapper();
	private HashMap<String, String> instanceIdMapTest = new HashMap<>();
	private ServiceInstancesRequest sir;
	private MsoRequest msoRequest;
	private Action action;
	private String version;
	private String originalRequestJSON;
	private String requestJSON;
	private boolean expected;
	private String expectedException;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	
	@Before
	public void validate(){
		msoRequest = new MsoRequest();
	}
	public String inputStream(String JsonInput)throws IOException{
		String input = IOUtils.toString(ClassLoader.class.getResourceAsStream (JsonInput), CharEncoding.UTF_8);
		return input;
	}
	@Test
	public void nullInstanceIdMapTest() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.sir = mapper.readValue(inputStream("/RequestParametersNull.json"), ServiceInstancesRequest.class);
		this.instanceIdMapTest = null;
		thrown.expect(NullPointerException.class);
		this.msoRequest = new MsoRequest("nullINstanceIdMap");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
	}
	@Test
	@Parameters(method = "successParameters")
	public void successTest(ServiceInstancesRequest sir, HashMap<String, String> instanceIdMapTest, Action action, String version) throws ValidationException{
		this.sir = sir;
		this.instanceIdMapTest = instanceIdMapTest;
		this.action = action;
		this.version = version;
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.instanceIdMapTest.put("vnfInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.msoRequest = new MsoRequest("successTest");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
	}
	@Parameters
	private Collection<Object[]> successParameters() throws JsonParseException, JsonMappingException, IOException{
		return Arrays.asList(new Object[][]{
			{mapper.readValue(inputStream("/EmptyRequestorId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v3"},
			{mapper.readValue(inputStream("/ValidModelCustomizationId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v4"},
			{mapper.readValue(inputStream("/EmptyCloudConfiguration.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v3"},
			{mapper.readValue(inputStream("/RelatedInstancesModelInvariantId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "v5"},
			{mapper.readValue(inputStream("/PlatformTest.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v5"},
			{mapper.readValue(inputStream("/v5EnablePortMirrorService.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.enablePort, "v5"},
			{mapper.readValue(inputStream("/ValidModelCustomizationId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "v5"},
			{mapper.readValue(inputStream("/ValidModelCustomizationIdService.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "v5"},
			{mapper.readValue(inputStream("/ServiceProductFamilyIdUpdate.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v5"},
			{mapper.readValue(inputStream("/ServiceProductFamilyIdFlag.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v5"},		
			{mapper.readValue(inputStream("/VnfModelCustomizationIdEmpty.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "v5"},
			{mapper.readValue(inputStream("/RelatedInstancesVfModule.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{mapper.readValue(inputStream("/RelatedInstances.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{mapper.readValue(inputStream("/VnfModelCustomizationNameNull.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "v5"},
			{mapper.readValue(inputStream("/VnfModelCustomizationTest.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "v5"},
			{mapper.readValue(inputStream("/ServiceModelNameEmptyOnDelete.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "v2"},
			{mapper.readValue(inputStream("/ServiceModelNameEmptyOnActivate.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v2"},
			{mapper.readValue(inputStream("/ModelVersionNetwork.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "v3"},
			{mapper.readValue(inputStream("/ModelInvariantIdService.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deactivateInstance, "v4"},
			{mapper.readValue(inputStream("/ModelInvariantIdConfigurationDelete.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "v3"},
			{mapper.readValue(inputStream("/ModelCustomizationIdUsingPreload.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "v5"},
			{mapper.readValue(inputStream("/ServiceInPlaceSoftwareUpdate.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.inPlaceSoftwareUpdate, "v6"},
			{mapper.readValue(inputStream("/EmptyOwningEntityName.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{mapper.readValue(inputStream("/VnfRequestParameters.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "v6"},
			{mapper.readValue(inputStream("/VnfActivate.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v6"},
			{mapper.readValue(inputStream("/ServiceInPlaceSoftwareUpdate.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.applyUpdatedConfig, "v6"},
			{mapper.readValue(inputStream("/ProjectAndOwningEntity2.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v6"},
			{mapper.readValue(inputStream("/PlatformAndLineOfBusiness2.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v6"},
			{mapper.readValue(inputStream("/UserParams.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"}
		});
	}
	@Test
	@Parameters(method = "aLaCarteParameters")
	public void aLaCarteFlagTest(boolean expected, ServiceInstancesRequest sir, HashMap<String, String> instanceIdMapTest, Action action, String version) throws JsonParseException, IOException, ValidationException{
		this.expected = expected;
		this.sir = sir;
		this.instanceIdMapTest = instanceIdMapTest;
		this.action = action;
		this.version = version;
		this.msoRequest = new MsoRequest("aLaCarteCheck");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
		assertEquals(expected, msoRequest.getALaCarteFlag());
	}
	@Parameters
	private Collection<Object[]> aLaCarteParameters() throws IOException{
		return Arrays.asList(new Object[][] {
			{false, mapper.readValue(inputStream("/RequestParametersNull.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "v3"},
			{true, mapper.readValue(inputStream("/RequestParametersALaCarteTrue.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v3"},
			{true, mapper.readValue(inputStream("/v2requestParametersALaCarteFalse.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v2"},
		});
	}
	@Test
	@Parameters(method = "validationParameters")
	public void validationFailureTest(String expectedException, ServiceInstancesRequest sir, HashMap<String, String> instanceIdMapTest, Action action, String version) throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.expectedException = expectedException;
		this.sir = sir;
		this.instanceIdMapTest = instanceIdMapTest;
		this.action = action;
		this.version = version;
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.instanceIdMapTest.put("vnfInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		thrown.expect(ValidationException.class);
		thrown.expectMessage(expectedException);
		this.msoRequest = new MsoRequest("validationFailure");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
	}
	@Parameters
	private Collection<Object[]> validationParameters() throws IOException{
		return Arrays.asList(new Object[][] {
			{"No valid aLaCarte in requestParameters", mapper.readValue(inputStream("/RequestParametersNull.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.addRelationships, "v4"},
			{"No valid model-info is specified", mapper.readValue(inputStream("/ModelInfoNull.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid requestInfo is specified", mapper.readValue(inputStream("/RequestInfoNull.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelType is specified", mapper.readValue(inputStream("/ModelTypeNull.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid lcpCloudRegionId is specified", mapper.readValue(inputStream("/EmptyLcpCloudConfiguration.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid tenantId is specified", mapper.readValue(inputStream("/EmptyTenantId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid requestParameters is specified", mapper.readValue(inputStream("/RequestParametersNull.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid subscriptionServiceType is specified", mapper.readValue(inputStream("/EmptySubscriptionServiceType.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid instanceName format is specified", mapper.readValue(inputStream("/InvalidInstanceName.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v2"},
			{"No valid requestorId is specified", mapper.readValue(inputStream("/EmptyRequestorId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelInvariantId format is specified", mapper.readValue(inputStream("/InvalidModelInvariantId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v2"},
			{"No valid subscriberInfo is specified", mapper.readValue(inputStream("/EmptySubscriberInfo.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid globalSubscriberId is specified", mapper.readValue(inputStream("/EmptyGlobalSubscriberId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid platformName is specified", mapper.readValue(inputStream("/EmptyPlatform.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid platform is specified", mapper.readValue(inputStream("/Platform.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v6"},
			{"No valid lineOfBusinessName is specified", mapper.readValue(inputStream("/EmptyLineOfBusiness.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid projectName is specified", mapper.readValue(inputStream("/EmptyProject.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid owningEntity is specified", mapper.readValue(inputStream("/OwningEntity.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v6"},
			{"No valid owningEntityId is specified", mapper.readValue(inputStream("/EmptyOwningEntityId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelCustomizationId is specified", mapper.readValue(inputStream("/ModelCustomizationId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "v5"},
			{"No valid modelCustomizationId or modelCustomizationName is specified", mapper.readValue(inputStream("/VnfModelCustomizationId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "v5"},
			{"No valid modelName is specified", mapper.readValue(inputStream("/VfModuleModelName.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "v4"},
			{"No valid modelCustomizationId or modelCustomizationName is specified", mapper.readValue(inputStream("/VnfModelCustomizationId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid cloudConfiguration is specified", mapper.readValue(inputStream("/CloudConfiguration.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v4"},
			{"No valid modelVersionId is specified", mapper.readValue(inputStream("/ModelVersionId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelVersionId is specified", mapper.readValue(inputStream("/ConfigurationModelVersionId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid productFamilyId is specified", mapper.readValue(inputStream("/VnfProductFamilyId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v3"},
			{"No valid productFamilyId is specified", mapper.readValue(inputStream("/NetworkProductFamilyId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v3"},
			{"No valid productFamilyId is specified", mapper.readValue(inputStream("/NetworkProductFamilyId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v3"},
			{"No valid productFamilyId is specified", mapper.readValue(inputStream("/ServiceProductFamilyId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelVersionId in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesModelVersionId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelType in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesModelType.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelInfo in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesModelInfo.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid instanceName format in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesNameFormat.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid instanceId in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid instanceId format in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesIdFormat.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelInvariantId in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesModelInvariantId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelName in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesModelName.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelVersion in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesModelVersion.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelInvariantId format in relatedInstance is specified", mapper.readValue(inputStream("/RelatedInstancesModelInvariantIdFormat.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelCustomizationName or modelCustomizationId in relatedInstance of vnf is specified", mapper.readValue(inputStream("/RelatedInstancesModelCustomizationId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid serviceInstanceId matching the serviceInstanceId in request URI is specified", mapper.readValue(inputStream("/RelatedInstancesInstanceId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid vnfInstanceId matching the vnfInstanceId in request URI is specified", mapper.readValue(inputStream("/RelatedInstancesVnfInstanceId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid related vnf instance for volumeGroup request is specified", mapper.readValue(inputStream("/RelatedInstancesVnfInstance.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid related service instance for volumeGroup request is specified", mapper.readValue(inputStream("/RelatedInstancesServiceInstance.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid related vnf instance for vfModule request is specified", mapper.readValue(inputStream("/VfModuleRelatedInstancesVnf.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid related service instance for vfModule request is specified", mapper.readValue(inputStream("/VfModuleRelatedInstancesService.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid related service instance for vnf request is specified", mapper.readValue(inputStream("/VnfRelatedInstancesService.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid modelCustomizationId or modelCustomizationName is specified", mapper.readValue(inputStream("/VnfModelCustomizationIdPreload.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "v5"},
			{"No valid modelVersion is specified", mapper.readValue(inputStream("/ModelVersionService.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v3"},
			{"No valid modelVersion is specified", mapper.readValue(inputStream("/ModelVersionVfModule.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v3"},
			{"No valid modelInvariantId is specified", mapper.readValue(inputStream("/ModelInvariantId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deactivateInstance, "v4"},
			{"No valid modelInvariantId is specified", mapper.readValue(inputStream("/v5ModelInvariantIdNetwork.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.enablePort, "v5"},
			{"No valid modelInvariantId is specified", mapper.readValue(inputStream("/v5ModelInvariantIdDisablePort.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.disablePort, "v5"},
			{"No valid modelInvariantId is specified", mapper.readValue(inputStream("/ModelInvariantIdVnf.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v3"},
			{"No valid modelInvariantId is specified", mapper.readValue(inputStream("/ModelInvariantIdConfiguration.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v3"},
			{"No valid modelVersion is specified", mapper.readValue(inputStream("/ModelInvariantIdServiceCreate.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v3"},
			{"No valid modelVersionId is specified", mapper.readValue(inputStream("/v2ModelVersionId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v4"},
			{"No valid modelVersionId is specified", mapper.readValue(inputStream("/v2ModelVersionId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deactivateInstance, "v4"},
			{"No valid modelVersionId is specified", mapper.readValue(inputStream("/v2ModelVersionId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v5"},
			{"No valid modelVersionId is specified", mapper.readValue(inputStream("/v2ModelVersionId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.enablePort, "v5"},
			{"No valid modelVersionId is specified", mapper.readValue(inputStream("/v2ModelVersionId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.disablePort, "v5"},
			{"No valid modelCustomizationId or modelCustomizationName is specified", mapper.readValue(inputStream("/ModelVersionIdTest.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid modelVersionId is specified", mapper.readValue(inputStream("/ModelVersionIdCreate.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid requestParameters is specified", mapper.readValue(inputStream("/RequestParameters.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.applyUpdatedConfig, "v6"},
			{"No valid requestInfo is specified", mapper.readValue(inputStream("/RequestInfo.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.applyUpdatedConfig, "v6"},
			{"No valid requestorId is specified", mapper.readValue(inputStream("/RequestorId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.applyUpdatedConfig, "v6"},
			{"No valid requestParameters is specified", mapper.readValue(inputStream("/RequestParameters.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.inPlaceSoftwareUpdate, "v6"},
			{"No valid requestInfo is specified", mapper.readValue(inputStream("/RequestInfo.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.inPlaceSoftwareUpdate, "v6"},
			{"No valid requestorId is specified", mapper.readValue(inputStream("/RequestorId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.inPlaceSoftwareUpdate, "v6"},
			{"No valid cloudConfiguration is specified", mapper.readValue(inputStream("/InPlaceSoftwareUpdateCloudConfiguration.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.inPlaceSoftwareUpdate, "v6"},
			{"No valid lcpCloudRegionId is specified", mapper.readValue(inputStream("/InPlaceSoftwareUpdateCloudRegionId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.inPlaceSoftwareUpdate, "v6"},
			{"No valid tenantId is specified", mapper.readValue(inputStream("/InPlaceSoftwareUpdateTenantId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.inPlaceSoftwareUpdate, "v6"},
			{"No valid serviceInstanceId matching the serviceInstanceId in request URI is specified", mapper.readValue(inputStream("/v6VnfDeleteInstance.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deleteInstance, "v6"},
			{"No valid related instances is specified", mapper.readValue(inputStream("/ServiceNoRelatedInstance.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.addRelationships, "v2"},
			{"No valid related instances is specified", mapper.readValue(inputStream("/ServiceNoRelatedInstance.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.removeRelationships, "v2"},
			{"No valid modelCustomizationId or modelCustomizationName is specified", mapper.readValue(inputStream("/VnfRequestParameters.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.replaceInstance, "v6"},
			{"No valid modelCustomizationId or modelCustomizationName is specified", mapper.readValue(inputStream("/VnfRequestParameters.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v6"},
			{"No valid instanceName in relatedInstance for pnf modelType is specified", mapper.readValue(inputStream("/v6AddRelationshipsBadData.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.removeRelationships, "v6"},
			{"No valid instanceName in relatedInstance for pnf modelType is specified", mapper.readValue(inputStream("/v6AddRelationshipsBadData.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.addRelationships, "v6"},
			{"No valid modelInvariantId is specified", mapper.readValue(inputStream("/v5DeactivatePortMirrorBadData.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.deactivateInstance, "v5"},
			{"No valid modelVersionId is specified", mapper.readValue(inputStream("/v5ActivatePortMirrorBadData.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.activateInstance, "v5"},
			{"No valid related instances is specified", mapper.readValue(inputStream("/v5EnablePortMirrorNoRelatedInstance.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.disablePort, "v2"},
			{"No valid connectionPoint relatedInstance for Port Configuration is specified", mapper.readValue(inputStream("/v5EnablePortMirrorNoConnectionPoint.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.disablePort, "v5"},
			{"No valid connectionPoint relatedInstance for Port Configuration is specified", mapper.readValue(inputStream("/v5EnablePortMirrorNoConnectionPoint.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.enablePort, "v5"},
			{"No valid related instances is specified", mapper.readValue(inputStream("/v5EnablePortMirrorNoRelatedInstance.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.enablePort, "v5"},
			{"No valid modelCustomizationId is specified", mapper.readValue(inputStream("/v5PortMirrorCreateConfigurationBad.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid source vnf relatedInstance for Port Configuration is specified", mapper.readValue(inputStream("/v5PortMirrorCreateNoSourceRelatedInstance.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid destination vnf relatedInstance for Port Configuration is specified", mapper.readValue(inputStream("/v5PortMirrorCreateNoDestinationRelatedInstance.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid related instances is specified", mapper.readValue(inputStream("/v5PortMirrorCreateNoRelatedInstances.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{"No valid modelCustomizationId is specified", mapper.readValue(inputStream("/v4CreateVfModuleMissingModelCustomizationId.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v4"},
			{"No valid modelInvariantId is specified", mapper.readValue(inputStream("/v3DeleteServiceInstanceALaCarte.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v3"},
			{"No valid modelInvariantId is specified", mapper.readValue(inputStream("/v3UpdateNetworkBad.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v4"},
			{"No valid related instances is specified", mapper.readValue(inputStream("/v3VolumeGroupBad.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.updateInstance, "v4"}
		});
	}
	@Test
	public void setInstancedIdHashMapTest() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.sir = mapper.readValue(inputStream("/v2AutoBuildVfModulesTrue.json"), ServiceInstancesRequest.class);
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.instanceIdMapTest.put("vnfInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff001");
		this.instanceIdMapTest.put("vfModuleInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff002");
		this.instanceIdMapTest.put("volumeGroupInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff003");
		this.instanceIdMapTest.put("networkInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff004");
		this.instanceIdMapTest.put("configurationInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff005");
		this.action = Action.createInstance;
		this.version = "v5";
		this.msoRequest = new MsoRequest("setInstanceIdHashMap");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
		assertEquals("ff305d54-75b4-431b-adb2-eb6b9e5ff000", msoRequest.getServiceInstancesRequest().getServiceInstanceId());
		assertEquals("ff305d54-75b4-431b-adb2-eb6b9e5ff001", msoRequest.getServiceInstancesRequest().getVnfInstanceId());
		assertEquals("ff305d54-75b4-431b-adb2-eb6b9e5ff002", msoRequest.getServiceInstancesRequest().getVfModuleInstanceId());
		assertEquals("ff305d54-75b4-431b-adb2-eb6b9e5ff003", msoRequest.getServiceInstancesRequest().getVolumeGroupInstanceId());
		assertEquals("ff305d54-75b4-431b-adb2-eb6b9e5ff004", msoRequest.getServiceInstancesRequest().getNetworkInstanceId());
		assertEquals("ff305d54-75b4-431b-adb2-eb6b9e5ff005", msoRequest.getServiceInstancesRequest().getConfigurationId());
	}
	@Test
	public void serviceInstanceIdHashMapFailureTest() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.sir = mapper.readValue(inputStream("/v2AutoBuildVfModulesTrue.json"), ServiceInstancesRequest.class);
		this.instanceIdMapTest.put("serviceInstanceId", "test");
		this.action = Action.createInstance;
		this.version = "v5";
		thrown.expect(ValidationException.class);
		thrown.expectMessage("No valid serviceInstanceId is specified");
		this.msoRequest = new MsoRequest("serviceInstanceIdFailure");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
	}
	@Test
	public void vnfInstanceIdHashMapFailureTest() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.sir = mapper.readValue(inputStream("/v2AutoBuildVfModulesTrue.json"), ServiceInstancesRequest.class);
		this.instanceIdMapTest.put("vnfInstanceId", "test");
		this.action = Action.createInstance;
		this.version = "v5";
		thrown.expect(ValidationException.class);
		thrown.expectMessage("No valid vnfInstanceId is specified");
		this.msoRequest = new MsoRequest("vnfInstanceIdFailure");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
	}
	@Test
	public void vfModuleInstanceIdHashMapFailureTest() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.sir = mapper.readValue(inputStream("/v2AutoBuildVfModulesTrue.json"), ServiceInstancesRequest.class);
		this.instanceIdMapTest.put("vfModuleInstanceId", "test");
		this.action = Action.createInstance;
		this.version = "v5";
		thrown.expect(ValidationException.class);
		thrown.expectMessage("No valid vfModuleInstanceId is specified");
		this.msoRequest = new MsoRequest("vfModuleInstanceIdFailure");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
	}
	@Test
	public void volumeGroupInstanceIdHashMapFailureTest() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.sir = mapper.readValue(inputStream("/v2AutoBuildVfModulesTrue.json"), ServiceInstancesRequest.class);
		this.instanceIdMapTest.put("volumeGroupInstanceId", "test");
		this.action = Action.createInstance;
		this.version = "v5";
		thrown.expect(ValidationException.class);
		thrown.expectMessage("No valid volumeGroupInstanceId is specified");
		this.msoRequest = new MsoRequest("volumeGroupInstanceIdFailure");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
	}
	@Test
	public void networkInstanceIdHashMapFailureTest() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.sir = mapper.readValue(inputStream("/v2AutoBuildVfModulesTrue.json"), ServiceInstancesRequest.class);
		this.instanceIdMapTest.put("networkInstanceId", "test");
		this.action = Action.createInstance;
		this.version = "v5";
		thrown.expect(ValidationException.class);
		thrown.expectMessage("No valid networkInstanceId is specified");
		this.msoRequest = new MsoRequest("networkInstanceIdFailure");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
	}
	@Test
	public void configurationInstanceIdHashMapFailureTest() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.sir = mapper.readValue(inputStream("/v2AutoBuildVfModulesTrue.json"), ServiceInstancesRequest.class);
		this.instanceIdMapTest.put("configurationInstanceId", "test");
		this.action = Action.createInstance;
		this.version = "v5";
		thrown.expect(ValidationException.class);
		thrown.expectMessage("No valid configurationInstanceId is specified");
		this.msoRequest = new MsoRequest("configurationInstanceIdFailure");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
	}
	@Test
	public void setVolumeGroupRelatedInstancesTest() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.sir = mapper.readValue(inputStream("/VolumeGroupRelatedInstances.json"), ServiceInstancesRequest.class);
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.instanceIdMapTest.put("vnfInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff001");
		this.action = Action.createInstance;
		this.version = "v5";
		this.msoRequest = new MsoRequest("setVolumeGroupRelatedInstances");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
		assertEquals("vSAMP12", msoRequest.getServiceInstanceType());
		assertEquals("vSAMP12" + "/" + "test", msoRequest.getVnfType());
		assertEquals("1.0", msoRequest.getAsdcServiceModelVersion());
	}
	@Test
	@Parameters(method = "projectParameters")
	public void setProjectAndOwningEntityTest(ServiceInstancesRequest sir, HashMap<String, String> instanceIdMapTest, Action action, String version) throws ValidationException, JsonParseException, JsonMappingException, IOException{
		this.sir = sir;
		this.action = action;
		this.version = version;
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.msoRequest = new MsoRequest("setProjectAndOwningEntity");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
		assertEquals("projectName", msoRequest.getProject().getProjectName());
		assertEquals("oeId", msoRequest.getOwningEntity().getOwningEntityId());
		assertEquals("oeName", msoRequest.getOwningEntity().getOwningEntityName());
	}
	@Parameters
	private Collection<Object[]> projectParameters() throws IOException{
		return Arrays.asList(new Object[][] {
			{mapper.readValue(inputStream("/ProjectAndOwningEntity.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{mapper.readValue(inputStream("/ProjectAndOwningEntity.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v6"},
		});
	}
	
	@Test
	public void setModelInfoTest() throws ValidationException, JsonParseException, JsonMappingException, IOException{
		this.sir = mapper.readValue(inputStream("/RequestParametersALaCarteTrue.json"), ServiceInstancesRequest.class);
		this.action = Action.createInstance;
		this.version = "v5";
		this.msoRequest = new MsoRequest("setModelInfo");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
		assertEquals("test", msoRequest.getServiceInstancesRequest().getRequestDetails().getModelInfo().getModelVersionId());
	}
	@Test
	public void setServiceInstanceTypeTest() throws ValidationException, JsonParseException, JsonMappingException, IOException{
		this.sir = mapper.readValue(inputStream("/RequestParametersALaCarteTrue.json"), ServiceInstancesRequest.class);
		this.action = Action.createInstance;
		this.version = "v5";
		this.msoRequest = new MsoRequest("setServiceInstanceType");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
		assertEquals("SDNW Service 1710", msoRequest.getServiceInstanceType());
	}
	@Test
	@Parameters(method = "platformParameters")
	public void setPlatformAndLineOfBusinessTest(ServiceInstancesRequest sir, HashMap<String, String> instanceIdMapTest, Action action, String version) throws ValidationException, JsonParseException, JsonMappingException, IOException{
		this.sir = sir;
		this.action = action;
		this.instanceIdMapTest = instanceIdMapTest;
		this.version = version;
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.msoRequest = new MsoRequest("setPlatformAndLineOfBusiness");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
		assertEquals("platformName", msoRequest.getPlatform().getPlatformName());
		assertEquals("lobName", msoRequest.getLineOfBusiness().getLineOfBusinessName());
	}
	@Parameters
	private Collection<Object[]> platformParameters() throws IOException{
		return Arrays.asList(new Object[][] {
			{mapper.readValue(inputStream("/PlatformAndLineOfBusiness.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v5"},
			{mapper.readValue(inputStream("/PlatformAndLineOfBusiness.json"), ServiceInstancesRequest.class), instanceIdMapTest, Action.createInstance, "v6"},
		});
	}
	
	@Test
	public void setNetworkTypeTest() throws ValidationException, JsonParseException, JsonMappingException, IOException{
		this.sir = mapper.readValue(inputStream("/NetworkType.json"), ServiceInstancesRequest.class);
		this.action = Action.createInstance;
		this.version = "v2";
		this.msoRequest = new MsoRequest("setNetworkType");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
		assertEquals("TestNetworkType", msoRequest.getNetworkType());
	}
	@Test
	public void setModelNameVersionIdTest() throws ValidationException, JsonParseException, JsonMappingException, IOException{
		this.sir = mapper.readValue(inputStream("/ModelNameVersionId.json"), ServiceInstancesRequest.class);
		this.action = Action.createInstance;
		this.version = "v5";
		this.msoRequest = new MsoRequest("setModelNameVersionId");
		this.msoRequest.parse(sir, instanceIdMapTest, action, version, originalRequestJSON);
		assertEquals("test", msoRequest.getModelInfo().getModelNameVersionId());
	}
	@Test
	public void testParseOrchestration() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = " {\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\", \"requestorId\": \"zz9999\"}}}";
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("ParseOrchestration");
		msoRequest.parseOrchestration(sir);
		assertEquals(msoRequest.getRequestInfo().getSource(),"VID");
		assertEquals(msoRequest.getRequestInfo().getRequestorId(),"zz9999");

	}
	@Test
	public void testParseOrchestrationFailure() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = " {\"requestDetails\": {\"requestInfo\": { \"source\": \"VID\"}}}";
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		thrown.expect(ValidationException.class);
		thrown.expectMessage("No valid requestorId is specified");
		this. msoRequest = new MsoRequest ("ParseOrchestration");
		msoRequest.parseOrchestration(sir);
	}
	@Test
	public void testParseV3VnfCreate() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v3VnfCreate.json");
		this.instanceIdMapTest.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.createInstance, "v3", originalRequestJSON);
		assertEquals(msoRequest.getRequestInfo().getSource(),"VID");
		assertEquals(msoRequest.getReqVersion(),3);
	}
	@Test
	public void testParseV3UpdateNetwork() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v3UpdateNetwork.json");
		this.instanceIdMapTest.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.updateInstance, "v3", originalRequestJSON);
	}
	@Test
	public void testParseV3DeleteNetwork() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v3DeleteNetwork.json");
		this.instanceIdMapTest.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.deleteInstance, "v3", originalRequestJSON);
	}
	@Test
	public void testParseV3ServiceInstanceDelete() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		String requestJSON2;
		this.requestJSON = inputStream("/v3DeleteServiceInstance.json");
		requestJSON2 = inputStream("/v3DeleteServiceInstanceALaCarte.json");
		this.instanceIdMapTest.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.deleteInstance, "v3", originalRequestJSON);
		boolean testIsALaCarteSet = msoRequest.getServiceInstancesRequest().getRequestDetails().getRequestParameters().isaLaCarte();
		assertFalse(testIsALaCarteSet);
		this.sir  = mapper.readValue(requestJSON2, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("12345");
		msoRequest.parse(sir, instanceIdMapTest, Action.deleteInstance, "v3", originalRequestJSON);
		testIsALaCarteSet = msoRequest.getServiceInstancesRequest().getRequestDetails().getRequestParameters().isaLaCarte();
		assertTrue(testIsALaCarteSet);
		assertTrue(msoRequest.getALaCarteFlag());
	}
	@Test
	public void testVfModuleV4UsePreLoad() throws JsonParseException, JsonMappingException, IOException, ValidationException {
		this.requestJSON = inputStream("/v4CreateVfModule.json");
		this.instanceIdMapTest.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
		this.instanceIdMapTest.put("vnfInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.createInstance, "v4", originalRequestJSON);
		
		this.requestJSON = inputStream("/v4CreateVfModuleNoCustomizationId.json");
		this.instanceIdMapTest.put("serviceInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
		this.instanceIdMapTest.put("vnfInstanceId", "3eecada1-83a4-4f33-9ed2-7937e7b8dbbc");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.createInstance, "v4", originalRequestJSON);
	}
	@Test
	public void testV5PortMirrorCreateConfiguration() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v5PortMirrorCreateConfiguration.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.createInstance, "v5", originalRequestJSON);
	}
	@Test
	public void testV6PortMirrorCreateConfiguration() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v6PortMirrorCreateConfiguration.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.createInstance, "v6", originalRequestJSON);
	}
	@Test
	public void testV5EnablePortMirrorConfiguration() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v5EnablePortMirrorConfiguration.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.enablePort, "v5", originalRequestJSON);
	}
	@Test
	public void testV5DisablePortMirrorConfiguration() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v5EnablePortMirrorConfiguration.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.disablePort, "v5", originalRequestJSON);
	}
	@Test
	public void testV5ActivatePortMirrorConfiguration() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v5ActivatePortMirrorConfiguration.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.activateInstance, "v5", originalRequestJSON);
	}
	@Test
	public void testV5ActivatePortMirrorNoRelatedInstance() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v5ActivatePortMirrorNoRelatedInstance.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.activateInstance, "v5", originalRequestJSON);
	}
	@Test
	public void testV5DeactivatePortMirrorConfiguration() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v5DeactivatePortMirrorConfiguration.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.deactivateInstance, "v5", originalRequestJSON);
	}
	@Test
	public void testV5DeactivatePortMirrorNoRelatedInstance() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v5DeactivatePortMirrorNoRelatedInstance.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("1234");
		msoRequest.parse(sir, instanceIdMapTest, Action.deactivateInstance, "v5", originalRequestJSON);
	}
	@Test
	public void testV6AddRelationships() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v6AddRelationships.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("V6AddRelationships");
		msoRequest.parse(sir, instanceIdMapTest, Action.addRelationships, "v6", originalRequestJSON);
	}
	@Test
	public void testV6RemoveRelationships() throws JsonParseException, JsonMappingException, IOException, ValidationException{
		this.requestJSON = inputStream("/v6AddRelationships.json");
		this.instanceIdMapTest.put("serviceInstanceId", "ff305d54-75b4-431b-adb2-eb6b9e5ff000");
		this.sir  = mapper.readValue(requestJSON, ServiceInstancesRequest.class);
		this.msoRequest = new MsoRequest ("V6RemoveRelationships");
		msoRequest.parse(sir, instanceIdMapTest, Action.removeRelationships, "v6", originalRequestJSON);
		assertNotNull(msoRequest.getRequestId());
		assertEquals(msoRequest.getReqVersion(), 6);
	}
}
