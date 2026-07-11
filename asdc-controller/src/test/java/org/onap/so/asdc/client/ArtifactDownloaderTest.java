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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdc.api.IDistributionClient;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.exceptions.ASDCDownloadException;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactDownloaderTest {

    @Mock
    private ASDCConfiguration asdcConfig;
    @Mock
    private DistributionStatusSender statusSender;
    @Mock
    private IDistributionClient client;
    @Mock
    private IArtifactInfo artifact;
    @Mock
    private IDistributionClientDownloadResult downloadResult;

    @InjectMocks
    private ArtifactDownloader downloader;

    @Before
    public void setUp() {
        when(artifact.getArtifactUUID()).thenReturn("art-1");
        when(artifact.getArtifactURL()).thenReturn("http://artifact");
        when(artifact.getArtifactName()).thenReturn("artifact.csar");
    }

    @Test
    public void downloadTheArtifact_success_returnsResultAndSendsDownloadOk() throws Exception {
        when(client.download(artifact)).thenReturn(downloadResult);
        when(downloadResult.getDistributionActionResult()).thenReturn(DistributionActionResultEnum.SUCCESS);
        when(downloadResult.getArtifactPayload()).thenReturn(new byte[] {1, 2, 3});

        IDistributionClientDownloadResult result = downloader.downloadTheArtifact(client, artifact, "dist-1");

        assertSame(downloadResult, result);
        verify(statusSender).sendASDCNotification(eq(client), eq(DistributionStatusSender.NotificationType.DOWNLOAD),
                anyString(), eq("dist-1"), eq(DistributionStatusEnum.DOWNLOAD_OK), isNull(), anyLong());
    }

    @Test
    public void downloadTheArtifact_nullResult_returnsNull() throws Exception {
        when(client.download(artifact)).thenReturn(null);
        assertNull(downloader.downloadTheArtifact(client, artifact, "dist-1"));
    }

    @Test(expected = ASDCDownloadException.class)
    public void downloadTheArtifact_runtimeException_throwsAndSendsError() throws Exception {
        when(client.download(artifact)).thenThrow(new RuntimeException("net"));
        try {
            downloader.downloadTheArtifact(client, artifact, "dist-1");
        } finally {
            verify(statusSender).sendASDCNotification(eq(client),
                    eq(DistributionStatusSender.NotificationType.DOWNLOAD), anyString(), eq("dist-1"),
                    eq(DistributionStatusEnum.DOWNLOAD_ERROR), anyString(), anyLong());
        }
    }

    @Test(expected = ASDCDownloadException.class)
    public void downloadTheArtifact_actionResultNotSuccess_throws() throws Exception {
        when(client.download(artifact)).thenReturn(downloadResult);
        when(downloadResult.getDistributionActionResult()).thenReturn(DistributionActionResultEnum.FAIL);
        downloader.downloadTheArtifact(client, artifact, "dist-1");
    }
}
