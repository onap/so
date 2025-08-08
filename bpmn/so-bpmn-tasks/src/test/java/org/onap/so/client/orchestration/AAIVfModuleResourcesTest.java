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
import java.util.Optional;
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
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.client.aai.mapper.AAIObjectMapper;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AAIVfModuleResourcesTest extends TestDataSetup {
    @InjectMocks
    private AAIVfModuleResources aaiVfModuleResources = new AAIVfModuleResources();

    private VfModule vfModule;
    private GenericVnf vnf;

    @Mock
    protected AAIResourcesClient MOCK_aaiResourcesClient;

    @Mock
    protected AAIObjectMapper MOCK_aaiObjectMapper;

    @Mock
    protected InjectionHelper MOCK_injectionHelper;

    @Before
    public void before() {
        vfModule = buildVfModule();
        vnf = buildGenericVnf();
        doReturn(MOCK_aaiResourcesClient).when(MOCK_injectionHelper).getAaiClient();
    }

    @Test
    public void updateOrchestrationStatusVfModuleTest() {
        vfModule.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.VfModule.class));

        aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf, OrchestrationStatus.ACTIVE);

        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), ArgumentMatchers.isNull());

        assertEquals(OrchestrationStatus.ACTIVE, vfModule.getOrchestrationStatus());
    }

    @Test
    public void createVfModuleTest() {
        vfModule.setOrchestrationStatus(OrchestrationStatus.PRECREATED);

        doReturn(new org.onap.aai.domain.yang.VfModule()).when(MOCK_aaiObjectMapper).mapVfModule(vfModule);
        doReturn(MOCK_aaiResourcesClient).when(MOCK_aaiResourcesClient).createIfNotExists(isA(AAIResourceUri.class),
                any(Optional.class));
        aaiVfModuleResources.createVfModule(vfModule, vnf);

        verify(MOCK_aaiResourcesClient, times(1)).createIfNotExists(any(AAIResourceUri.class), any(Optional.class));
        assertEquals(OrchestrationStatus.INVENTORIED, vfModule.getOrchestrationStatus());
    }

    @Test
    public void deleteVfModuleTest() {
        doNothing().when(MOCK_aaiResourcesClient).delete(isA(AAIResourceUri.class));

        aaiVfModuleResources.deleteVfModule(vfModule, vnf);

        verify(MOCK_aaiResourcesClient, times(1)).delete(any(AAIResourceUri.class));
    }

    @Test
    public void changeAssignVfModuleTest() {
        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.VfModule.class));

        aaiVfModuleResources.changeAssignVfModule(vfModule, vnf);

        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), ArgumentMatchers.isNull());
    }

    @Test
    public void connectVfModuleToVolumeGroupTest() {
        VolumeGroup volumeGroup = buildVolumeGroup();
        volumeGroup.setOrchestrationStatus(OrchestrationStatus.ASSIGNED);

        CloudRegion cloudRegion = buildCloudRegion();

        aaiVfModuleResources.connectVfModuleToVolumeGroup(vnf, vfModule, volumeGroup, cloudRegion);
        verify(MOCK_aaiResourcesClient, times(1)).connect(any(AAIResourceUri.class), any(AAIResourceUri.class));
    }

    @Test
    public void updateHeatStackIdVfModuleTest() {
        vfModule.setHeatStackId("testHeatStackId");

        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.VfModule.class));

        aaiVfModuleResources.updateHeatStackIdVfModule(vfModule, vnf);

        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), ArgumentMatchers.isNull());

        assertEquals("testHeatStackId", vfModule.getHeatStackId());
    }

    @Test
    public void updateContrailServiceInstanceFqdnVfModuleTest() {
        vfModule.setContrailServiceInstanceFqdn("testContrailServiceInstanceFqdn");

        doNothing().when(MOCK_aaiResourcesClient).update(isA(AAIResourceUri.class),
                isA(org.onap.aai.domain.yang.VfModule.class));

        aaiVfModuleResources.updateContrailServiceInstanceFqdnVfModule(vfModule, vnf);

        verify(MOCK_aaiResourcesClient, times(1)).update(any(AAIResourceUri.class), ArgumentMatchers.isNull());

        assertEquals("testContrailServiceInstanceFqdn", vfModule.getContrailServiceInstanceFqdn());
    }

    @Test
    public void checkNameInUseTrueTest() {
        AAIPluralResourceUri vfModuleUri = AAIUriFactory.createNodesUri(Types.VF_MODULES.getFragment())
                .queryParam("vf-module-name", vfModule.getVfModuleName());
        AAIPluralResourceUri vfModuleUriWithCustomization = vfModuleUri.clone().queryParam("model-customization-id",
                vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        doReturn(false).when(MOCK_aaiResourcesClient).exists(eq(vfModuleUriWithCustomization));
        doReturn(true).when(MOCK_aaiResourcesClient).exists(eq(vfModuleUri));
        boolean nameInUse = aaiVfModuleResources.checkNameInUse(vfModule);
        assertTrue(nameInUse);
    }

    @Test
    public void checkNameInUseFalseIsResumeTest() {
        AAIPluralResourceUri vfModuleUri = AAIUriFactory.createNodesUri(Types.VF_MODULES.getFragment())
                .queryParam("vf-module-name", vfModule.getVfModuleName());
        AAIPluralResourceUri vfModuleUriWithCustomization = vfModuleUri.clone().queryParam("model-customization-id",
                vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        doReturn(true).when(MOCK_aaiResourcesClient).exists(eq(vfModuleUriWithCustomization));
        boolean nameInUse = aaiVfModuleResources.checkNameInUse(vfModule);
        assertFalse(nameInUse);
    }

    @Test
    public void checkNameInUseFalseTest() {
        AAIPluralResourceUri vfModuleUri = AAIUriFactory.createNodesUri(Types.VF_MODULES.getFragment())
                .queryParam("vf-module-name", vfModule.getVfModuleName());
        AAIPluralResourceUri vfModuleUriWithCustomization = vfModuleUri.clone().queryParam("model-customization-id",
                vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        doReturn(false).when(MOCK_aaiResourcesClient).exists(eq(vfModuleUriWithCustomization));
        doReturn(false).when(MOCK_aaiResourcesClient).exists(eq(vfModuleUri));
        boolean nameInUse = aaiVfModuleResources.checkNameInUse(vfModule);
        assertFalse(nameInUse);
    }
}
