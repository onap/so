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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.Service;
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
    public void processVNFCGroupSequenceKeepsModelOrderWithoutRequirements() {
        IEntityDetails group2 = vnfcGroup("group2", vfc("b"));
        IEntityDetails group1 = vnfcGroup("group1", vfc("a"));

        assertEquals(Arrays.asList("group2", "group1"),
                vnfResourceInstaller.processVNFCGroupSequence(Arrays.asList(group2, group1)));
    }

    @Test
    public void processVNFCGroupSequenceOrdersRequirementChainOutOfModelOrder() {
        IEntityDetails group3 = vnfcGroup("group3", vfc("c", "b"));
        IEntityDetails group2 = vnfcGroup("group2", vfc("b", "a"));
        IEntityDetails group1 = vnfcGroup("group1", vfc("a"));

        assertEquals(Arrays.asList("group1", "group2", "group3"),
                vnfResourceInstaller.processVNFCGroupSequence(Arrays.asList(group3, group2, group1)));
    }

    @Test
    public void processVNFCGroupSequenceWaitsForAllRequirementsOfAllMembers() {
        IEntityDetails group4 = vnfcGroup("group4", vfc("d1", "a"), vfc("d2", "c"));
        IEntityDetails group3 = vnfcGroup("group3", vfc("c", "b"));
        IEntityDetails group2 = vnfcGroup("group2", vfc("b", "a"));
        IEntityDetails group1 = vnfcGroup("group1", vfc("a"));

        assertEquals(Arrays.asList("group1", "group2", "group3", "group4"),
                vnfResourceInstaller.processVNFCGroupSequence(Arrays.asList(group4, group3, group2, group1)));
    }

    @Test
    public void processVNFCGroupSequenceIgnoresRequirementsOutsideOtherGroups() {
        IEntityDetails ownMember = vnfcGroup("own-member", vfc("p"), vfc("q", "p"));
        IEntityDetails outsideGroups = vnfcGroup("outside-groups", vfc("r", "not-in-a-group"));
        IEntityDetails group1 = vnfcGroup("group1", vfc("a"));

        assertEquals(Arrays.asList("group1", "own-member", "outside-groups"),
                vnfResourceInstaller.processVNFCGroupSequence(Arrays.asList(ownMember, outsideGroups, group1)));
    }

    @Test
    public void processVNFCGroupSequenceAppendsGroupsInCycleInModelOrder() {
        IEntityDetails groupX = vnfcGroup("groupX", vfc("x", "y"));
        IEntityDetails group1 = vnfcGroup("group1", vfc("a"));
        IEntityDetails groupY = vnfcGroup("groupY", vfc("y", "x"));
        IEntityDetails group2 = vnfcGroup("group2", vfc("b", "a"));

        assertEquals(Arrays.asList("group1", "group2", "groupX", "groupY"),
                vnfResourceInstaller.processVNFCGroupSequence(Arrays.asList(groupX, group1, groupY, group2)));
    }

    private static IEntityDetails vnfcGroup(String name, IEntityDetails... members) {
        IEntityDetails group = mock(IEntityDetails.class);
        doReturn(name).when(group).getName();
        doReturn(Arrays.asList(members)).when(group).getMemberNodes();
        return group;
    }

    private static IEntityDetails vfc(String name, String... requiredNodes) {
        IEntityDetails vfc = mock(IEntityDetails.class);
        doReturn(name).when(vfc).getName();
        List<RequirementAssignment> requirements = new ArrayList<>();
        for (String requiredNode : requiredNodes) {
            RequirementAssignment requirement = mock(RequirementAssignment.class);
            doReturn(requiredNode).when(requirement).getNodeTemplateName();
            requirements.add(requirement);
        }
        doReturn(requirements).when(vfc).getRequirements();
        return vfc;
    }
}
