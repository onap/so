/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.cloudify.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.cloudify.beans.DeploymentInfo;
import org.openecomp.mso.cloudify.exceptions.MsoCloudifyManagerNotFound;
import org.openecomp.mso.cloudify.v3.client.Cloudify;
import org.openecomp.mso.cloudify.v3.model.DeploymentOutputs;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.mock;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

//@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoCloudifyUtils.class})


public class MsoCloudifyUtilsTest {


    @Mock
    MsoPropertiesFactory msoPropertiesFactory;

    @Mock
    CloudConfigFactory cloudConfigFactory;

    @Mock
    DeploymentInfo deploymentInfo;

    @Mock
    Cloudify cloudify;

    @Mock
    DeploymentOutputs deploymentOutputs;

    @Mock
    CloudSite cloudSite;

    @Test(expected = NullPointerException.class)
    public void testCreateandInstallDeployment() throws MsoException {

        MsoCloudifyUtils mcu = new MsoCloudifyUtils("msoPropID", msoPropertiesFactory, cloudConfigFactory);
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("1", "value");

        mcu.createAndInstallDeployment("cloudSiteId", "tenantId", "deploymentId", "blueprintId"
                , inputs, true, 1, true);

        assert (mcu.createAndInstallDeployment("cloudSiteId", "tenantId", "deploymentId", "blueprintId"
                , inputs, true, 1, true) != null);


    }

    @Test(expected = NullPointerException.class)
    public void testDeploymentOutputs() throws MsoException {

        MsoCloudifyUtils mcu = new MsoCloudifyUtils("msoPropID", msoPropertiesFactory, cloudConfigFactory);
        mcu.queryDeployment("cloudSiteId", "tenantId", "deploymentId");
        assert (mcu.queryDeployment("cloudSiteId", "tenantId", "deploymentId") != null);
    }

    @Test(expected = NullPointerException.class)
    public void testUninstallAndDeleteDeployment() throws MsoException {

        MsoCloudifyUtils mcu = new MsoCloudifyUtils("msoPropID", msoPropertiesFactory, cloudConfigFactory);
        mcu.uninstallAndDeleteDeployment("cloudSiteId", "tenantId", "deploymentId", 1);
        assert (mcu.uninstallAndDeleteDeployment("cloudSiteId", "tenantId", "deploymentId", 1) != null);
    }

    @Test(expected = NullPointerException.class)
    public void testIsBlueprintLoaded() throws MsoException {

        MsoCloudifyUtils mcu = new MsoCloudifyUtils("msoPropID", msoPropertiesFactory, cloudConfigFactory);
        mcu.isBlueprintLoaded("cloudSiteId", "blueprintId");
        assertTrue(mcu.isBlueprintLoaded("cloudSiteId", "blueprintId"));
    }

    @Test(expected = MsoCloudifyManagerNotFound.class)
    public void testCloudifyClient() throws MsoException {
    	msoPropertiesFactory = mock(MsoPropertiesFactory.class);
    	cloudConfigFactory = mock(CloudConfigFactory.class);
    	cloudSite = mock(CloudSite.class);
        MsoCloudifyUtils mcu = new MsoCloudifyUtils("msoPropID", msoPropertiesFactory, cloudConfigFactory);
        mcu.getCloudifyClient(cloudSite);
        assert (mcu.getCloudifyClient(cloudSite) != null);

    }

    @Test(expected = NullPointerException.class)
    public void testuploadBlueprint() throws MsoException {

            MsoCloudifyUtils mcu = new MsoCloudifyUtils("msoPropID", msoPropertiesFactory, cloudConfigFactory);

            Map<String, byte[]> blueprintFiles = new HashMap<String, byte[]>();
            byte[] byteArray = new byte[]{8, 1, 2, 8};
            blueprintFiles.put("1", byteArray);

            mcu.uploadBlueprint("cloudSiteId", "blueprintId", "mainFileName", blueprintFiles, false);

    }

    @Test(expected = NullPointerException.class)
    public void testqueryDeployment() throws MsoException {

        MsoCloudifyUtils mcu = new MsoCloudifyUtils("msoPropID", msoPropertiesFactory, cloudConfigFactory);
        mcu.queryDeployment(cloudify, "deploymentId");
        assert (mcu.queryDeployment(cloudify, "deploymentId") != null);


    }

}
