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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.elements.queries.EntityQuery;
import org.onap.sdc.tosca.parser.elements.queries.TopologyTemplateQuery;
import org.onap.sdc.tosca.parser.impl.SdcPropertyNames;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.ToscaResourceStructure;
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
    private NetworkResourceInstaller installer;

    @Before
    public void setUp() {
        heatRepo = mock(HeatTemplateRepository.class);
        tempNetworkLookupRepo = mock(TempNetworkHeatTemplateRepository.class);
        networkRepo = mock(NetworkResourceRepository.class);
        installer = new NetworkResourceInstaller(heatRepo, tempNetworkLookupRepo, networkRepo,
                mock(NetworkResourceCustomizationRepository.class), mock(CollectionResourceRepository.class),
                mock(InstanceGroupRepository.class));
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
}
