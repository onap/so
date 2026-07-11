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

import static org.mockito.Mockito.*;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdc.api.IDistributionClient;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;
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
}
