/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.installer.heat;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.exception.LockAcquisitionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcCsarHelperImpl;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.elements.StatefulEntityType;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.client.test.emulators.ArtifactInfoImpl;
import org.onap.so.asdc.client.test.emulators.JsonStatusData;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.ServiceProxyResourceCustomization;
import org.onap.so.db.catalog.data.repository.AllottedResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.AllottedResourceRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class ToscaResourceInstallerTest extends BaseTest {
	@Autowired
	private ToscaResourceInstaller toscaInstaller;
	@Autowired
	private WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;
	@Autowired
	private AllottedResourceRepository allottedRepo;
	@Autowired
	private AllottedResourceCustomizationRepository allottedCustomizationRepo;
	@Autowired
	private ServiceRepository serviceRepo;
	@Mock
	private SdcCsarHelperImpl sdcCsarHelper;
	@Mock
	private Metadata metadata;
	@Mock
	private ArtifactInfoImpl artifactInfo;
	@Mock
	private List<Group> vfGroups;
	@Mock
	private IResourceInstance resourceInstance;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private NodeTemplate nodeTemplate;
	@Mock
	private ToscaResourceStructure toscaResourceStructure;
	@Mock 
	private ServiceProxyResourceCustomization spResourceCustomization;
	@Mock
	private ISdcCsarHelper csarHelper;
	@Mock
	private StatefulEntityType entityType;

	private NotificationDataImpl notificationData;
	private JsonStatusData statusData;
	private static final String MSO = "SO";

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		
		notificationData = new NotificationDataImpl();
		statusData = new JsonStatusData();
	}
	
	@Test
	public void isResourceAlreadyDeployedTest() throws Exception {
		notificationData.setServiceName("serviceName");
		notificationData.setServiceVersion("123456");
		notificationData.setServiceUUID("serviceUUID");
		notificationData.setDistributionID("testStatusSuccessTosca");
		
		WatchdogComponentDistributionStatus expectedComponentDistributionStatus = 
				new WatchdogComponentDistributionStatus(notificationData.getDistributionID(), MSO);
		expectedComponentDistributionStatus.setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());
		
		doReturn(true).when(vfResourceStructure).isDeployedSuccessfully();
		doReturn(notificationData).when(vfResourceStructure).getNotification();
		doReturn(resourceInstance).when(vfResourceStructure).getResourceInstance();
		doReturn("resourceInstanceName").when(resourceInstance).getResourceInstanceName();
		doReturn("resourceCustomizationUUID").when(resourceInstance).getResourceCustomizationUUID();
		doReturn("resourceName").when(resourceInstance).getResourceName();
		
		toscaInstaller.isResourceAlreadyDeployed(vfResourceStructure);
		
		WatchdogComponentDistributionStatus actualWatchdogComponentDistributionStatus = getWatchdogCDStatusWithName(watchdogCDStatusRepository.findByDistributionId(notificationData.getDistributionID()), MSO);
		
		verify(vfResourceStructure, times(3)).getResourceInstance();
		verify(vfResourceStructure, times(5)).getNotification();
		assertThat(actualWatchdogComponentDistributionStatus, sameBeanAs(expectedComponentDistributionStatus)
				.ignoring("createTime")
				.ignoring("modifyTime"));
	}
	
	@Test
	public void isResourceAlreadyDeployedFalseTest() throws Exception {
		notificationData.setServiceName("serviceName");
		notificationData.setServiceVersion("123456");
		notificationData.setServiceUUID("serviceUUID");
		notificationData.setDistributionID("testStatusSuccess");
		
		doThrow(RuntimeException.class).when(vfResourceStructure).isDeployedSuccessfully();
		doReturn(notificationData).when(vfResourceStructure).getNotification();
		doReturn(resourceInstance).when(vfResourceStructure).getResourceInstance();
		doReturn("resourceInstanceName").when(resourceInstance).getResourceInstanceName();
		doReturn("resourceCustomizationUUID").when(resourceInstance).getResourceCustomizationUUID();
		doReturn("resourceName").when(resourceInstance).getResourceName();
		
		toscaInstaller.isResourceAlreadyDeployed(vfResourceStructure);
		
		verify(vfResourceStructure, times(3)).getResourceInstance();
		verify(vfResourceStructure, times(4)).getNotification();
	}
	
	@Test
	public void isResourceAlreadyDeployedExceptionTest() throws ArtifactInstallerException {
		expectedException.expect(ArtifactInstallerException.class);
		
		toscaInstaller.isResourceAlreadyDeployed(vfResourceStructure);
	}
	
	@Test
	public void installTheComponentStatusTest() throws Exception {
		String distributionId = "testStatusSuccessTosca";
		String componentName = "testComponentName";
		
		statusData = spy(JsonStatusData.class);
		doReturn(distributionId).when(statusData).getDistributionID();
		doReturn(componentName).when(statusData).getComponentName();
		
		WatchdogComponentDistributionStatus expectedWatchdogComponentDistributionStatus = 
				new WatchdogComponentDistributionStatus(distributionId, componentName);
		expectedWatchdogComponentDistributionStatus.setComponentDistributionStatus(statusData.getStatus().toString());
		
		WatchdogComponentDistributionStatus cdStatus = new WatchdogComponentDistributionStatus(statusData.getDistributionID(),
				statusData.getComponentName());
		
		toscaInstaller.installTheComponentStatus(statusData);
		
		WatchdogComponentDistributionStatus actualWatchdogComponentDistributionStatus = getWatchdogCDStatusWithName(watchdogCDStatusRepository.findByDistributionId("testStatusSuccessTosca"), statusData.getComponentName());
		
		assertEquals(statusData.getDistributionID(), cdStatus.getDistributionId());
		assertEquals(statusData.getComponentName(), cdStatus.getComponentName());
		assertThat(actualWatchdogComponentDistributionStatus, sameBeanAs(expectedWatchdogComponentDistributionStatus)
				.ignoring("createTime")
				.ignoring("modifyTime"));
	}
	
	@Test
	public void installTheComponentStatusExceptionTest() throws ArtifactInstallerException {
		expectedException.expect(ArtifactInstallerException.class);
		
		statusData = spy(JsonStatusData.class);
		doReturn(null).when(statusData).getStatus();
		
		toscaInstaller.installTheComponentStatus(statusData);
	}
	
	@Test
	public void installTheResourceExceptionTest() throws Exception {
		expectedException.expect(ArtifactInstallerException.class);
		
		notificationData.setDistributionID("testStatusFailureTosca");
		notificationData.setServiceVersion("123456");
		notificationData.setServiceUUID("serviceUUID");
		notificationData.setWorkloadContext("workloadContext");
		
		doReturn(notificationData).when(vfResourceStructure).getNotification();
		doReturn(resourceInstance).when(vfResourceStructure).getResourceInstance();
		
		toscaInstaller.installTheResource(toscaResourceStruct, vfResourceStructure);
	}
	
	@Test
	public void installTheResourceDBExceptionTest() throws Exception {
		notificationData.setDistributionID("testStatusSuccessTosca");
		notificationData.setServiceVersion("123456");
		notificationData.setServiceUUID("serviceUUID");
		notificationData.setWorkloadContext("workloadContext");
		
		doReturn(notificationData).when(vfResourceStructure).getNotification();
		doReturn(resourceInstance).when(vfResourceStructure).getResourceInstance();
		doThrow(LockAcquisitionException.class).when(toscaResourceStruct).getToscaArtifact();
		
		toscaInstaller.installTheResource(toscaResourceStruct, vfResourceStructure);
	}
	
	@Test
	public void verifyTheFilePrefixInStringTest() {
		String body = "abcabcabcfile:///testfile.txtabcbabacbcabc";
		String filenameToVerify = "testfile.txt";
		String expectedFileBody = "abcabcabctestfile.txtabcbabacbcabc";
		
		String newFileBody = toscaInstaller.verifyTheFilePrefixInString(body, filenameToVerify);
		
		assertEquals(expectedFileBody, newFileBody);
	}
	
	@Test
	public void verifyTheFilePrefixInStringNullBodyTest() {
		String body = null;
		String filenameToVerify = "testfile.txt";
		
		String newFileBody = toscaInstaller.verifyTheFilePrefixInString(body, filenameToVerify);
		
		assertEquals(body, newFileBody);
	}
	
	@Test
	public void verifyTheFilePrefixInStringEmptyBodyTest() {
		String body = "";
		String filenameToVerify = "testfile.txt";
		
		String newFileBody = toscaInstaller.verifyTheFilePrefixInString(body, filenameToVerify);
		
		assertEquals(body, newFileBody);
	}
	
	@Test
	public void verifyTheFilePrefixInStringNullFilenameTest() {
		String body = "abcabcabcfile:///testfile.txtabcbabacbcabc";
		String filenameToVerify = null;
		
		String newFileBody = toscaInstaller.verifyTheFilePrefixInString(body, filenameToVerify);
		
		assertEquals(body, newFileBody);
	}
	
	@Test
	public void verifyTheFilePrefixInStringEmptyFilenameTest() {
		String body = "abcabcabcfile:///testfile.txtabcbabacbcabc";
		String filenameToVerify = "";
		
		String newFileBody = toscaInstaller.verifyTheFilePrefixInString(body, filenameToVerify);
		
		assertEquals(body, newFileBody);
	}
	
	private WatchdogComponentDistributionStatus getWatchdogCDStatusWithName(List<WatchdogComponentDistributionStatus> watchdogComponentDistributionStatuses, String componentName) {
		WatchdogComponentDistributionStatus actualWatchdogComponentDistributionStatus = new WatchdogComponentDistributionStatus();
		for(WatchdogComponentDistributionStatus watchdogComponentDistributionStatus : watchdogComponentDistributionStatuses) {
			if(componentName.equals(watchdogComponentDistributionStatus.getComponentName())) {
				actualWatchdogComponentDistributionStatus = watchdogComponentDistributionStatus;
				break;
			}
		}
		return actualWatchdogComponentDistributionStatus;
	}
	
	
	
	
	private void prepareConfigurationResource() {
		doReturn(metadata).when(nodeTemplate).getMetaData();
		doReturn(MockConstants.TEMPLATE_TYPE).when(nodeTemplate).getType();
		
		doReturn(MockConstants.MODEL_NAME).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_NAME);		
		doReturn(MockConstants.MODEL_INVARIANT_UUID).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID);		
		doReturn(MockConstants.MODEL_UUID).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_UUID);		
		doReturn(MockConstants.MODEL_VERSION).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_VERSION);
		doReturn(MockConstants.MODEL_DESCRIPTION).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION);
		doReturn(MockConstants.MODEL_NAME).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_NAME);
		doReturn(MockConstants.MODEL_NAME).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_NAME);
	}
	
	@Test
	public void getConfigurationResourceTest() {
		prepareConfigurationResource();
		
		ConfigurationResource configResource=toscaInstaller.getConfigurationResource(nodeTemplate);
		
		assertNotNull(configResource);
		assertEquals(MockConstants.MODEL_NAME, configResource.getModelName());
		assertEquals(MockConstants.MODEL_INVARIANT_UUID, configResource.getModelInvariantUUID());
		assertEquals(MockConstants.MODEL_UUID, configResource.getModelUUID());
		assertEquals(MockConstants.MODEL_VERSION, configResource.getModelVersion());
		assertEquals(MockConstants.MODEL_DESCRIPTION, configResource.getDescription());
		assertEquals(MockConstants.TEMPLATE_TYPE, nodeTemplate.getType());
	}
	
	private void prepareConfigurationResourceCustomization() {
		prepareConfigurationResource();
		doReturn(MockConstants.MODEL_CUSTOMIZATIONUUID).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
		doReturn(csarHelper).when(toscaResourceStructure).getSdcCsarHelper();
		doReturn(null).when(csarHelper).getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFFUNCTION);
		doReturn(null).when(csarHelper).getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFROLE);
		doReturn(null).when(csarHelper).getNodeTemplatePropertyLeafValue(nodeTemplate, SdcPropertyNames.PROPERTY_NAME_NFTYPE);
		doReturn(MockConstants.MODEL_CUSTOMIZATIONUUID).when(spResourceCustomization).getModelCustomizationUUID();
	}
	
	
	@Test
	public void getConfigurationResourceCustomizationTest() {
		prepareConfigurationResourceCustomization();		
		
		ConfigurationResourceCustomization configurationResourceCustomization = toscaInstaller.getConfigurationResourceCustomization(
				nodeTemplate, toscaResourceStructure, spResourceCustomization);
		assertNotNull(configurationResourceCustomization);
		assertNotNull(configurationResourceCustomization.getConfigurationResource());
		assertEquals(MockConstants.MODEL_CUSTOMIZATIONUUID, configurationResourceCustomization.getServiceProxyResourceCustomizationUUID());
	}
	
	@Test
	public void getVnrNodeTemplateTest() {
		prepareConfigurationResourceCustomization();
		List<NodeTemplate> nodeTemplateList = new ArrayList<>();
		doReturn(ToscaResourceInstaller.VLAN_NETWORK_RECEPTOR).when(entityType).getType();
		doReturn(entityType).when(nodeTemplate).getTypeDefinition();
		nodeTemplateList.add(nodeTemplate);
		Optional<ConfigurationResourceCustomization> vnrResourceCustomization= 
				toscaInstaller.getVnrNodeTemplate(nodeTemplateList, toscaResourceStructure, spResourceCustomization);
		assertTrue(vnrResourceCustomization.isPresent());
		assertEquals(ToscaResourceInstaller.VLAN_NETWORK_RECEPTOR, entityType.getType());
	}
	
	class MockConstants{
		public final static String MODEL_NAME = "VLAN Network Receptor Configuration";
		public final static String MODEL_INVARIANT_UUID = "1608eef4-de53-4334-a8d2-ba79cab4bde0";
		public final static String MODEL_UUID = "212ca27b-554c-474c-96b9-ddc2f1b1ddba";
		public final static String MODEL_VERSION = "30.0";
		public final static String MODEL_DESCRIPTION = "VLAN network receptor configuration object";
		public final static String MODEL_CUSTOMIZATIONUUID = "2db953e8-679d-437b-bff7-cb262638a8cd";
		public final static String TEMPLATE_TYPE = "org.openecomp.nodes.VLANNetworkReceptor";
		public final static String TEMPLATE_NAME = "VLAN Network Receptor Configuration 0";
		
	}
}
