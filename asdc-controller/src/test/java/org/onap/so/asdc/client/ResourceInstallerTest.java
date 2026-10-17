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

package org.onap.so.asdc.client;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.DistributionStatusEnum;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.so.asdc.client.exceptions.ArtifactInstallerException;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
import org.onap.so.asdc.client.test.emulators.ResourceInfoImpl;
import org.onap.so.asdc.installer.ToscaResourceStructure;
import org.onap.so.asdc.installer.heat.ToscaResourceInstaller;
import org.onap.so.db.request.data.repository.WatchdogComponentDistributionStatusRepository;

@RunWith(MockitoJUnitRunner.class)
public class ResourceInstallerTest {

    @Mock
    private ToscaResourceInstaller toscaInstaller;
    @Mock
    private ArtifactDownloader artifactDownloader;
    @Mock
    private DistributionStatusSender statusSender;
    @Mock
    private WatchdogComponentDistributionStatusRepository watchdogCDStatusRepository;
    @Mock
    private IDistributionClient client;

    @InjectMocks
    private ResourceInstaller resourceInstaller;

    /**
     * A notification carrying no service artifacts and no resources must short-circuit on the empty-resources guard and
     * never attempt to install anything.
     */
    @Test
    public void processResourceNotification_noResources_shortCircuitsWithoutInstalling() throws Exception {
        when(artifactDownloader.getMsoConfigPath()).thenReturn("");
        NotificationDataImpl notif = new NotificationDataImpl();
        notif.setDistributionID("dist-1");
        notif.setServiceUUID("svc-1");
        notif.setResources(Collections.emptyList());
        notif.setServiceArtifacts(Collections.emptyList());

        resourceInstaller.processResourceNotification(client, notif);

        // with no resources present, we never install a resource
        verify(toscaInstaller, never()).installTheResource(any(), any());
    }

    @Test
    public void isCsarAlreadyDeployed_reportsEveryResourceThroughItsOwnArtifacts() throws Exception {
        NotificationDataImpl notif = new NotificationDataImpl();
        notif.setDistributionID("dist-1");
        notif.setResources(Arrays.asList(resource("res-1"), resource("res-2")));
        IArtifactInfo csar = mock(IArtifactInfo.class);
        when(csar.getArtifactUUID()).thenReturn("csar-1");
        ToscaResourceStructure toscaResourceStructure = mock(ToscaResourceStructure.class);
        when(toscaResourceStructure.getToscaArtifact()).thenReturn(csar);
        when(toscaInstaller.isCsarAlreadyDeployed(toscaResourceStructure)).thenReturn(true);

        resourceInstaller.isCsarAlreadyDeployed(client, notif, toscaResourceStructure);

        verify(statusSender, times(1)).sendCsarDeployNotification(eq(client), any(), eq(toscaResourceStructure),
                eq(DistributionStatusEnum.ALREADY_DEPLOYED), eq("Csar with UUID: csar-1 already exists"));
        for (String uuid : Arrays.asList("res-1", "res-2")) {
            verify(statusSender).sendDeployNotificationsForResource(eq(client),
                    argThat(structure -> uuid.equals(structure.getResourceInstance().getResourceUUID())),
                    eq(DistributionStatusEnum.ALREADY_DEPLOYED), eq("Resource with UUID: " + uuid + " already exists"));
        }
    }

    @Test
    public void isCsarAlreadyDeployed_checksumMismatch_reportsTheFailureOnEveryResource() throws Exception {
        NotificationDataImpl notif = new NotificationDataImpl();
        notif.setDistributionID("dist-1");
        notif.setResources(Collections.singletonList(resource("res-1")));
        ToscaResourceStructure toscaResourceStructure = mock(ToscaResourceStructure.class);
        when(toscaInstaller.isCsarAlreadyDeployed(toscaResourceStructure))
                .thenThrow(new ArtifactInstallerException("checksum mismatch"));

        resourceInstaller.isCsarAlreadyDeployed(client, notif, toscaResourceStructure);

        verify(statusSender).sendDeployNotificationsForResource(eq(client), any(),
                eq(DistributionStatusEnum.DEPLOY_ERROR), eq("checksum mismatch"));
    }

    private static ResourceInfoImpl resource(String uuid) {
        ResourceInfoImpl resource = new ResourceInfoImpl();
        resource.setResourceUUID(uuid);
        return resource;
    }
}
