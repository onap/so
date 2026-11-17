/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.CapabilityAssignment;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.NodeTemplate;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.RequirementAssignments;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VnfResource;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.data.repository.CvnfcCustomizationRepository;
import org.onap.so.db.catalog.data.repository.InstanceGroupRepository;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.VFModuleRepository;
import org.onap.so.db.catalog.data.repository.VnfResourceRepository;
import org.onap.so.db.catalog.data.repository.VnfcCustomizationRepository;
import org.onap.so.db.catalog.data.repository.VnfcInstanceGroupCustomizationRepository;

public class VnfResourceInstallerTest {

    private static final String CUSTOMIZATION_UUID = "vnfc-customization-uuid";

    @Mock
    private ToscaResourceStructure toscaResourceStructure;
    @Mock
    private ISdcCsarHelper csarHelper;

    private VnfResourceRepository vnfRepo;
    private ServiceRepository serviceRepo;
    private VnfcCustomizationRepository vnfcCustomizationRepo;
    private CvnfcCustomizationRepository cvnfcCustomizationRepo;
    private VnfResourceInstaller vnfResourceInstaller;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
        vnfRepo = mock(VnfResourceRepository.class);
        serviceRepo = mock(ServiceRepository.class);
        vnfcCustomizationRepo = mock(VnfcCustomizationRepository.class);
        cvnfcCustomizationRepo = mock(CvnfcCustomizationRepository.class);
        vnfResourceInstaller = new VnfResourceInstaller(vnfRepo, mock(VFModuleRepository.class),
                mock(VnfcInstanceGroupCustomizationRepository.class), vnfcCustomizationRepo, cvnfcCustomizationRepo,
                mock(InstanceGroupRepository.class), serviceRepo, mock(HeatArtifactInstaller.class),
                mock(ConfigurationInstaller.class));
    }

    @Test
    public void processVfModulesAddsNewVnfCustomizationToService() throws Exception {
        IEntityDetails vfEntity = prepareVfEntity("vf-customization-uuid");
        VfResourceStructure vfResourceStructure = prepareVfResourceStructure("vf-customization-uuid");
        doReturn(csarHelper).when(toscaResourceStructure).getSdcCsarHelper();
        Service service = new Service();
        service.setModelUUID("service-uuid");

        vnfResourceInstaller.processVfModules(vfEntity, toscaResourceStructure, vfResourceStructure, service,
                vfEntity.getMetadata());

        assertEquals(1, service.getVnfCustomizations().size());
        VnfResourceCustomization vnfCustomization = service.getVnfCustomizations().get(0);
        assertEquals("vf-customization-uuid", vnfCustomization.getModelCustomizationUUID());
        assertEquals("vnf-model-uuid", vnfCustomization.getVnfResources().getModelUUID());
        assertSame(service, vnfCustomization.getService());
        verify(vnfRepo).findResourceByModelUUID("vnf-model-uuid");
        verify(serviceRepo).findByModelUUID("service-uuid");
    }

    @Test
    public void processVfModulesSkipsVfWhoseCustomizationUuidDiffersFromNotification() throws Exception {
        IEntityDetails vfEntity = prepareVfEntity("vf-customization-uuid");
        VfResourceStructure vfResourceStructure = prepareVfResourceStructure("other-customization-uuid");
        Service service = new Service();

        vnfResourceInstaller.processVfModules(vfEntity, toscaResourceStructure, vfResourceStructure, service,
                vfEntity.getMetadata());

        assertTrue(service.getVnfCustomizations().isEmpty());
        verify(vnfRepo, never()).findResourceByModelUUID(any());
    }

    private IEntityDetails prepareVfEntity(String customizationUuid) {
        Metadata metadata = mock(Metadata.class);
        doReturn(customizationUuid).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
        doReturn("vnf-model-uuid").when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_UUID);
        IEntityDetails vfEntity = mock(IEntityDetails.class);
        doReturn(metadata).when(vfEntity).getMetadata();
        return vfEntity;
    }

    private VfResourceStructure prepareVfResourceStructure(String notificationCustomizationUuid) {
        IResourceInstance resourceInstance = mock(IResourceInstance.class);
        doReturn(notificationCustomizationUuid).when(resourceInstance).getResourceCustomizationUUID();
        VfResourceStructure vfResourceStructure = mock(VfResourceStructure.class);
        doReturn(resourceInstance).when(vfResourceStructure).getResourceInstance();
        return vfResourceStructure;
    }

    @Test
    public void findExistingVfcReturnsMatchFromSetWithoutRepositoryLookup() {
        VnfcCustomization other = new VnfcCustomization();
        other.setModelCustomizationUUID("other-uuid");
        VnfcCustomization match = new VnfcCustomization();
        match.setModelCustomizationUUID(CUSTOMIZATION_UUID);
        Set<VnfcCustomization> existing = new HashSet<>();
        existing.add(other);
        existing.add(match);

        assertSame(match, vnfResourceInstaller.findExistingVfc(existing, CUSTOMIZATION_UUID));
        verify(vnfcCustomizationRepo, never()).findOneByModelCustomizationUUID(CUSTOMIZATION_UUID);
    }

    @Test
    public void findExistingVfcFallsBackToRepositoryWhenAbsentFromSet() {
        VnfcCustomization stored = new VnfcCustomization();
        doReturn(stored).when(vnfcCustomizationRepo).findOneByModelCustomizationUUID(CUSTOMIZATION_UUID);
        VnfcCustomization other = new VnfcCustomization();
        other.setModelCustomizationUUID("other-uuid");
        Set<VnfcCustomization> existing = new HashSet<>();
        existing.add(other);

        assertSame(stored, vnfResourceInstaller.findExistingVfc(existing, CUSTOMIZATION_UUID));
        verify(vnfcCustomizationRepo).findOneByModelCustomizationUUID(CUSTOMIZATION_UUID);
    }

    @Test
    public void findExistingCvfcReturnsMatchFromSetWithoutRepositoryLookup() {
        CvnfcCustomization other = new CvnfcCustomization();
        other.setId(1);
        other.setModelCustomizationUUID("other-uuid");
        CvnfcCustomization match = new CvnfcCustomization();
        match.setId(2);
        match.setModelCustomizationUUID(CUSTOMIZATION_UUID);
        Set<CvnfcCustomization> existing = new HashSet<>();
        existing.add(other);
        existing.add(match);

        assertSame(match, vnfResourceInstaller.findExistingCvfc(existing, CUSTOMIZATION_UUID));
        verify(cvnfcCustomizationRepo, never()).findOneByModelCustomizationUUID(CUSTOMIZATION_UUID);
    }

    @Test
    public void findExistingCvfcFallsBackToRepositoryWhenAbsentFromSet() {
        CvnfcCustomization stored = new CvnfcCustomization();
        doReturn(stored).when(cvnfcCustomizationRepo).findOneByModelCustomizationUUID(CUSTOMIZATION_UUID);
        CvnfcCustomization other = new CvnfcCustomization();
        other.setModelCustomizationUUID("other-uuid");
        Set<CvnfcCustomization> existing = new HashSet<>();
        existing.add(other);

        assertSame(stored, vnfResourceInstaller.findExistingCvfc(existing, CUSTOMIZATION_UUID));
        verify(cvnfcCustomizationRepo).findOneByModelCustomizationUUID(CUSTOMIZATION_UUID);
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

        VnfResourceInstaller installer = vnfResourceInstaller;
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

    @Test
    public void vnfCustomizationParsesInstanceCountsWithScalableCapability() {
        IEntityDetails vfEntity = vfEntityWithCounts("2", "1", "4", true);

        VnfResourceCustomization customization = vnfResourceInstaller.createVnfResourceCustomization(vfEntity,
                new ToscaResourceStructure(), new VnfResource());

        assertEquals(Integer.valueOf(2), customization.getAvailabilityZoneMaxCount());
        assertEquals(Integer.valueOf(1), customization.getMinInstances());
        assertEquals(Integer.valueOf(4), customization.getMaxInstances());
    }

    @Test
    public void vnfCustomizationParsesInstanceCountsWithoutScalableCapability() {
        IEntityDetails vfEntity = vfEntityWithCounts(null, "0", "3", false);

        VnfResourceCustomization customization = vnfResourceInstaller.createVnfResourceCustomization(vfEntity,
                new ToscaResourceStructure(), new VnfResource());

        assertNull(customization.getAvailabilityZoneMaxCount());
        assertEquals(Integer.valueOf(0), customization.getMinInstances());
        assertEquals(Integer.valueOf(3), customization.getMaxInstances());
    }

    @Test
    public void vnfCustomizationStoresBlankOrNonNumericCountsAsNull() {
        IEntityDetails vfEntity = vfEntityWithCounts("", "", "UNBOUNDED", true);

        VnfResourceCustomization customization = vnfResourceInstaller.createVnfResourceCustomization(vfEntity,
                new ToscaResourceStructure(), new VnfResource());

        assertNull(customization.getAvailabilityZoneMaxCount());
        assertNull(customization.getMinInstances());
        assertNull(customization.getMaxInstances());
    }

    private IEntityDetails vfEntityWithCounts(String availabilityZoneCount, String minInstances, String maxInstances,
            boolean scalable) {
        Metadata metadata = mock(Metadata.class);
        doReturn(availabilityZoneCount).when(metadata).getValue(SdcPropertyNames.PROPERTY_NAME_AVAILABILITYZONECOUNT);
        IEntityDetails vfEntity = mock(IEntityDetails.class);
        doReturn(metadata).when(vfEntity).getMetadata();
        doReturn(Map.of(SdcPropertyNames.PROPERTY_NAME_MININSTANCES, property(minInstances),
                SdcPropertyNames.PROPERTY_NAME_MAXINSTANCES, property(maxInstances))).when(vfEntity).getProperties();
        if (scalable) {
            CapabilityAssignment capability = mock(CapabilityAssignment.class);
            doReturn("scalable").when(capability).getName();
            doReturn(List.of(capability)).when(vfEntity).getCapabilities();
        }
        return vfEntity;
    }

    private static Property property(Object value) {
        Property property = mock(Property.class);
        doReturn(value).when(property).getValue();
        return property;
    }
}
