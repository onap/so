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

import java.io.IOException;
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

import org.openecomp.sdc.api.notification.IArtifactInfo;
import org.openecomp.sdc.api.notification.INotificationData;
import org.openecomp.sdc.api.notification.IResourceInstance;
import org.openecomp.sdc.api.results.IDistributionClientDownloadResult;
import org.openecomp.sdc.api.results.IDistributionClientResult;
import org.openecomp.sdc.utils.DistributionActionResultEnum;
import org.openecomp.mso.asdc.client.ASDCConfiguration;
import org.openecomp.mso.asdc.client.ASDCGlobalController;
import org.openecomp.mso.asdc.client.exceptions.ASDCControllerException;
import org.openecomp.mso.asdc.client.exceptions.ASDCParametersException;
import org.openecomp.mso.asdc.client.exceptions.ArtifactInstallerException;
import org.openecomp.mso.properties.MsoJavaProperties;
import org.openecomp.mso.properties.MsoPropertiesException;
import org.openecomp.mso.properties.MsoPropertiesFactory;


/**
 * THis class tests the ASDC Controller by using the ASDC Mock CLient
 */
public class ASDCGlobalControllerTest {

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

    public static final String ASDC_PROP = MsoJavaProperties.class.getClassLoader().getResource("mso.json").toString().substring(5);
    public static final String ASDC_PROP2 = MsoJavaProperties.class.getClassLoader().getResource("mso2.json").toString().substring(5);
    public static final String ASDC_PROP3 = MsoJavaProperties.class.getClassLoader().getResource("mso3.json").toString().substring(5);
    public static final String ASDC_PROP_BAD = MsoJavaProperties.class.getClassLoader().getResource("mso-bad.json").toString().substring(5);
    public static final String ASDC_PROP_WITH_NULL = MsoJavaProperties.class.getClassLoader().getResource("mso-with-NULL.json").toString().substring(5);
    public static final String ASDC_PROP_WITH_DOUBLE = MsoJavaProperties.class.getClassLoader().getResource("mso-two-configs.json").toString().substring(5);
    public static final String ASDC_PROP_WITH_DOUBLE2 = MsoJavaProperties.class.getClassLoader().getResource("mso-two-configs2.json").toString().substring(5);

    @BeforeClass
    public static final void prepareMockNotification() throws MsoPropertiesException, IOException, URISyntaxException, NoSuchAlgorithmException, ArtifactInstallerException {

        heatExample = new String(Files.readAllBytes(Paths.get(ASDCGlobalControllerTest.class.getClassLoader().getResource("resource-examples/autoscaling.yaml").toURI())));
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] md5Hash = md.digest(heatExample.getBytes());
        heatExampleMD5HashBase64 = Base64.encodeBase64String(md5Hash);

        iNotif = Mockito.mock(INotificationData.class);

        // Create fake ArtifactInfo
        artifactInfo1 = Mockito.mock(IArtifactInfo.class);
        Mockito.when(artifactInfo1.getArtifactChecksum()).thenReturn(ASDCGlobalControllerTest.heatExampleMD5HashBase64);

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
    public final void testUpdateControllersConfigIfNeeded() throws ASDCControllerException, ASDCParametersException, IOException, MsoPropertiesException {

        ASDCGlobalController asdcGlobalController = new ASDCGlobalController();
        assertTrue(asdcGlobalController.getControllers().size() == 0);

        // first init
        assertTrue(asdcGlobalController.updateControllersConfigIfNeeded());
        assertTrue(asdcGlobalController.getControllers().size() == 1);
        assertTrue(asdcGlobalController.getControllers().get("asdc-controller1") != null);

        // Add a second one
        msoPropertiesFactory.removeAllMsoProperties();
        msoPropertiesFactory.initializeMsoProperties(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP_WITH_DOUBLE);
        assertTrue(asdcGlobalController.updateControllersConfigIfNeeded());
        assertTrue(asdcGlobalController.getControllers().size() == 2);
        assertTrue(asdcGlobalController.getControllers().get("asdc-controller1") != null);
        assertTrue(asdcGlobalController.getControllers().get("asdc-controller2") != null);
        // Check that update does nothing
        assertFalse(asdcGlobalController.updateControllersConfigIfNeeded());
        assertTrue(asdcGlobalController.getControllers().size() == 2);

        // Change the second one name
        msoPropertiesFactory.removeAllMsoProperties();
        msoPropertiesFactory.initializeMsoProperties(ASDCConfiguration.MSO_PROP_ASDC, ASDC_PROP_WITH_DOUBLE2);
        assertTrue(asdcGlobalController.updateControllersConfigIfNeeded());
        assertTrue(asdcGlobalController.getControllers().size() == 2);
        assertTrue(asdcGlobalController.getControllers().get("asdc-controller1") != null);
        assertTrue(asdcGlobalController.getControllers().get("asdc-controller2B") != null);


    }

    @Test
    public final void testCloseASDC() {

        ASDCGlobalController asdcGlobalController = new ASDCGlobalController();
        assertTrue(asdcGlobalController.getControllers().size() == 0);

        // first init
        assertTrue(asdcGlobalController.updateControllersConfigIfNeeded());
        assertTrue(asdcGlobalController.getControllers().size() == 1);
        assertTrue(asdcGlobalController.getControllers().get("asdc-controller1") != null);

        asdcGlobalController.closeASDC();
        assertTrue(asdcGlobalController.getControllers().size() == 0);


    }

}
