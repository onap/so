/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.enums.SdcTypes;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.mockito.ArgumentCaptor;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkInstanceGroup;
import org.onap.so.db.catalog.beans.NetworkResource;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.TempNetworkHeatTemplateLookup;
import org.onap.so.db.catalog.data.repository.CollectionResourceRepository;
import org.onap.so.db.catalog.data.repository.HeatTemplateRepository;
import org.onap.so.db.catalog.data.repository.InstanceGroupRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceCustomizationRepository;
import org.onap.so.db.catalog.data.repository.NetworkResourceRepository;
import org.onap.so.db.catalog.data.repository.TempNetworkHeatTemplateRepository;

public class NetworkResourceInstallerTest {

    private HeatTemplateRepository heatRepo;
    private TempNetworkHeatTemplateRepository tempNetworkLookupRepo;
    private NetworkResourceRepository networkRepo;
    private CollectionResourceRepository collectionRepo;
    private InstanceGroupRepository instanceGroupRepo;
    private NetworkResourceInstaller installer;

    @Before
    public void setUp() {
        heatRepo = mock(HeatTemplateRepository.class);
        tempNetworkLookupRepo = mock(TempNetworkHeatTemplateRepository.class);
        networkRepo = mock(NetworkResourceRepository.class);
        collectionRepo = mock(CollectionResourceRepository.class);
        instanceGroupRepo = mock(InstanceGroupRepository.class);
        installer = new NetworkResourceInstaller(heatRepo, tempNetworkLookupRepo, networkRepo,
                mock(NetworkResourceCustomizationRepository.class), collectionRepo, instanceGroupRepo);
    }

    @Test
    public void findExistingNetworkResourcePrefersNetworkOnService() {
        NetworkResource existing = new NetworkResource();
        existing.setModelUUID("network-model-uuid");
        NetworkResourceCustomization customization = new NetworkResourceCustomization();
        customization.setNetworkResource(existing);
        Service service = new Service();
        service.getNetworkCustomizations().add(customization);

        NetworkResource found = installer.findExistingNetworkResource(service, "network-model-uuid");

        assertSame(existing, found);
        verify(networkRepo, never()).findResourceByModelUUID(anyString());
    }

    @Test
    public void processNetworksWithoutHeatTemplateThrows() {
        Metadata metadata = mock(Metadata.class);
        when(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)).thenReturn("network-model-name");
        IEntityDetails vlEntity = mock(IEntityDetails.class);
        when(vlEntity.getMetadata()).thenReturn(metadata);
        ISdcCsarHelper csarHelper = mock(ISdcCsarHelper.class);
        when(csarHelper.getEntity(any(EntityQuery.class), any(TopologyTemplateQuery.class), anyBoolean()))
                .thenReturn(Collections.singletonList(vlEntity));
        ToscaResourceStructure toscaResourceStruct = new ToscaResourceStructure();
        toscaResourceStruct.setSdcCsarHelper(csarHelper);
        TempNetworkHeatTemplateLookup lookup = new TempNetworkHeatTemplateLookup();
        lookup.setHeatTemplateArtifactUuid("heat-artifact-uuid");
        when(tempNetworkLookupRepo.findFirstBynetworkResourceModelName("network-model-name")).thenReturn(lookup);

        ArtifactInstallerException exception = assertThrows(ArtifactInstallerException.class,
                () -> installer.processNetworks(toscaResourceStruct, new Service()));

        assertEquals("No HeatTemplate found for artifactUUID: heat-artifact-uuid", exception.getMessage());
        verify(heatRepo).findByArtifactUuid("heat-artifact-uuid");
    }

    @Test
    public void processNetworkCollectionsWithoutCollectionResourcesLinksNothing() {
        ToscaResourceStructure toscaResourceStruct =
                toscaResourceStructWith(Collections.emptyList(), Collections.emptyList());
        Service service = new Service();

        installer.processNetworkCollections(toscaResourceStruct, service);

        assertEquals(Collections.emptyList(), service.getCollectionResourceCustomizations());
        verify(collectionRepo, never()).saveAndFlush(any());
    }

    @Test
    public void processNetworkCollectionsLinksEveryCollectionResourceToService() {
        IEntityDetails collectionA = entity("NetworkCollectionA", "collection-a-customization-uuid");
        IEntityDetails collectionB = entity("NetworkCollectionB", "collection-b-customization-uuid");
        ToscaResourceStructure toscaResourceStruct =
                toscaResourceStructWith(Arrays.asList(collectionA, collectionB), Collections.emptyList());
        Service service = new Service();

        installer.processNetworkCollections(toscaResourceStruct, service);

        List<CollectionResourceCustomization> linked = service.getCollectionResourceCustomizations();
        assertEquals(2, linked.size());
        assertEquals("collection-a-customization-uuid", linked.get(0).getModelCustomizationUUID());
        assertEquals("collection-b-customization-uuid", linked.get(1).getModelCustomizationUUID());
        ArgumentCaptor<CollectionResource> saved = ArgumentCaptor.forClass(CollectionResource.class);
        verify(collectionRepo, times(2)).saveAndFlush(saved.capture());
        assertSame(linked.get(0).getCollectionResource(), saved.getAllValues().get(0));
        assertSame(linked.get(1).getCollectionResource(), saved.getAllValues().get(1));
        assertNotSame(saved.getAllValues().get(0), saved.getAllValues().get(1));
    }

    @Test
    public void processNetworkCollectionsDoesNotCarryInstanceGroupsOverToNextCollection() {
        IEntityDetails collectionA = entity("NetworkCollectionA", "collection-a-customization-uuid");
        IEntityDetails collectionB = entity("NetworkCollectionB", "collection-b-customization-uuid");
        IEntityDetails groupOfA = entity("collection_a_group", "collection-a-group-customization-uuid");
        ToscaResourceStructure toscaResourceStruct =
                toscaResourceStructWith(Arrays.asList(collectionA, collectionB), Collections.singletonList(groupOfA));

        installer.processNetworkCollections(toscaResourceStruct, new Service());

        ArgumentCaptor<NetworkInstanceGroup> saved = ArgumentCaptor.forClass(NetworkInstanceGroup.class);
        verify(instanceGroupRepo).saveAndFlush(saved.capture());
        assertEquals("collection_a_group", saved.getValue().getModelName());
        assertEquals(Collections.emptyList(), toscaResourceStruct.getCatalogNetworkInstanceGroup());
    }

    private static IEntityDetails entity(String name, String customizationUuid) {
        Metadata metadata = mock(Metadata.class);
        when(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_NAME)).thenReturn(name);
        when(metadata.getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID)).thenReturn(customizationUuid);
        IEntityDetails entity = mock(IEntityDetails.class);
        when(entity.getMetadata()).thenReturn(metadata);
        when(entity.getName()).thenReturn(name);
        return entity;
    }

    /**
     * Answers the service-level CR query with {@code collections}, and the NetworkCollection group query with
     * {@code groupsOfFirstCollection} for the first collection only, so later collections have no groups.
     */
    private static ToscaResourceStructure toscaResourceStructWith(List<IEntityDetails> collections,
            List<IEntityDetails> groupsOfFirstCollection) {
        String firstCollectionCustomizationUuid = collections.isEmpty() ? null
                : collections.get(0).getMetadata().getValue(SdcPropertyNames.PROPERTY_NAME_CUSTOMIZATIONUUID);
        ISdcCsarHelper csarHelper = mock(ISdcCsarHelper.class);
        when(csarHelper.getEntity(any(EntityQuery.class), any(TopologyTemplateQuery.class), anyBoolean()))
                .thenAnswer(invocation -> {
                    EntityQuery entityQuery = invocation.getArgument(0);
                    TopologyTemplateQuery topologyQuery = invocation.getArgument(1);
                    if (topologyQuery.getNodeTemplateType() == SdcTypes.SERVICE) {
                        return entityQuery.getNodeTemplateType() == SdcTypes.CR ? collections : null;
                    }
                    if (entityQuery.getNodeTemplateType() == SdcTypes.VL) {
                        return Collections.emptyList();
                    }
                    return topologyQuery.getCustomizationUUID().equals(firstCollectionCustomizationUuid)
                            ? groupsOfFirstCollection
                            : Collections.emptyList();
                });
        ToscaResourceStructure toscaResourceStruct = new ToscaResourceStructure();
        toscaResourceStruct.setSdcCsarHelper(csarHelper);
        return toscaResourceStruct;
    }
}
