/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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


package org.onap.so.asdc.client;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.installer.ASDCElementInfo;
import org.onap.so.asdc.installer.VfModuleStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.asdc.installer.IVfModuleData;

public class ASDCElementInfoTest {

    @Test
    public void createASDCElementInfoWithNullParameterTest() {
        ASDCElementInfo elementInfoFromNullVfArtifact = ASDCElementInfo.createElementFromVfArtifactInfo(null);
        ASDCElementInfo elementInfoFromNullVfModuleStructure = ASDCElementInfo.createElementFromVfModuleStructure(null);
        ASDCElementInfo elementInfoFromNullVfResourceStructure =
                ASDCElementInfo.createElementFromVfResourceStructure(null);

        elementInfoFromNullVfArtifact.addElementInfo(null, null);
        elementInfoFromNullVfModuleStructure.addElementInfo(null, "someValue");
        elementInfoFromNullVfResourceStructure.addElementInfo("someKey", null);

        assertEquals(elementInfoFromNullVfArtifact.toString(), "");
        assertEquals(elementInfoFromNullVfModuleStructure.toString(), "");
        assertEquals(elementInfoFromNullVfResourceStructure.toString(), "");

        assertNotNull(elementInfoFromNullVfArtifact);
        assertNotNull(elementInfoFromNullVfModuleStructure);
        assertNotNull(elementInfoFromNullVfResourceStructure);

        assertNotNull(ASDCElementInfo.EMPTY_INSTANCE);

        assertEquals(elementInfoFromNullVfArtifact, ASDCElementInfo.EMPTY_INSTANCE);
        assertEquals(elementInfoFromNullVfModuleStructure, ASDCElementInfo.EMPTY_INSTANCE);
        assertEquals(elementInfoFromNullVfResourceStructure, ASDCElementInfo.EMPTY_INSTANCE);

        assertEquals(ASDCElementInfo.EMPTY_INSTANCE.getType(), "");
        assertEquals(ASDCElementInfo.EMPTY_INSTANCE.toString(), "");

        assertEquals(elementInfoFromNullVfArtifact.getType(), ASDCElementInfo.EMPTY_INSTANCE.getType());
        assertEquals(elementInfoFromNullVfModuleStructure.getType(), ASDCElementInfo.EMPTY_INSTANCE.getType());
        assertEquals(elementInfoFromNullVfResourceStructure.getType(), ASDCElementInfo.EMPTY_INSTANCE.getType());
    }

    @Test
    public void createASDCElementInfoFromVfResourceTest() {

        String resourceInstanceName = "Resource 1";

        UUID generatedUUID = UUID.randomUUID();

        INotificationData notificationData = Mockito.mock(INotificationData.class);
        IResourceInstance resourceInstance = Mockito.mock(IResourceInstance.class);

        Mockito.when(resourceInstance.getResourceInstanceName()).thenReturn(resourceInstanceName);
        Mockito.when(resourceInstance.getResourceInvariantUUID()).thenReturn(generatedUUID.toString());

        VfResourceStructure vfResourceStructure = new VfResourceStructure(notificationData, resourceInstance);

        ASDCElementInfo elementInfoFromVfResource =
                ASDCElementInfo.createElementFromVfResourceStructure(vfResourceStructure);

        assertTrue(elementInfoFromVfResource.toString().contains(resourceInstanceName));
        assertTrue(elementInfoFromVfResource.toString().contains(generatedUUID.toString()));

        assertFalse(ASDCConfiguration.VF_MODULES_METADATA.equals(elementInfoFromVfResource.getType()));
        assertEquals(ASDCElementInfo.ASDCElementTypeEnum.VNF_RESOURCE.name(), elementInfoFromVfResource.getType());

        assertFalse(elementInfoFromVfResource.toString().contains("MyInfo1: someValue"));
        elementInfoFromVfResource.addElementInfo("MyInfo1", "someValue");
        assertTrue(elementInfoFromVfResource.toString().contains("MyInfo1: someValue"));
    }

    @Test
    public void createASDCElementInfoFromVfModuleTest() throws ArtifactInstallerException {

        String resourceInstanceName = "Resource 1";

        UUID generatedUUID = UUID.randomUUID();

        INotificationData notificationData = Mockito.mock(INotificationData.class);
        IResourceInstance resourceInstance = Mockito.mock(IResourceInstance.class);

        Mockito.when(resourceInstance.getResourceInstanceName()).thenReturn(resourceInstanceName);
        Mockito.when(resourceInstance.getResourceInvariantUUID()).thenReturn(generatedUUID.toString());

        VfResourceStructure vfResourceStructure = new VfResourceStructure(notificationData, resourceInstance);

        // Create module structure now

        String vfModuleModelName = "Module Model XYZ";

        UUID generatedUUIDForModule = UUID.randomUUID();

        IVfModuleData moduleMetadata = Mockito.mock(IVfModuleData.class);
        Mockito.when(moduleMetadata.getVfModuleModelName()).thenReturn(vfModuleModelName);
        Mockito.when(moduleMetadata.getVfModuleModelInvariantUUID()).thenReturn(generatedUUIDForModule.toString());
        Mockito.when(moduleMetadata.getArtifacts()).thenReturn(Collections.<String>emptyList());

        VfModuleStructure vfModuleStructure = new VfModuleStructure(vfResourceStructure, moduleMetadata);

        ASDCElementInfo elementInfoFromVfModule = ASDCElementInfo.createElementFromVfModuleStructure(vfModuleStructure);

        assertTrue(elementInfoFromVfModule.toString().contains(vfModuleModelName));
        assertTrue(elementInfoFromVfModule.toString().contains(generatedUUIDForModule.toString()));

        assertFalse(ASDCElementInfo.ASDCElementTypeEnum.VNF_RESOURCE.name().equals(elementInfoFromVfModule.getType()));
        assertEquals(ASDCConfiguration.VF_MODULES_METADATA, elementInfoFromVfModule.getType());

        assertFalse(elementInfoFromVfModule.toString().contains("MyInfo2: someValue"));
        elementInfoFromVfModule.addElementInfo("MyInfo2", "someValue");
        assertTrue(elementInfoFromVfModule.toString().contains("MyInfo2: someValue"));
    }

    @Test
    public void createASDCElementInfoFromArtifact() {
        for (String eVal : ASDCConfiguration.SUPPORTED_ARTIFACT_TYPES_LIST) {
            String generatedArtifactName = eVal + " 1";
            UUID generatedUUIDForArtifact = UUID.randomUUID();

            IArtifactInfo artifactInfo = Mockito.mock(IArtifactInfo.class);
            Mockito.when(artifactInfo.getArtifactType()).thenReturn(eVal);
            Mockito.when(artifactInfo.getArtifactName()).thenReturn(generatedArtifactName);
            Mockito.when(artifactInfo.getArtifactUUID()).thenReturn(generatedUUIDForArtifact.toString());

            ASDCElementInfo elementInfoFromArtifact = ASDCElementInfo.createElementFromVfArtifactInfo(artifactInfo);

            assertTrue(elementInfoFromArtifact.toString().contains(generatedArtifactName));
            assertTrue(elementInfoFromArtifact.toString().contains(generatedUUIDForArtifact.toString()));

            assertFalse(
                    ASDCElementInfo.ASDCElementTypeEnum.VNF_RESOURCE.name().equals(elementInfoFromArtifact.getType()));
            assertEquals(eVal, elementInfoFromArtifact.getType());

            assertFalse(elementInfoFromArtifact.toString().contains("MyInfo3: someValue"));
            elementInfoFromArtifact.addElementInfo("MyInfo3", "someValue");
            assertTrue(elementInfoFromArtifact.toString().contains("MyInfo3: someValue"));
        }
    }
}
