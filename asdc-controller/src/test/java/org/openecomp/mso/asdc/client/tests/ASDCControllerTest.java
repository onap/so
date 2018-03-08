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

package org.openecomp.mso.asdc.client.tests;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.openecomp.sdc.api.IDistributionClient;
import org.openecomp.sdc.api.consumer.INotificationCallback;
import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.api.results.IDistributionClientResult;
import org.openecomp.sdc.impl.mock.DistributionClientStubImpl;
import org.openecomp.sdc.utils.DistributionActionResultEnum;
import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.asdc.client.ASDCController;
import org.openecomp.mso.asdc.client.ASDCControllerStatus;
import org.openecomp.mso.asdc.client.exceptions.ASDCControllerException;
import org.openecomp.mso.asdc.client.exceptions.ASDCParametersException;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.asdc.installer.heat.VfResourceInstaller;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;


/**
 * THis class tests the ASDC Controller by using the ASDC Mock CLient
 */
public class ASDCControllerTest {

    private static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();

    private static String heatExample;
    private static String heatExampleMD5HashBase64;

    private static INotificationData iNotif;

    private static IDistributionClientDownloadResult downloadResult;
    private static IDistributionClientDownloadResult downloadCorruptedResult;

    private static IDistributionClientResult successfulClientInitResult;
    private static IDistributionClientResult unsuccessfulClientInitResult;

    private static IArtifactInfo artifactInfo1;

    private static IResourceInstance resource1;

    private static VfResourceInstaller vnfInstaller;

    public static final String ASDC_PROP = MsoJavaProperties.class.getClassLoader().getResource("mso.json").toString().substring(5);
    public static final String ASDC_PROP2 = MsoJavaProperties.class.getClassLoader().getResource("mso2.json").toString().substring(5);
    public static final String ASDC_PROP3 = MsoJavaProperties.class.getClassLoader().getResource("mso3.json").toString().substring(5);
    public static final String ASDC_PROP_BAD = MsoJavaProperties.class.getClassLoader().getResource("mso-bad.json").toString().substring(5);
    public static final String ASDC_PROP_WITH_NULL = MsoJavaProperties.class.getClassLoader().getResource("mso-with-NULL.json").toString().substring(5);

    @BeforeClass
    public static final void prepareMockNotification() throws MsoPropertiesException, IOException, URISyntaxException, NoSuchAlgorithmException, ArtifactInstallerException {

        heatExample = new String(Files.readAllBytes(Paths.get(ASDCControllerTest.class.getClassLoader().getResource("resource-examples/autoscaling.yaml").toURI())));
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md5Hash = md.digest(heatExample.getBytes());
        heatExampleMD5HashBase64 = Base64.encodeBase64String(md5Hash);

        iNotif = Mockito.mock(INotificationData.class);

        // Create fake ArtifactInfo
        artifactInfo1 = Mockito.mock(IArtifactInfo.class);
        Mockito.when(artifactInfo1.getArtifactChecksum()).thenReturn(ASDCControllerTest.heatExampleMD5HashBase64);

        Mockito.when(artifactInfo1.getArtifactName()).thenReturn("artifact1");
        Mockito.when(artifactInfo1.getArtifactType()).thenReturn(ASDCConfiguration.HEAT);
        Mockito.when(artifactInfo1.getArtifactURL()).thenReturn("https://localhost:8080/v1/catalog/services/srv1/2.0/resources/aaa/1.0/artifacts/aaa.yml");
        Mockito.when(artifactInfo1.getArtifactUUID()).thenReturn("UUID1");
        Mockito.when(artifactInfo1.getArtifactDescription()).thenReturn("testos artifact1");

        // Now provision the NotificationData mock
        List<IArtifactInfo> listArtifact = new ArrayList<>();
        listArtifact.add(artifactInfo1);

        // Create fake resource Instance
        resource1 = Mockito.mock(IResourceInstance.class);
        Mockito.when(resource1.getResourceType()).thenReturn("VF");
        Mockito.when(resource1.getResourceName()).thenReturn("resourceName");
        Mockito.when(resource1.getArtifacts()).thenReturn(listArtifact);

        List<IResourceInstance> resources = new ArrayList<>();
        resources.add(resource1);

        Mockito.when(iNotif.getResources()).thenReturn(resources);
        Mockito.when(iNotif.getDistributionID()).thenReturn("distributionID1");
        Mockito.when(iNotif.getServiceName()).thenReturn("serviceName1");
        Mockito.when(iNotif.getServiceUUID()).thenReturn("serviceNameUUID1");
        Mockito.when(iNotif.getServiceVersion()).thenReturn("1.0");

        downloadResult = Mockito.mock(IDistributionClientDownloadResult.class);
        Mockito.when(downloadResult.getArtifactPayload()).thenReturn(heatExample.getBytes());
        Mockito.when(downloadResult.getDistributionActionResult()).thenReturn(DistributionActionResultEnum.SUCCESS);
        Mockito.when(downloadResult.getDistributionMessageResult()).thenReturn("Success");

        downloadCorruptedResult = Mockito.mock(IDistributionClientDownloadResult.class);
        Mockito.when(downloadCorruptedResult.getArtifactPayload()).thenReturn((heatExample + "badone").getBytes());
        Mockito.when(downloadCorruptedResult.getDistributionActionResult()).thenReturn(DistributionActionResultEnum.SUCCESS);
        Mockito.when(downloadCorruptedResult.getDistributionMessageResult()).thenReturn("Success");

        vnfInstaller = Mockito.mock(VfResourceInstaller.class);

        // Mock now the ASDC distribution client behavior
        successfulClientInitResult = Mockito.mock(IDistributionClientResult.class);
        Mockito.when(successfulClientInitResult.getDistributionActionResult()).thenReturn(DistributionActionResultEnum.SUCCESS);

        unsuccessfulClientInitResult = Mockito.mock(IDistributionClientResult.class);
        Mockito.when(unsuccessfulClientInitResult.getDistributionActionResult()).thenReturn(DistributionActionResultEnum.GENERAL_ERROR);

    }

    @Before
    public final void initBeforeEachTest() throws MsoPropertiesException {
        // load the config
        msoPropertiesFactory.removeAllMsoProperties();
        msoPropertiesFactory.initializeMsoProperties(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP);
    }

    @AfterClass
    public static final void kill() throws MsoPropertiesException {

        msoPropertiesFactory.removeMsoProperties(ASDCConfiguration.MSO_PROP_ASDC);

    }

    @Test
    public final void testTheInitWithASDCStub() throws ASDCControllerException, ASDCParametersException, IOException {

        ASDCController asdcController = new ASDCController("asdc-controller1", new DistributionClientStubImpl());
        asdcController.initASDC();
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.IDLE);
        assertTrue(asdcController.getNbOfNotificationsOngoing() == 0);
    }

    @Test
    public final void testTheNotificationWithASDCStub() throws ASDCControllerException, ASDCParametersException, IOException {

        ASDCController asdcController = new ASDCController("asdc-controller1", new DistributionClientStubImpl(), vnfInstaller);
        asdcController.initASDC();
        // try to send a notif, this should fail internally, we just want to ensure that in case of crash, controller status goes to IDLE
        asdcController.treatNotification(iNotif);

        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.IDLE);
        assertTrue(asdcController.getNbOfNotificationsOngoing() == 0);

    }

    @Test
    public final void testASecondInit() throws ASDCControllerException, ASDCParametersException, IOException {
        ASDCController asdcController = new ASDCController("asdc-controller1", new DistributionClientStubImpl(), vnfInstaller);
        asdcController.initASDC();
        // try to send a notif, this should fail internally, we just want to ensure that in case of crash, controller status goes to IDLE

        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.IDLE);
        assertTrue(asdcController.getNbOfNotificationsOngoing() == 0);

        try {
            asdcController.initASDC();
            fail("ASDCControllerException should have been raised for the init");
        } catch (ASDCControllerException e) {
            assertTrue("The controller is already initialized, call the closeASDC method first".equals(e.getMessage()));
        }

        // No changes expected on the controller state
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.IDLE);
        assertTrue(asdcController.getNbOfNotificationsOngoing() == 0);
    }

    @Test
    public final void testInitCrashWithMockitoClient() throws ASDCParametersException, IOException {

        IDistributionClient distributionClient;
        // First case for init method
        distributionClient = Mockito.mock(IDistributionClient.class);
        Mockito.when(distributionClient.download(artifactInfo1)).thenThrow(new RuntimeException("ASDC Client not initialized"));
        Mockito.when(distributionClient.init(any(ASDCConfiguration.class), any(INotificationCallback.class))).thenReturn(unsuccessfulClientInitResult);
        Mockito.when(distributionClient.start()).thenReturn(unsuccessfulClientInitResult);

        ASDCController asdcController = new ASDCController("asdc-controller1", distributionClient, vnfInstaller);

        // This should return an exception
        try {
            asdcController.initASDC();
            fail("ASDCControllerException should have been raised for the init");
        } catch (ASDCControllerException e) {
            assertTrue("Initialization of the ASDC Controller failed with reason: null".equals(e.getMessage()));
        }

        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.STOPPED);
        assertTrue(asdcController.getNbOfNotificationsOngoing() == 0);

        // Second case for start method

        Mockito.when(distributionClient.init(any(ASDCConfiguration.class), any(INotificationCallback.class))).thenReturn(successfulClientInitResult);
        Mockito.when(distributionClient.start()).thenReturn(unsuccessfulClientInitResult);

        // This should return an exception
        try {
            asdcController.initASDC();
            fail("ASDCControllerException should have been raised for the init");
        } catch (ASDCControllerException e) {
            assertTrue("Startup of the ASDC Controller failed with reason: null".equals(e.getMessage()));
        }

        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.STOPPED);
        assertTrue(asdcController.getNbOfNotificationsOngoing() == 0);
    }

    @Test
    public final void testTheStop() throws ASDCControllerException, ASDCParametersException, IOException {

        ASDCController asdcController = new ASDCController("asdc-controller1", new DistributionClientStubImpl(), vnfInstaller);

        asdcController.closeASDC();
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.STOPPED);


        asdcController = new ASDCController("asdc-controller1", new DistributionClientStubImpl(), vnfInstaller);
        asdcController.initASDC();
        asdcController.closeASDC();
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.STOPPED);
    }

    @Test
    public final void testConfigRefresh() throws ASDCParametersException, ASDCControllerException, IOException, MsoPropertiesException {
        IDistributionClient distributionClient;
        distributionClient = Mockito.mock(IDistributionClient.class);
        Mockito.when(distributionClient.download(artifactInfo1)).thenReturn(downloadResult);
        Mockito.when(distributionClient.init(any(ASDCConfiguration.class), any(INotificationCallback.class))).thenReturn(successfulClientInitResult);
        Mockito.when(distributionClient.start()).thenReturn(successfulClientInitResult);


        ASDCController asdcController = new ASDCController("asdc-controller1", distributionClient, vnfInstaller);

        // it should not raise any exception even if controller is not yet initialized
        asdcController.updateConfigIfNeeded();

        asdcController.initASDC();
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.IDLE);
        assertFalse(asdcController.updateConfigIfNeeded());

        msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP3);
        msoPropertiesFactory.reloadMsoProperties();
        // It should fail if it tries to refresh the config as the init will now fail
        assertTrue(asdcController.updateConfigIfNeeded());
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.IDLE);


        msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP);
        msoPropertiesFactory.reloadMsoProperties();
    }

    @Test
    public final void testConfigRefreshWhenBusy() throws IOException, MsoPropertiesException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ASDCParametersException, ASDCControllerException {
        IDistributionClient distributionClient;
        distributionClient = Mockito.mock(IDistributionClient.class);
        Mockito.when(distributionClient.download(artifactInfo1)).thenReturn(downloadResult);
        Mockito.when(distributionClient.init(any(ASDCConfiguration.class), any(INotificationCallback.class))).thenReturn(successfulClientInitResult);
        Mockito.when(distributionClient.start()).thenReturn(successfulClientInitResult);

        ASDCController asdcController = new ASDCController("asdc-controller1", distributionClient, vnfInstaller);

        // it should not raise any exception even if controller is not yet initialized
        asdcController.updateConfigIfNeeded();

        asdcController.initASDC();
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.IDLE);
        assertFalse(asdcController.updateConfigIfNeeded());

        // Simulate a BUSY case by reflection
        Field controllerStatus;
        controllerStatus = ASDCController.class.getDeclaredField("controllerStatus");
        controllerStatus.setAccessible(true);
        controllerStatus.set(asdcController, ASDCControllerStatus.BUSY);


        msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP3);
        msoPropertiesFactory.reloadMsoProperties();
        // It should fail if it tries to refresh the config as the init will now fail
        try {
            asdcController.updateConfigIfNeeded();
            fail("ASDCControllerException should have been raised");
        } catch (ASDCControllerException e) {
            assertTrue("Cannot close the ASDC controller as it's currently in BUSY state".equals(e.getMessage()));
        }

        // Try it a second time to see if we still see the changes
        try {
            asdcController.updateConfigIfNeeded();
            fail("ASDCControllerException should have been raised");
        } catch (ASDCControllerException e) {
            assertTrue("Cannot close the ASDC controller as it's currently in BUSY state".equals(e.getMessage()));
        }

        // Revert to Idle by reflection
        controllerStatus.set(asdcController, ASDCControllerStatus.IDLE);
        controllerStatus.setAccessible(false);

        // This should work now, controller should be restarted
        assertTrue(asdcController.updateConfigIfNeeded());
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.IDLE);

        msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP);
        msoPropertiesFactory.reloadMsoProperties();
    }


    @Test
    public final void testBadConfigRefresh() throws ASDCParametersException, ASDCControllerException, IOException, MsoPropertiesException {
        IDistributionClient distributionClient;
        distributionClient = Mockito.mock(IDistributionClient.class);
        Mockito.when(distributionClient.download(artifactInfo1)).thenReturn(downloadResult);
        Mockito.when(distributionClient.init(any(ASDCConfiguration.class), any(INotificationCallback.class))).thenReturn(successfulClientInitResult);
        Mockito.when(distributionClient.start()).thenReturn(successfulClientInitResult);


        ASDCController asdcController = new ASDCController("asdc-controller1", distributionClient, vnfInstaller);

        // it should not raise any exception even if controller is not yet initialized
        asdcController.updateConfigIfNeeded();

        asdcController.initASDC();
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.IDLE);
        assertFalse(asdcController.updateConfigIfNeeded());

        msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP_BAD);
        msoPropertiesFactory.reloadMsoProperties();
        // It should fail if it tries to refresh the config as the init will now fail
        try {
            asdcController.updateConfigIfNeeded();
            fail("ASDCParametersException should have been raised");
        } catch (ASDCParametersException ep) {
            assertTrue("consumerGroup parameter cannot be found in config mso.properties".equals(ep.getMessage()));
        }

        // This should stop the controller, as it can't work with a bad config file
        assertTrue(asdcController.getControllerStatus() == ASDCControllerStatus.STOPPED);


        msoPropertiesFactory.changeMsoPropertiesFilePath(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP);
        msoPropertiesFactory.reloadMsoProperties();
    }

    @Test
    public final void testConfigAccess() throws ASDCControllerException, ASDCParametersException, IOException {
        IDistributionClient distributionClient;
        distributionClient = Mockito.mock(IDistributionClient.class);
        Mockito.when(distributionClient.download(artifactInfo1)).thenReturn(downloadResult);
        Mockito.when(distributionClient.init(any(ASDCConfiguration.class), any(INotificationCallback.class))).thenReturn(successfulClientInitResult);
        Mockito.when(distributionClient.start()).thenReturn(successfulClientInitResult);


        ASDCController asdcController = new ASDCController("asdc-controller1", distributionClient, vnfInstaller);

        assertTrue("Unknown".equals(asdcController.getAddress()));
        assertTrue("Unknown".equals(asdcController.getEnvironment()));

        asdcController.initASDC();

        assertTrue("hostname".equals(asdcController.getAddress()));
        assertTrue("environmentName".equals(asdcController.getEnvironment()));

    }

}
