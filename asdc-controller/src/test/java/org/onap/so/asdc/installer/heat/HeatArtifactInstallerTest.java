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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.so.asdc.client.ASDCConfiguration;
import org.onap.so.asdc.installer.IVfModuleData;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.VfModuleArtifact;
import org.onap.so.asdc.installer.VfModuleStructure;
import org.onap.so.asdc.installer.VfResourceStructure;
import org.onap.so.db.catalog.beans.HeatFiles;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.data.repository.HeatEnvironmentRepository;
import org.onap.so.db.catalog.data.repository.HeatFilesRepository;
import org.onap.so.db.catalog.data.repository.HeatTemplateRepository;

public class HeatArtifactInstallerTest {

    private HeatTemplateRepository heatRepo;
    private HeatFilesRepository heatFilesRepo;
    private HeatArtifactInstaller heatArtifactInstaller;

    @Before
    public void before() {
        heatRepo = mock(HeatTemplateRepository.class);
        heatFilesRepo = mock(HeatFilesRepository.class);
        heatArtifactInstaller =
                new HeatArtifactInstaller(heatRepo, mock(HeatEnvironmentRepository.class), heatFilesRepo);
    }

    @Test
    public void verifyTheFilePrefixInStringTest() {
        String body = "abcabcabcfile:///testfile.txtabcbabacbcabc";
        String filenameToVerify = "testfile.txt";
        String expectedFileBody = "abcabcabctestfile.txtabcbabacbcabc";

        String newFileBody = heatArtifactInstaller.verifyTheFilePrefixInString(body, filenameToVerify);

        assertEquals(expectedFileBody, newFileBody);
    }

    @Test
    public void verifyTheFilePrefixInStringNullBodyTest() {
        String body = null;
        String filenameToVerify = "testfile.txt";

        String newFileBody = heatArtifactInstaller.verifyTheFilePrefixInString(body, filenameToVerify);

        assertEquals(body, newFileBody);
    }

    @Test
    public void verifyTheFilePrefixInStringEmptyBodyTest() {
        String body = "";
        String filenameToVerify = "testfile.txt";

        String newFileBody = heatArtifactInstaller.verifyTheFilePrefixInString(body, filenameToVerify);

        assertEquals(body, newFileBody);
    }

    @Test
    public void verifyTheFilePrefixInStringNullFilenameTest() {
        String body = "abcabcabcfile:///testfile.txtabcbabacbcabc";
        String filenameToVerify = null;

        String newFileBody = heatArtifactInstaller.verifyTheFilePrefixInString(body, filenameToVerify);

        assertEquals(body, newFileBody);
    }

    @Test
    public void verifyTheFilePrefixInStringEmptyFilenameTest() {
        String body = "abcabcabcfile:///testfile.txtabcbabacbcabc";
        String filenameToVerify = "";

        String newFileBody = heatArtifactInstaller.verifyTheFilePrefixInString(body, filenameToVerify);

        assertEquals(body, newFileBody);
    }

    @Test
    public void createVfModuleNameJoinsServiceResourceInstanceAndModuleNames() {
        INotificationData notification = mock(INotificationData.class);
        doReturn("vFW-service").when(notification).getServiceName();
        IResourceInstance resourceInstance = mock(IResourceInstance.class);
        doReturn("vFW 0").when(resourceInstance).getResourceInstanceName();
        VfResourceStructure vfResourceStructure = mock(VfResourceStructure.class);
        doReturn(notification).when(vfResourceStructure).getNotification();
        doReturn(resourceInstance).when(vfResourceStructure).getResourceInstance();
        IVfModuleData vfModuleMetadata = mock(IVfModuleData.class);
        doReturn("VfwBase..base_vfw..module-0").when(vfModuleMetadata).getVfModuleModelName();
        VfModuleStructure vfModuleStructure = mock(VfModuleStructure.class);
        doReturn(vfResourceStructure).when(vfModuleStructure).getParentVfResource();
        doReturn(vfModuleMetadata).when(vfModuleStructure).getVfModuleMetadata();

        assertEquals("vFW-service/vFW 0::VfwBase..base_vfw..module-0",
                HeatArtifactInstaller.createVfModuleName(vfModuleStructure));
    }

    @Test
    public void extractHeatInformationReusesStoredHeatTemplate() throws Exception {
        VfModuleArtifact heatArtifact = artifact("heat-uuid", "base.yaml", ASDCConfiguration.HEAT, "body");
        HeatTemplate stored = new HeatTemplate();
        doReturn(stored).when(heatRepo).findByArtifactUuid("heat-uuid");

        heatArtifactInstaller.extractHeatInformation(new ToscaResourceStructure(), vfResource(heatArtifact));

        assertSame(stored, heatArtifact.getHeatTemplate());
        assertEquals(Integer.valueOf(30), stored.getTimeoutMinutes());
    }

    @Test
    public void extractHeatInformationCreatesHeatFileWhenNoneStored() throws Exception {
        VfModuleArtifact fileArtifact =
                artifact("file-uuid", "cloud-init.sh", ASDCConfiguration.HEAT_ARTIFACT, "#!/bin/sh");
        ToscaResourceStructure toscaResourceStructure = new ToscaResourceStructure();

        heatArtifactInstaller.extractHeatInformation(toscaResourceStructure, vfResource(fileArtifact));

        verify(heatFilesRepo).findByArtifactUuid("file-uuid");
        HeatFiles heatFile = fileArtifact.getHeatFiles();
        assertNotNull(heatFile);
        assertEquals("cloud-init.sh", heatFile.getFileName());
        assertEquals("#!/bin/sh", heatFile.getFileBody());
        assertEquals("MANUAL_RECORD", heatFile.getArtifactChecksum());
        assertEquals("file-uuid", toscaResourceStructure.getHeatFilesUUID());
    }

    private static VfModuleArtifact artifact(String uuid, String name, String type, String body) throws Exception {
        IArtifactInfo info = mock(IArtifactInfo.class);
        doReturn(uuid).when(info).getArtifactUUID();
        doReturn(name).when(info).getArtifactName();
        doReturn(type).when(info).getArtifactType();
        doReturn("1.0").when(info).getArtifactVersion();
        doReturn(30).when(info).getArtifactTimeout();
        return new VfModuleArtifact(info, null, body);
    }

    private static VfResourceStructure vfResource(VfModuleArtifact artifact) {
        Map<String, VfModuleArtifact> artifacts = new LinkedHashMap<>();
        artifacts.put(artifact.getArtifactInfo().getArtifactUUID(), artifact);
        VfResourceStructure vfResourceStructure = mock(VfResourceStructure.class);
        doReturn(artifacts).when(vfResourceStructure).getArtifactsMapByUUID();
        return vfResourceStructure;
    }
}
