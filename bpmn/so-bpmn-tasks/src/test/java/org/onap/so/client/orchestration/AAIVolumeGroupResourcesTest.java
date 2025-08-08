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

package org.onap.so.client.orchestration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aaiclient.client.aai.AAIResourcesClient;
import org.onap.aaiclient.client.aai.entities.uri.AAIPluralResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri;
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory;
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types;
import org.onap.so.bpmn.common.InjectionHelper;
import org.onap.so.bpmn.common.data.TestDataSetup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAIVolumeGroupResourcesTest extends TestDataSetup {
    @InjectMocks
    private AAIVolumeGroupResources aaiVolumeGroupResources = new AAIVolumeGroupResources();

    private CloudRegion cloudRegion;
    private VolumeGroup volumeGroup;

    @Mock
    protected AAIResourcesClient MOCK_aaiResourcesClient;

    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;

    @Mock
    protected InjectionHelper MOCK_injectionHelper;

    @Before
    public void before() {
        cloudRegion = buildCloudRegion();
        volumeGroup = buildVolumeGroup();
        doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
    }



    @Test
    public void updateOrchestrationStatusVolumeGroupTest() {
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.VolumeGroup.class));

        aaiVolumeGroupResources.updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                OrchestrationStatus.ACTIVE);

        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), ArgumentMatchers.isNull());

        assertEquals(OrchestrationStatus.ACTIVE, volumeGroup.getOrchestrationStatus());
    }

    @Test
    public void createVolumeGroupTest() {
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.PRECREATED);

        doNothing().when(MOCK_aaiResourcesClient).create(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.VolumeGroup.class));

        aaiVolumeGroupResources.createVolumeGroup(volumeGroup, cloudRegion);

        verify(MOCK_aaiResourcesClient, times(1)).create(any(AAIResourceUri.class), ArgumentMatchers.isNull());

        assertEquals(OrchestrationStatus.ASSIGNED, volumeGroup.getOrchestrationStatus());
    }

    @Test
    public void connectVolumeGroupToVnfTest() {

        volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));

        aaiVolumeGroupResources.connectVolumeGroupToTenant(volumeGroup, cloudRegion);

        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void connectVolumeGroupToTenantTest() {
        GenericVnf genericVnf = buildGenericVnf();

        volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

        doNothing().when(MOCK_aaiResourcesClient).connect(isA(AAIResourceUri.class), isA(AAIResourceUri.class));

        aaiVolumeGroupResources.connectVolumeGroupToVnf(genericVnf, volumeGroup, cloudRegion);

        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void deleteVolumeGroupTest() {
        doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));

        aaiVolumeGroupResources.deleteVolumeGroup(volumeGroup, cloudRegion);

        verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
    }

    @Test
    public void updateHeatStackIdVolumeGroupTest() {
        volumeGroup.setHeatStackId("testVolumeHeatStackId");

        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.VolumeGroup.class));

        aaiVolumeGroupResources.updateHeatStackIdVolumeGroup(volumeGroup, cloudRegion);

        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), ArgumentMatchers.isNull());

        assertEquals("testVolumeHeatStackId", volumeGroup.getHeatStackId());
    }

    @Test
    public void checkNameInUseTrueTest() {
        AAIPluralResourceUri volumeGroupUri = AAIUriFactory.createNodesUri(Types.VOLUME_GROUPS.getFragment())
                .queryParam("volume-group-name", "testVolumeGroupName1");
        doReturn(true).when(MOCK_aaiResourcesClient).exists(eq(volumeGroupUri));
        boolean nameInUse = aaiVolumeGroupResources.checkNameInUse(volumeGroup);
        assertTrue(nameInUse);
    }

    @Test
    public void checkNameInUseFalseTest() {
        AAIPluralResourceUri volumeGroupUri = AAIUriFactory.createNodesUri(Types.VOLUME_GROUPS.getFragment())
                .queryParam("volume-group-name", "testVolumeGroupName1");
        doReturn(false).when(MOCK_aaiResourcesClient).exists(eq(volumeGroupUri));
        boolean nameInUse = aaiVolumeGroupResources.checkNameInUse(volumeGroup);
        assertFalse(nameInUse);
    }
}
