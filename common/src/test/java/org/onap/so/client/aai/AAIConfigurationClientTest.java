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

package org.onap.so.client.aai;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.client.aai.entities.Configuration;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RequestDetails;
@RunWith(MockitoJUnitRunner.class) 
public class AAIConfigurationClientTest {

	@Mock
	AAIResourcesClient aaiClient;
	
	@InjectMocks
	AAIConfigurationClient aaiConfigurationClient = new AAIConfigurationClient();

	@Test
	public void verifyCreate() {		
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelInvariantId("testInvariantID");
		modelInfo.setModelVersionId("testVersionID");
		modelInfo.setModelCustomizationId("testCustomizationID");
		
		RequestDetails requestDetails = new RequestDetails();
		requestDetails.setModelInfo(modelInfo);
		
		String configurationId = UUID.randomUUID().toString();
		String configurationType = "test";
		String configurationSubType = "test";

		// Test Create Configuration
		doNothing().when(aaiClient).create(isA(AAIResourceUri.class), isA(Object.class));
		aaiConfigurationClient.createConfiguration(requestDetails, configurationId, configurationType, configurationSubType);	
		verify(aaiClient, times(1)).create(isA(AAIResourceUri.class), isA(Object.class));
	}
	
	@Test
	public void verifyConfigurePayload() {		
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelInvariantId("testInvariantID");
		modelInfo.setModelVersionId("testVersionID");
		modelInfo.setModelCustomizationId("testCustomizationID");
		
		RequestDetails requestDetails = new RequestDetails();
		requestDetails.setModelInfo(modelInfo);
		
		String configurationId = UUID.randomUUID().toString();
		String configurationType = "test";
		String configurationSubType = "test";
		AAIResourceUri uri = aaiConfigurationClient.getConfigurationURI(configurationId);
		
		Configuration payload = aaiConfigurationClient.configurePayload(requestDetails, configurationId, configurationType, configurationSubType);	
		
		assertEquals(configurationId, payload.getConfigurationId());
		assertEquals(configurationType, payload.getConfigurationType());
		assertEquals(configurationSubType, payload.getConfigurationSubType());
		assertEquals(uri.build().getPath(), payload.getConfigurationSelflink());
		assertEquals("PreCreated", payload.getOrchestrationStatus());
		assertEquals("", payload.getOperationalStatus());		
		assertEquals(modelInfo.getModelVersionId(), payload.getModelVersionId());
		assertEquals(modelInfo.getModelInvariantId(), payload.getModelInvariantId());
		assertEquals(modelInfo.getModelCustomizationId(), payload.getModelCustomizationId());
	}
	
	@Test
	public void testDeleteConfiguration() {
		String uuid = UUID.randomUUID().toString();
		doNothing().when(aaiClient).delete(isA(AAIResourceUri.class));
		aaiConfigurationClient.deleteConfiguration(uuid);
		verify(aaiClient, times(1)).delete(aaiConfigurationClient.getConfigurationURI(uuid));		
	}

	@Test
	public void testUpdateOrchestrationStatus() {
		String uuid = UUID.randomUUID().toString();
		doNothing().when(aaiClient).update(isA(AAIResourceUri.class), isA(Object.class));
		aaiConfigurationClient.updateOrchestrationStatus(uuid, "testPayload");
		verify(aaiClient, times(1)).update(aaiConfigurationClient.getConfigurationURI(uuid), "testPayload");
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetConfiguration() {
		String uuid = UUID.randomUUID().toString();
		Optional<Configuration> expectedConfiguration = Optional.of(new Configuration());
		expectedConfiguration.get().setConfigurationId(uuid);
		
		doReturn(expectedConfiguration).when(aaiClient).get(isA(Class.class), isA(AAIResourceUri.class));
		Configuration actualConfiguration = aaiConfigurationClient.getConfiguration(uuid);
		verify(aaiClient, times(1)).get(Configuration.class, aaiConfigurationClient.getConfigurationURI(uuid));
		assertEquals(expectedConfiguration.get(), actualConfiguration);
	}

	@Test
	public void testConfigurationExists() {
		String uuid = UUID.randomUUID().toString();
		AAIResourceUri uri = aaiConfigurationClient.getConfigurationURI(uuid);
		boolean expectedResult;
		boolean actualResult;
		
		expectedResult = true;
		doReturn(expectedResult).when(aaiClient).exists(isA(AAIResourceUri.class));
		actualResult = aaiConfigurationClient.configurationExists(uuid);
		assertEquals(expectedResult, actualResult);
		
		expectedResult = false;
		doReturn(expectedResult).when(aaiClient).exists(isA(AAIResourceUri.class));
		actualResult = aaiConfigurationClient.configurationExists(uuid);
		assertEquals(expectedResult, actualResult);
		
		verify(aaiClient, times(2)).exists(uri);
	}

	@Test
	public void testGetConfigurationURI() {
		String uuid = UUID.randomUUID().toString();
		AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectType.CONFIGURATION, uuid);
		assertEquals(uri, aaiConfigurationClient.getConfigurationURI(uuid));
	}
}
