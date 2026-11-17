/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.EntityTemplateType;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.RequirementAssignment;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.onap.so.asdc.installer.ResourceStructure;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.ServiceArtifact;
import org.onap.so.db.catalog.beans.ServiceInfo;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.db.catalog.data.repository.ServiceRepository;
import org.onap.so.db.catalog.data.repository.ToscaCsarRepository;

public class ServiceModelInstallerTest {

    @Mock
    private ISdcCsarHelper sdcCsarHelper;
    @Mock
    private IEntityDetails entityDetails;

    private ServiceRepository serviceRepo;
    private ToscaCsarRepository toscaCsarRepo;
    private ServiceModelInstaller serviceModelInstaller;

    @Before
    public void before() {
        MockitoAnnotations.openMocks(this);
        serviceRepo = mock(ServiceRepository.class);
        toscaCsarRepo = mock(ToscaCsarRepository.class);
        serviceModelInstaller = new ServiceModelInstaller(serviceRepo, toscaCsarRepo);
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

        Service service = serviceModelInstaller.createService(toscaResourceStructure, resourceStructure);

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

    @Test
    public void createServiceReusesServiceFoundByModelUuid() {
        Map<String, Object> meta = new HashMap<>();
        meta.put(SdcPropertyNames.PROPERTY_NAME_UUID, "service-uuid");
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();
        toscaResourceStructure.setServiceMetadata(new Metadata(meta));
        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);
        when(sdcCsarHelper.getServiceInputs()).thenReturn(Collections.emptyList());
        Service existing = new Service();
        when(serviceRepo.findByModelUUID("service-uuid")).thenReturn(Arrays.asList(existing));

        Service service = serviceModelInstaller.createService(toscaResourceStructure, null);

        assertSame(existing, service);
        assertSame(existing, toscaResourceStructure.getCatalogService());
    }

    @Test
    public void processResourceSequenceTest() {
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();
        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);
        ArrayList<Input> inputs = new ArrayList<>();
        Service service = new Service();

        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("name", "node1");

        Metadata metadata = new Metadata(hashMap);
        when(entityDetails.getMetadata()).thenReturn(metadata);
        when(sdcCsarHelper.getServiceInputs()).thenReturn(inputs);
        when(ToscaEntityHelper.getEntityDetails(toscaResourceStructure,
                EntityQuery.newBuilder(EntityTemplateType.NODE_TEMPLATE),
                TopologyTemplateQuery.newBuilder(SdcTypes.SERVICE), false)).thenReturn(Arrays.asList(entityDetails));


        when(entityDetails.getRequirements()).thenReturn(null);


        serviceModelInstaller.processResourceSequence(toscaResourceStructure, service);
        assertEquals(service.getResourceOrder(), "");
    }

    @Test
    public void processResourceSequencePlacesRequiringEntityAfterItsRequirement() {
        IEntityDetails a = namedEntity("A");
        IEntityDetails b = namedEntity("B");
        RequirementAssignment requiresA = mock(RequirementAssignment.class);
        when(requiresA.getNodeTemplateName()).thenReturn("A");
        when(a.getRequirements()).thenReturn(Collections.emptyList());
        when(b.getRequirements()).thenReturn(Arrays.asList(requiresA));
        when(sdcCsarHelper.getEntity(any(EntityQuery.class), any(TopologyTemplateQuery.class), anyBoolean()))
                .thenReturn(Arrays.asList(b, a));
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();
        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);
        Service service = new Service();

        serviceModelInstaller.processResourceSequence(toscaResourceStructure, service);

        assertEquals("A,B", service.getResourceOrder());
    }

    @Test
    public void createToscaCsarBuildsNewCsarWhenNoneIsStored() {
        IArtifactInfo artifact = mock(IArtifactInfo.class);
        when(artifact.getArtifactUUID()).thenReturn("csar-uuid");
        when(artifact.getArtifactName()).thenReturn("csar-name");
        when(toscaCsarRepo.findById("csar-uuid")).thenReturn(Optional.empty());
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();
        toscaResourceStructure.setToscaArtifact(artifact);

        serviceModelInstaller.createToscaCsar(toscaResourceStructure);

        ToscaCsar csar = toscaResourceStructure.getCatalogToscaCsar();
        assertEquals("csar-uuid", csar.getArtifactUUID());
        assertEquals("csar-name", csar.getName());
        assertEquals(ToscaEntityHelper.MANUAL_RECORD, csar.getArtifactChecksum());
    }

    @Test
    public void createToscaCsarUpdatesStoredCsar() {
        IArtifactInfo artifact = mock(IArtifactInfo.class);
        when(artifact.getArtifactUUID()).thenReturn("csar-uuid");
        when(artifact.getArtifactChecksum()).thenReturn("new-checksum");
        ToscaCsar stored = new ToscaCsar();
        stored.setArtifactUUID("csar-uuid");
        when(toscaCsarRepo.findById("csar-uuid")).thenReturn(Optional.of(stored));
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();
        toscaResourceStructure.setToscaArtifact(artifact);

        serviceModelInstaller.createToscaCsar(toscaResourceStructure);

        assertSame(stored, toscaResourceStructure.getCatalogToscaCsar());
        assertEquals("new-checksum", stored.getArtifactChecksum());
    }

    @Test
    public void createServiceInfoLeavesExistingServiceInfosUntouched() {
        Service service = new Service();
        ServiceInfo existing = new ServiceInfo();
        service.setServiceInfos(new ArrayList<>(Arrays.asList(existing)));

        serviceModelInstaller.createServiceInfo(new ToscaResourceStructure(), service);

        assertEquals(1, service.getServiceInfos().size());
        assertSame(existing, service.getServiceInfos().get(0));
    }

    @Test
    public void createServiceInfoStoresInputsWithoutReservedCdsInputs() {
        Input userInput = mock(Input.class);
        when(userInput.getName()).thenReturn("user_input");
        when(userInput.getType()).thenReturn("string");
        Input cdsModelName = mock(Input.class);
        when(cdsModelName.getName()).thenReturn("cds_model_name");
        when(sdcCsarHelper.getServiceInputs()).thenReturn(Arrays.asList(userInput, cdsModelName));
        when(sdcCsarHelper.getServiceNodeTemplates()).thenReturn(Collections.emptyList());
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();
        toscaResourceStructure.setSdcCsarHelper(sdcCsarHelper);
        Service service = new Service();

        serviceModelInstaller.createServiceInfo(toscaResourceStructure, service);

        ServiceInfo serviceInfo = service.getServiceInfos().get(0);
        assertSame(service, serviceInfo.getService());
        assertTrue(serviceInfo.getServiceInput().contains("\\\"name\\\":\\\"user_input\\\""));
        assertFalse(serviceInfo.getServiceInput().contains("cds_model_name"));
        assertNull(serviceInfo.getServiceProperties());
    }

    @Test
    public void createServiceArtifactIgnoresMissingResourceStructure() {
        Service service = new Service();
        List<ServiceArtifact> existing = new ArrayList<>(Arrays.asList(new ServiceArtifact()));
        service.setServiceArtifactList(existing);

        serviceModelInstaller.createServiceArtifact(service, null, "content");

        assertSame(existing, service.getServiceArtifactList());
        assertEquals(1, existing.size());
    }

    @Test
    public void createServiceArtifactKeepsOnlyWorkflowArtifacts() {
        IArtifactInfo workflow = mock(IArtifactInfo.class);
        when(workflow.getArtifactType()).thenReturn("WORKFLOW");
        when(workflow.getArtifactUUID()).thenReturn("workflow-uuid");
        IArtifactInfo other = mock(IArtifactInfo.class);
        when(other.getArtifactType()).thenReturn("TOSCA_CSAR");
        INotificationData notification = mock(INotificationData.class);
        when(notification.getServiceArtifacts()).thenReturn(Arrays.asList(workflow, other));
        VfResourceStructure vfResourceStructure = mock(VfResourceStructure.class);
        when(vfResourceStructure.getNotification()).thenReturn(notification);
        Service service = new Service();

        serviceModelInstaller.createServiceArtifact(service, vfResourceStructure, "content");

        List<ServiceArtifact> artifacts = service.getServiceArtifactList();
        assertEquals(1, artifacts.size());
        assertEquals("workflow-uuid", artifacts.get(0).getArtifactUUID());
        assertEquals("OTHER", artifacts.get(0).getType());
        assertEquals("content", artifacts.get(0).getContent());
        assertSame(service, artifacts.get(0).getService());
    }

    private static IEntityDetails namedEntity(String name) {
        IEntityDetails entity = mock(IEntityDetails.class);
        Map<String, Object> meta = new HashMap<>();
        meta.put("name", name);
        when(entity.getName()).thenReturn(name);
        when(entity.getMetadata()).thenReturn(new Metadata(meta));
        return entity;
    }
}
