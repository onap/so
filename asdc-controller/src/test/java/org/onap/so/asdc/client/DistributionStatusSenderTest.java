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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.consumer.IFinalDistrStatusMessage;
import org.onap.sdc.utils.DistributionStatusEnum;

@RunWith(MockitoJUnitRunner.class)
public class DistributionStatusSenderTest {

    @Mock
    private ASDCConfiguration asdcConfig;

    @Mock
    private IDistributionClient client;

    @InjectMocks
    private DistributionStatusSender sender;

    @Before
    public void setUp() {
        when(asdcConfig.getConsumerID()).thenReturn("consumer-1");
    }

    @Test
    public void sendASDCNotification_download_withoutError_callsSendDownloadStatus() {
        sender.sendASDCNotification(client, DistributionStatusSender.NotificationType.DOWNLOAD, "http://artifact",
                "dist-1", DistributionStatusEnum.DOWNLOAD_OK, null, 123L);
        verify(client).sendDownloadStatus(any(IDistributionStatusMessage.class));
        verify(client, never()).sendDownloadStatus(any(), anyString());
    }

    @Test
    public void sendASDCNotification_deploy_withError_callsSendDeploymentStatusWithReason() {
        sender.sendASDCNotification(client, DistributionStatusSender.NotificationType.DEPLOY, "http://artifact",
                "dist-1", DistributionStatusEnum.DEPLOY_ERROR, "boom", 123L);
        verify(client).sendDeploymentStatus(any(IDistributionStatusMessage.class), eq("boom"));
    }

    @Test
    public void sendASDCNotification_runtimeExceptionFromClient_isSwallowed() {
        doThrow(new RuntimeException("network")).when(client).sendDownloadStatus(any(IDistributionStatusMessage.class));
        // must not propagate
        sender.sendASDCNotification(client, DistributionStatusSender.NotificationType.DOWNLOAD, "http://artifact",
                "dist-1", DistributionStatusEnum.DOWNLOAD_OK, null, 123L);
    }

    @Test
    public void sendFinalDistributionStatus_withoutError_callsSendFinalDistrStatus() {
        sender.sendFinalDistributionStatus(client, "dist-1", DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK, null);
        verify(client).sendFinalDistrStatus(any(IFinalDistrStatusMessage.class));
    }

    @Test
    public void sendFinalDistributionStatus_withError_callsSendFinalDistrStatusWithReason() {
        sender.sendFinalDistributionStatus(client, "dist-1", DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR, "bad");
        verify(client).sendFinalDistrStatus(any(IFinalDistrStatusMessage.class), eq("bad"));
    }
}
