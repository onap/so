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

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcCsarHelperImpl;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.RequirementAssignments;
import org.onap.sdc.toscaparser.api.SubstitutionMappings;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.elements.StatefulEntityType;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.BaseTest;
import org.onap.so.asdc.client.ResourceInstance;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.client.test.emulators.ArtifactInfoImpl;
import org.onap.so.asdc.client.test.emulators.JsonStatusData;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.asdc.installer.IVfModuleData;
import org.onap.so.asdc.installer.ResourceStructure;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfModuleStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.bpmn.WorkflowResource;
import org.onap.so.db.catalog.beans.*;
import org.onap.so.db.catalog.data.repository.*;
import org.onap.so.db.request.beans.WatchdogComponentDistributionStatus;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.stream.Collectors;

public class ToscaResourceInstallerTest extends BaseTest {
    @Autowired
    private ToscaResourceInstaller toscaInstaller;
    @Autowired
    private WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;
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
    private IEntityDetails entityDetails;
    @Mock
    private ToscaResourceStructure toscaResourceStructure;
    @Mock
    private VfResourceStructure vfResourceStruct;
    @Mock
    private ServiceProxyResourceCustomization spResourceCustomization;
    @Mock
    private ISdcCsarHelper csarHelper;
    @Mock
    private StatefulEntityType entityType;
    @Mock
    private Service service;
    @Mock
    Property property;

    private NotificationDataImpl notificationData;
    private JsonStatusData statusData;
    private static final String MSO = "SO";

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);

        notificationData = new NotificationDataImpl();
        statusData = new JsonStatusData();
    }

    @Test
    public void isCsarAlreadyDeployedTest() throws ArtifactInstallerException {
        IArtifactInfo inputCsar = mock(IArtifactInfo.class);
        String artifactUuid = "0122c05e-e13a-4c63-b5d2-475ccf13aa74";
        String checkSum = "MGUzNjJjMzk3OTBkYzExYzQ0MDg2ZDc2M2E2ZjZiZmY=";

        doReturn(checkSum).when(inputCsar).getArtifactChecksum();
        doReturn(artifactUuid).when(inputCsar).getArtifactUUID();

        doReturn(inputCsar).when(toscaResourceStructure).getToscaArtifact();

        ToscaCsar toscaCsar = mock(ToscaCsar.class);

        Optional<ToscaCsar> returnValue = Optional.of(toscaCsar);

        ToscaCsarRepository toscaCsarRepo = spy(ToscaCsarRepository.class);


        doReturn(artifactUuid).when(toscaCsar).getArtifactUUID();
        doReturn(checkSum).when(toscaCsar).getArtifactChecksum();
        doReturn(returnValue).when(toscaCsarRepo).findById(artifactUuid);

        ReflectionTestUtils.setField(toscaInstaller, "toscaCsarRepo", toscaCsarRepo);

        boolean isCsarDeployed = toscaInstaller.isCsarAlreadyDeployed(toscaResourceStructure);
        assertTrue(isCsarDeployed);
        verify(toscaCsarRepo, times(1)).findById(artifactUuid);
        verify(toscaResourceStructure, times(1)).getToscaArtifact();
        verify(toscaCsar, times(2)).getArtifactChecksum();
    }

    @Test
    public void isResourceAlreadyDeployedTest() throws Exception {
        notificationData.setServiceName("serviceName");
        notificationData.setServiceVersion("123456");
        notificationData.setServiceUUID("serviceUUID");
        notificationData.setDistributionID("testStatusSuccessTosca");

        WatchdogComponentDistributionStatus expectedComponentDistributionStatus =
                new WatchdogComponentDistributionStatus(notificationData.getDistributionID(), MSO);
        expectedComponentDistributionStatus
                .setComponentDistributionStatus(DistributionStatusEnum.COMPONENT_DONE_OK.name());

        doReturn(true).when(vfResourceStructure).isDeployedSuccessfully();
        doReturn(notificationData).when(vfResourceStructure).getNotification();
        doReturn(resourceInstance).when(vfResourceStructure).getResourceInstance();
        doReturn("resourceInstanceName").when(resourceInstance).getResourceInstanceName();
        doReturn("resourceCustomizationUUID").when(resourceInstance).getResourceCustomizationUUID();
        doReturn("resourceName").when(resourceInstance).getResourceName();

        toscaInstaller.isResourceAlreadyDeployed(vfResourceStructure, false);

        WatchdogComponentDistributionStatus actualWatchdogComponentDistributionStatus = getWatchdogCDStatusWithName(
                watchdogCDStatusRepository.findByDistributionId(notificationData.getDistributionID()), MSO);

        verify(vfResourceStructure, times(3)).getResourceInstance();
        verify(vfResourceStructure, times(5)).getNotification();
        assertThat(actualWatchdogComponentDistributionStatus,
                sameBeanAs(expectedComponentDistributionStatus).ignoring("createTime").ignoring("modifyTime"));
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

        toscaInstaller.isResourceAlreadyDeployed(vfResourceStructure, false);

        verify(vfResourceStructure, times(3)).getResourceInstance();
        verify(vfResourceStructure, times(4)).getNotification();
    }

    @Test
    public void isResourceAlreadyDeployedExceptionTest() throws ArtifactInstallerException {
        expectedException.expect(ArtifactInstallerException.class);

        toscaInstaller.isResourceAlreadyDeployed(vfResourceStructure, false);
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

        WatchdogComponentDistributionStatus cdStatus =
                new WatchdogComponentDistributionStatus(statusData.getDistributionID(), statusData.getComponentName());

        toscaInstaller.installTheComponentStatus(statusData);

        WatchdogComponentDistributionStatus actualWatchdogComponentDistributionStatus =
                getWatchdogCDStatusWithName(watchdogCDStatusRepository.findByDistributionId("testStatusSuccessTosca"),
                        statusData.getComponentName());

        assertEquals(statusData.getDistributionID(), cdStatus.getDistributionId());
        assertEquals(statusData.getComponentName(), cdStatus.getComponentName());
        assertThat(actualWatchdogComponentDistributionStatus,
                sameBeanAs(expectedWatchdogComponentDistributionStatus).ignoring("createTime").ignoring("modifyTime"));
    }

    @Test
    public void installTheComponentStatusExceptionTest() throws ArtifactInstallerException {
        expectedException.expect(ArtifactInstallerException.class);

        statusData = spy(JsonStatusData.class);
        doReturn(null).when(statusData).getStatus();

        toscaInstaller.installTheComponentStatus(statusData);
    }

    @Test
    public void installTheResourceWithGroupAndVFModulesTest() throws Exception {
        ToscaResourceInstaller toscaInstaller = new ToscaResourceInstaller();
        ToscaResourceStructure toscaResourceStructObj = prepareToscaResourceStructure(true, toscaInstaller);

        toscaInstaller.installTheResource(toscaResourceStructObj, vfResourceStruct);
        assertEquals(true, toscaResourceStructObj.isDeployedSuccessfully());
    }

    @Test
    public void installTheResourceGroupWithoutVFModulesTest() throws Exception {
        ToscaResourceInstaller toscaInstaller = new ToscaResourceInstaller();
        ToscaResourceStructure toscaResourceStructObj = prepareToscaResourceStructure(false, toscaInstaller);

        toscaInstaller.installTheResource(toscaResourceStructObj, vfResourceStruct);
        assertEquals(true, toscaResourceStructObj.isDeployedSuccessfully());
    }

    private ToscaResourceStructure prepareToscaResourceStructure(boolean prepareVFModuleStructures,
            ToscaResourceInstaller toscaInstaller) throws ArtifactInstallerException {

        Metadata metadata = mock(Metadata.class);
        IResourceInstance resourceInstance = mock(ResourceInstance.class);
        NodeTemplate nodeTemplate = mock(NodeTemplate.class);
        ISdcCsarHelper csarHelper = mock(SdcCsarHelperImpl.class);

        IArtifactInfo inputCsar = mock(IArtifactInfo.class);
        String artifactUuid = "0122c05e-e13a-4c63-b5d2-475ccf23aa74";
        String checkSum = "MGUzNjJjMzk3OTBkYzExYzQ0MDg2ZDc2M2E3ZjZiZmY=";

        doReturn(checkSum).when(inputCsar).getArtifactChecksum();
        doReturn(artifactUuid).when(inputCsar).getArtifactUUID();
        doReturn("1.0").when(inputCsar).getArtifactVersion();
        doReturn("TestCsarWithGroupAndVFModule").when(inputCsar).getArtifactName();
        doReturn("Test Csar data with Group and VF module inputs").when(inputCsar).getArtifactDescription();
        doReturn("http://localhost/dummy/url/test.csar").when(inputCsar).getArtifactURL();

        ToscaResourceStructure toscaResourceStructObj = new ToscaResourceStructure();
        toscaResourceStructObj.setToscaArtifact(inputCsar);

        ToscaCsarRepository toscaCsarRepo = spy(ToscaCsarRepository.class);


        ToscaCsar toscaCsar = mock(ToscaCsar.class);
        Optional<ToscaCsar> returnValue = Optional.of(toscaCsar);
        doReturn(artifactUuid).when(toscaCsar).getArtifactUUID();
        doReturn(checkSum).when(toscaCsar).getArtifactChecksum();
        doReturn(returnValue).when(toscaCsarRepo).findById(artifactUuid);

        ReflectionTestUtils.setField(toscaInstaller, "toscaCsarRepo", toscaCsarRepo);

        NotificationDataImpl notificationData = new NotificationDataImpl();
        notificationData.setDistributionID("testStatusSuccessfulTosca");
        notificationData.setServiceVersion("1234567");
        notificationData.setServiceUUID("serviceUUID1");
        notificationData.setWorkloadContext("workloadContext1");

        String serviceType = "test-type1";
        String serviceRole = "test-role1";
        String category = "Network L3+";
        String description = "Customer Orderable service description";
        String name = "Customer_Orderable_Service";
        String uuid = "72db5868-4575-4804-b546-0b0d3c3b5ac6";
        String invariantUUID = "6f30bbe3-4590-4185-a7e0-4f9610926c6f";
        String namingPolicy = "naming Policy1";
        String ecompGeneratedNaming = "true";
        String environmentContext = "General_Revenue-Bearing1";
        String resourceCustomizationUUID = "0177ba22-5547-4e4e-bcf8-178f7f71de3a";

        doReturn(serviceType).when(metadata).getValue("serviceType");
        doReturn(serviceRole).when(metadata).getValue("serviceRole");

        doReturn(category).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);
        doReturn(description).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION);
        doReturn("1.0").when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_VERSION);
        doReturn(name).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_NAME);

        doReturn(uuid).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_UUID);

        doReturn(environmentContext).when(metadata).getValue(metadata.getValue("environmentContext"));
        doReturn(invariantUUID).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID);
        doReturn(namingPolicy).when(metadata).getValue("namingPolicy");
        doReturn(ecompGeneratedNaming).when(metadata).getValue("ecompGeneratedNaming");
        doReturn(resourceCustomizationUUID).when(metadata).getValue("vfModuleModelCustomizationUUID");

        ServiceRepository serviceRepo = spy(ServiceRepository.class);

        VnfResourceRepository vnfRepo = spy(VnfResourceRepository.class);
        doReturn(null).when(vnfRepo).findResourceByModelUUID(uuid);

        VFModuleRepository vfModuleRepo = spy(VFModuleRepository.class);
        InstanceGroupRepository instanceGroupRepo = spy(InstanceGroupRepository.class);

        WorkflowResource workflowResource = spy(WorkflowResource.class);

        ReflectionTestUtils.setField(toscaInstaller, "serviceRepo", serviceRepo);
        ReflectionTestUtils.setField(toscaInstaller, "vnfRepo", vnfRepo);
        ReflectionTestUtils.setField(toscaInstaller, "vfModuleRepo", vfModuleRepo);
        ReflectionTestUtils.setField(toscaInstaller, "instanceGroupRepo", instanceGroupRepo);
        ReflectionTestUtils.setField(toscaInstaller, "workflowResource", workflowResource);

        // doReturn(csarHelper).when(toscaResourceStructure).getSdcCsarHelper();
        toscaResourceStructObj.setSdcCsarHelper(csarHelper);

        doReturn(resourceCustomizationUUID).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
        doReturn(uuid).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_VFMODULEMODELUUID);


        // vnfc instance group list
        List<Group> vnfcInstanceGroupList = new ArrayList<>();
        Group vnfcG1 = mock(Group.class);
        Map<String, Object> metaProperties = new HashMap<>();
        metaProperties.put(SdcPropertyNames.PROPERTY_NAME_UUID, "vnfc_group1_uuid");
        metaProperties.put(SdcPropertyNames.PROPERTY_NAME_NAME, "vnfc_group1_uuid");
        metaProperties.put(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID, "vnfc_group1_invariantid");
        metaProperties.put(SdcPropertyNames.PROPERTY_NAME_VERSION, "1.0");
        Metadata vnfcmetadata = new Metadata(metaProperties);

        doReturn(vnfcmetadata).when(vnfcG1).getMetadata();
        ArrayList<NodeTemplate> memberList = new ArrayList();
        doReturn(memberList).when(vnfcG1).getMemberNodes();
        vnfcInstanceGroupList.add(vnfcG1);
        SubstitutionMappings submappings = mock(SubstitutionMappings.class);
        doReturn(new ArrayList<Input>()).when(submappings).getInputs();
        doReturn(submappings).when(nodeTemplate).getSubMappingToscaTemplate();

        doReturn(notificationData).when(vfResourceStruct).getNotification();
        doReturn(resourceInstance).when(vfResourceStruct).getResourceInstance();

        if (prepareVFModuleStructures) {

            // VfModule list
            List<Group> vfModuleGroups = new ArrayList<>();
            Group g1 = mock(Group.class);
            doReturn(metadata).when(g1).getMetadata();
            vfModuleGroups.add(g1);

            doReturn(metadata).when(nodeTemplate).getMetaData();
            List<NodeTemplate> nodeList = new ArrayList<>();
            nodeList.add(nodeTemplate);

            IVfModuleData moduleMetadata = mock(IVfModuleData.class);
            doReturn(name).when(moduleMetadata).getVfModuleModelName();
            doReturn(invariantUUID).when(moduleMetadata).getVfModuleModelInvariantUUID();
            doReturn(Collections.<String>emptyList()).when(moduleMetadata).getArtifacts();
            doReturn(resourceCustomizationUUID).when(moduleMetadata).getVfModuleModelCustomizationUUID();
            doReturn(uuid).when(moduleMetadata).getVfModuleModelUUID();
            doReturn("1.0").when(moduleMetadata).getVfModuleModelVersion();

            VfModuleStructure moduleStructure = new VfModuleStructure(vfResourceStruct, moduleMetadata);

            List<VfModuleStructure> moduleStructures = new ArrayList<>();
            moduleStructures.add(moduleStructure);
            doReturn(moduleStructures).when(vfResourceStruct).getVfModuleStructure();
        }

        toscaResourceStructObj.setServiceMetadata(metadata);
        doReturn("resourceInstanceName1").when(resourceInstance).getResourceInstanceName();
        doReturn(resourceCustomizationUUID).when(resourceInstance).getResourceCustomizationUUID();
        doReturn("resourceName1").when(resourceInstance).getResourceName();

        Service service = toscaInstaller.createService(toscaResourceStructObj, vfResourceStruct);

        assertNotNull(service);
        service.setModelVersion("1.0");

        doReturn(service).when(serviceRepo).save(service);

        WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository =
                spy(WatchdogComponentDistributionStatusRepository.class);
        ReflectionTestUtils.setField(toscaInstaller, "watchdogCDStatusRepository", watchdogCDStatusRepository);
        doReturn(null).when(watchdogCDStatusRepository).save(any(WatchdogComponentDistributionStatus.class));

        VnfcInstanceGroupCustomizationRepository vnfcInstanceGroupCustomizationRepo =
                spy(VnfcInstanceGroupCustomizationRepository.class);
        ReflectionTestUtils.setField(toscaInstaller, "vnfcInstanceGroupCustomizationRepo",
                vnfcInstanceGroupCustomizationRepo);
        doReturn(null).when(vnfcInstanceGroupCustomizationRepo).save(any(VnfcInstanceGroupCustomization.class));
        return toscaResourceStructObj;
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

        assertDoesNotThrow(() -> toscaInstaller.installTheResource(toscaResourceStruct, vfResourceStructure));
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

    private WatchdogComponentDistributionStatus getWatchdogCDStatusWithName(
            List<WatchdogComponentDistributionStatus> watchdogComponentDistributionStatuses, String componentName) {
        WatchdogComponentDistributionStatus actualWatchdogComponentDistributionStatus =
                new WatchdogComponentDistributionStatus();
        for (WatchdogComponentDistributionStatus watchdogComponentDistributionStatus : watchdogComponentDistributionStatuses) {
            if (componentName.equals(watchdogComponentDistributionStatus.getComponentName())) {
                actualWatchdogComponentDistributionStatus = watchdogComponentDistributionStatus;
                break;
            }
        }
        return actualWatchdogComponentDistributionStatus;
    }

    @Test
    public void createServiceTest() {
        ToscaResourceStructure toscaResourceStructure = mock(ToscaResourceStructure.class);
        ResourceStructure resourceStructure = mock(ResourceStructure.class);
        Metadata metadata = mock(Metadata.class);
        INotificationData notification = mock(INotificationData.class);

        doReturn("e2899e5c-ae35-434c-bada-0fabb7c1b44d").when(toscaResourceStructure).getServiceVersion();
        doReturn(metadata).when(toscaResourceStructure).getServiceMetadata();
        doReturn("production").when(notification).getWorkloadContext();
        doReturn(notification).when(resourceStructure).getNotification();

        String serviceType = "test-type";
        String serviceRole = "test-role";
        String category = "Network L4+";
        String description = "Customer Orderable service description";
        String name = "Customer Orderable Service";
        String uuid = "72db5868-4575-4804-b546-0b0d3c3b5ac6";
        String invariantUUID = "6f30bbe3-4590-4185-a7e0-4f9610926c6f";
        String namingPolicy = "naming Policy";
        String ecompGeneratedNaming = "true";
        String environmentContext = "General_Revenue-Bearing";

        doReturn(serviceType).when(metadata).getValue("serviceType");
        doReturn(serviceRole).when(metadata).getValue("serviceRole");

        doReturn(category).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_CATEGORY);
        doReturn(description).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION);

        doReturn(name).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_NAME);

        doReturn(uuid).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_UUID);

        doReturn(environmentContext).when(metadata).getValue(metadata.getValue("environmentContext"));
        doReturn(invariantUUID).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID);
        doReturn(namingPolicy).when(metadata).getValue("namingPolicy");
        doReturn(ecompGeneratedNaming).when(metadata).getValue("ecompGeneratedNaming");

        ISdcCsarHelper iSdcCsarHelper = mock(ISdcCsarHelper.class);
        List<Input> serviceInputs = new ArrayList<Input>();

        LinkedHashMap<String, Object> value = new LinkedHashMap<String, Object>();
        value.put("controller_actor", "SO-REF-DATA");
        value.put("type", "string");
        Input input = new Input("controller_actor", value, null);
        serviceInputs.add(0, input);

        value = new LinkedHashMap<String, Object>();
        value.put("cds_model_version", "v1.4.0");
        value.put("type", "string");
        input = new Input("cds_model_version", value, null);
        serviceInputs.add(1, input);

        value = new LinkedHashMap<String, Object>();
        value.put("cds_model_name", "Blueprint140");
        value.put("type", "string");
        input = new Input("cds_model_name", value, null);
        serviceInputs.add(2, input);

        value = new LinkedHashMap<String, Object>();
        value.put("skip_post_instantiation_configuration", "false");
        value.put("type", "boolean");
        input = new Input("skip_post_instantiation_configuration", value, null);
        serviceInputs.add(3, input);

        doReturn(iSdcCsarHelper).when(toscaResourceStructure).getSdcCsarHelper();
        doReturn(serviceInputs).when(iSdcCsarHelper).getServiceInputs();

        Service service = toscaInstaller.createService(toscaResourceStructure, resourceStructure);

        assertNotNull(service);

        verify(toscaResourceStructure, times(2)).getServiceVersion();
        assertNotNull(service.getNamingPolicy());
        assertEquals(serviceType, service.getServiceType());
        assertEquals(serviceRole, service.getServiceRole());
        assertEquals(category, service.getCategory());
        assertEquals(description, service.getDescription());
        assertEquals(uuid, service.getModelUUID());
        assertEquals(invariantUUID, service.getModelInvariantUUID());
        assertEquals(namingPolicy, service.getNamingPolicy());
        assertTrue(service.getOnapGeneratedNaming());
    }

    private void prepareConfigurationResource() {
        doReturn(metadata).when(entityDetails).getMetadata();
        doReturn(MockConstants.TEMPLATE_TYPE).when(entityDetails).getToscaType();

        doReturn(MockConstants.MODEL_NAME).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_NAME);
        doReturn(MockConstants.MODEL_INVARIANT_UUID).when(metadata)
                .getValue(SdcPropertyNames.PROPERTY_NAME_INVARIANTUUID);
        doReturn(MockConstants.MODEL_UUID).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_UUID);
        doReturn(MockConstants.MODEL_VERSION).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_VERSION);
        doReturn(MockConstants.MODEL_DESCRIPTION).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_DESCRIPTION);
        doReturn(MockConstants.MODEL_NAME).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_NAME);
        doReturn(MockConstants.MODEL_NAME).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_NAME);
    }

    @Test
    public void getConfigurationResourceTest() {
        prepareConfigurationResource();

        ConfigurationResource configResource = toscaInstaller.getConfigurationResource(entityDetails);

        assertNotNull(configResource);
        assertEquals(MockConstants.MODEL_NAME, configResource.getModelName());
        assertEquals(MockConstants.MODEL_INVARIANT_UUID, configResource.getModelInvariantUUID());
        assertEquals(MockConstants.MODEL_UUID, configResource.getModelUUID());
        assertEquals(MockConstants.MODEL_VERSION, configResource.getModelVersion());
        assertEquals(MockConstants.MODEL_DESCRIPTION, configResource.getDescription());
        assertEquals(MockConstants.TEMPLATE_TYPE, entityDetails.getToscaType());
    }

    private void prepareConfigurationResourceCustomization() {
        prepareConfigurationResource();
        doReturn(MockConstants.MODEL_CUSTOMIZATIONUUID).when(metadata)
                .getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
        doReturn(csarHelper).when(toscaResourceStructure).getSdcCsarHelper();
        doReturn(MockConstants.MODEL_CUSTOMIZATIONUUID).when(spResourceCustomization).getModelCustomizationUUID();
        doReturn(null).when(toscaInstaller).getLeafPropertyValue(entityDetails,
                SdcPropertyNames.PROPERTY_NAME_NFFUNCTION);
        doReturn(null).when(toscaInstaller).getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFROLE);
        doReturn(null).when(toscaInstaller).getLeafPropertyValue(entityDetails, SdcPropertyNames.PROPERTY_NAME_NFTYPE);
    }


    @Test
    public void getConfigurationResourceCustomizationTest() {
        prepareConfigurationResourceCustomization();

        ConfigurationResourceCustomization configurationResourceCustomization =
                toscaInstaller.getConfigurationResourceCustomization(entityDetails, toscaResourceStructure,
                        spResourceCustomization, service);
        assertNotNull(configurationResourceCustomization);
        assertNotNull(configurationResourceCustomization.getConfigurationResource());
        assertEquals(MockConstants.MODEL_CUSTOMIZATIONUUID,
                configurationResourceCustomization.getServiceProxyResourceCustomization().getModelCustomizationUUID());
    }

    @Test
    public void correlateConfigCustomResourcesTest() {
        ConfigurationResource vrfConfigResource = mock(ConfigurationResource.class);
        ConfigurationResourceCustomization vrfConfigCustom = mock(ConfigurationResourceCustomization.class);
        doReturn(ToscaResourceInstaller.NODES_VRF_ENTRY).when(vrfConfigResource).getToscaNodeType();
        doReturn(vrfConfigResource).when(vrfConfigCustom).getConfigurationResource();

        ConfigurationResource vnrConfigResource = mock(ConfigurationResource.class);
        ConfigurationResourceCustomization vnrConfigCustom = mock(ConfigurationResourceCustomization.class);
        doReturn(ToscaResourceInstaller.VLAN_NETWORK_RECEPTOR).when(vnrConfigResource).getToscaNodeType();
        doReturn(vnrConfigResource).when(vnrConfigCustom).getConfigurationResource();

        ConfigurationResourceCustomizationRepository configCustomizationRepo =
                spy(ConfigurationResourceCustomizationRepository.class);
        ReflectionTestUtils.setField(toscaInstaller, "configCustomizationRepo", configCustomizationRepo);
        doReturn(vrfConfigCustom).when(configCustomizationRepo).save(vrfConfigCustom);
        doReturn(vnrConfigCustom).when(configCustomizationRepo).save(vnrConfigCustom);

        List<ConfigurationResourceCustomization> configList = new ArrayList<>();
        configList.add(vrfConfigCustom);
        configList.add(vnrConfigCustom);
        doReturn(configList).when(service).getConfigurationCustomizations();

        toscaInstaller.correlateConfigCustomResources(service);
        verify(vrfConfigCustom, times(1)).getConfigurationResource();
        verify(vrfConfigCustom, times(1)).setConfigResourceCustomization(vnrConfigCustom);
        verify(service, times(1)).getConfigurationCustomizations();
        verify(vnrConfigCustom, times(1)).getConfigurationResource();
        verify(vnrConfigCustom, times(1)).setConfigResourceCustomization(vrfConfigCustom);
    }

    @Test
    public void testProcessVNFCGroupSequence() {
        List<Group> groupList = new ArrayList<>();

        Group group1 = mock(Group.class);
        NodeTemplate node1 = mock(NodeTemplate.class);
        List<NodeTemplate> nodeList1 = new ArrayList<>();
        nodeList1.add(node1);
        doReturn("VfcInstanceGroup..0").when(group1).getName();
        doReturn(nodeList1).when(group1).getMemberNodes();
        doReturn("deviceV3").when(node1).getName();

        Group group2 = mock(Group.class);
        NodeTemplate node2 = mock(NodeTemplate.class);
        List<NodeTemplate> nodeList2 = new ArrayList<>();
        nodeList2.add(node2);
        doReturn("VfcInstanceGroup..1").when(group2).getName();
        doReturn(nodeList2).when(group2).getMemberNodes();
        RequirementAssignments requirements2 = mock(RequirementAssignments.class);
        RequirementAssignment requirement2 = mock(RequirementAssignment.class);
        List<RequirementAssignment> requirementCollection2 = new ArrayList<>();
        requirementCollection2.add(requirement2);
        doReturn(requirementCollection2).when(requirements2).getAll();
        doReturn("deviceV3").when(requirement2).getNodeTemplateName();
        doReturn("SiteV2").when(node2).getName();

        Group group3 = mock(Group.class);
        NodeTemplate node3 = mock(NodeTemplate.class);
        List<NodeTemplate> nodeList3 = new ArrayList<>();
        nodeList3.add(node3);
        doReturn("VfcInstanceGroup..2").when(group3).getName();
        doReturn(nodeList3).when(group3).getMemberNodes();
        RequirementAssignments requirements3 = mock(RequirementAssignments.class);
        RequirementAssignment requirement3 = mock(RequirementAssignment.class);
        List<RequirementAssignment> requirementCollection3 = new ArrayList<>();
        requirementCollection3.add(requirement3);
        doReturn(requirementCollection3).when(requirements3).getAll();
        doReturn("SiteV2").when(requirement3).getNodeTemplateName();
        doReturn("siteWanV2").when(node3).getName();

        groupList.add(group1);
        groupList.add(group2);
        groupList.add(group3);

        doReturn(csarHelper).when(toscaResourceStructure).getSdcCsarHelper();

        ToscaResourceInstaller installer = new ToscaResourceInstaller();
        Method[] methods = installer.getClass().getDeclaredMethods();
        Method testMethod = null;
        for (Method method : methods) {
            String name = method.getName();
            if (name.equals("processVNFCGroupSequence")) {
                method.setAccessible(true);
                testMethod = method;
            }
        }

        if (null != testMethod) {
            try {
                Object seqResult = testMethod.invoke(installer, toscaResourceStructure, groupList);
                if (seqResult instanceof List) {
                    String resultStr = ((List<String>) seqResult).stream().collect(Collectors.joining(","));
                    assertEquals(((List<String>) seqResult).size(), 3);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }


    class MockConstants {
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
