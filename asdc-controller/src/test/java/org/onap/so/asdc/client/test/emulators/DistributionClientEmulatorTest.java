/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.client.test.emulators;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.api.consumer.IDistributionStatusMessage;
import org.onap.sdc.api.results.IDistributionClientDownloadResult;
import org.onap.sdc.api.results.IDistributionClientResult;
import org.onap.sdc.impl.DistributionClientDownloadResultImpl;
import org.onap.sdc.impl.DistributionClientResultImpl;
import org.onap.sdc.utils.DistributionActionResultEnum;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.client.ASDCConfiguration;
import org.onap.so.asdc.client.ASDCNotificationCallBack;
import org.onap.so.asdc.client.ASDCStatusCallBack;
import org.onap.so.asdc.client.DistributionStatusMessage;

public class DistributionClientEmulatorTest {

    private DistributionClientEmulator distClientEmulator;

    @Before
    public void before() {
        distClientEmulator = new DistributionClientEmulator();
    }

    @Test
    public void getDistributionMessageReceived() {
        List<IDistributionStatusMessage> receivedMessages = distClientEmulator.getDistributionMessageReceived();
        assertEquals(new LinkedList<>(), receivedMessages);

        IDistributionStatusMessage message = new DistributionStatusMessage("testArtifactUrl", "testConsumerId",
                "testDistributionId", DistributionStatusEnum.DOWNLOAD_OK, 123456);
        distClientEmulator.sendDeploymentStatus(message);
        assertEquals(message, receivedMessages.get(0));

        IDistributionStatusMessage message2 = new DistributionStatusMessage("testArtifactUrl2", "testConsumerId2",
                "testDistributionId2", DistributionStatusEnum.DOWNLOAD_OK, 1234567);
        distClientEmulator.sendDeploymentStatus(message2);
        assertEquals(message2, receivedMessages.get(1));
    }

    @Test
    public void sendDeploymentStatusPrimary() {
        IDistributionStatusMessage message = new DistributionStatusMessage("testArtifactUrl", "testConsumerId",
                "testDistributionId", DistributionStatusEnum.DOWNLOAD_OK, 123456);
        IDistributionClientResult result = distClientEmulator.sendDeploymentStatus(message);
        IDistributionClientResult expectedResult = new DistributionClientResultImpl(
                DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name());
        assertEquals(message, distClientEmulator.getDistributionMessageReceived().get(0));
        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }

    @Test
    public void sendDeploymentStatusSecondary() {
        IDistributionStatusMessage message = new DistributionStatusMessage("testArtifactUrl", "testConsumerId",
                "testDistributionId", DistributionStatusEnum.DOWNLOAD_OK, 123456);
        IDistributionClientResult result = distClientEmulator.sendDeploymentStatus(message, "test");
        IDistributionClientResult expectedResult = new DistributionClientResultImpl(
                DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name());
        assertEquals(message, distClientEmulator.getDistributionMessageReceived().get(0));
        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }

    @Test
    public void sendDownloadStatusPrimary() {
        IDistributionStatusMessage message = new DistributionStatusMessage("testArtifactUrl", "testConsumerId",
                "testDistributionId", DistributionStatusEnum.DOWNLOAD_OK, 123456);
        IDistributionClientResult result = distClientEmulator.sendDownloadStatus(message);
        IDistributionClientResult expectedResult = new DistributionClientResultImpl(
                DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name());
        assertEquals(message, distClientEmulator.getDistributionMessageReceived().get(0));
        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }

    @Test
    public void sendDownloadStatusSecondary() {
        IDistributionStatusMessage message = new DistributionStatusMessage("testArtifactUrl", "testConsumerId",
                "testDistributionId", DistributionStatusEnum.DOWNLOAD_OK, 123456);
        IDistributionClientResult result = distClientEmulator.sendDownloadStatus(message, "test");
        IDistributionClientResult expectedResult = new DistributionClientResultImpl(
                DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name());
        assertEquals(message, distClientEmulator.getDistributionMessageReceived().get(0));
        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }

    @Test
    public void initPrimary() {
        IDistributionClientResult result =
                distClientEmulator.init(new ASDCConfiguration(), new ASDCNotificationCallBack());
        IDistributionClientResult expectedResult = new DistributionClientResultImpl(
                DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name());
        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }

    @Test
    public void initSecondary() {
        IDistributionClientResult result = distClientEmulator.init(new ASDCConfiguration(),
                new ASDCNotificationCallBack(), new ASDCStatusCallBack());
        IDistributionClientResult expectedResult = new DistributionClientResultImpl(
                DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name());
        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }

    @Test
    public void start() {
        IDistributionClientResult result = distClientEmulator.start();
        IDistributionClientResult expectedResult = new DistributionClientResultImpl(
                DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name());
        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }

    @Test
    public void stop() {
        IDistributionClientResult result = distClientEmulator.stop();
        IDistributionClientResult expectedResult = new DistributionClientResultImpl(
                DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name());
        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }

    @Test
    public void updateConfiguration() {
        IDistributionClientResult result = distClientEmulator.updateConfiguration(new ASDCConfiguration());
        IDistributionClientResult expectedResult = new DistributionClientResultImpl(
                DistributionActionResultEnum.SUCCESS, DistributionActionResultEnum.SUCCESS.name());
        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }

    @Test
    public void getResourcePath() {
        String testResourcePath = "testResourcePath";
        distClientEmulator = new DistributionClientEmulator(testResourcePath);
        assertEquals(testResourcePath, distClientEmulator.getResourcePath());
    }

    @Test
    public void setResourcePath() {
        String testResourcePath = "testResourcePath";
        distClientEmulator.setResourcePath(testResourcePath);
        assertEquals(testResourcePath, distClientEmulator.getResourcePath());
    }

    @Test
    public void downloadSuccess() throws IOException {
        ArtifactInfoImpl info = new ArtifactInfoImpl();
        info.setArtifactURL("mso.json");
        info.setArtifactName("testArtifactName");

        distClientEmulator.setResourcePath("src/test/resources/");

        IDistributionClientDownloadResult result = distClientEmulator.download(info);

        byte[] expectedInputStream =
                Files.readAllBytes(Paths.get(distClientEmulator.getResourcePath() + info.getArtifactURL()));
        IDistributionClientDownloadResult expectedResult =
                new DistributionClientDownloadResultImpl(DistributionActionResultEnum.SUCCESS,
                        DistributionActionResultEnum.SUCCESS.name(), info.getArtifactName(), expectedInputStream);

        assertEquals(expectedResult.getDistributionActionResult(), result.getDistributionActionResult());
        assertEquals(expectedResult.getDistributionMessageResult(), result.getDistributionMessageResult());
    }
}
