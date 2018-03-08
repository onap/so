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

package org.openecomp.mso.adapter_utils.tests;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudIdentity.IdentityServerType;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.openstack.exceptions.MsoCloudSiteNotFound;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoIOException;
import org.openecomp.mso.openstack.utils.MsoHeatUtilsWithUpdate;
import org.openecomp.mso.properties.MsoPropertiesFactory;

import com.woorea.openstack.base.client.OpenStackConnectException;

@RunWith(MockitoJUnitRunner.class)
public class MsoHeatUtilsWithUpdateTest {

    public static MsoPropertiesFactory msoPropertiesFactory = new MsoPropertiesFactory();
    public static CloudConfigFactory cloudConfigFactory = new CloudConfigFactory();

    @Mock
    CloudConfig cloudConfig;
    @InjectMocks
    MsoHeatUtilsWithUpdate util = new MsoHeatUtilsWithUpdate("NO_PROP", msoPropertiesFactory, cloudConfigFactory);

    private CloudSite cloudSite;

    @Before
    public void init() {
        cloudSite = new CloudSite();
        cloudSite.setId("cloud");
        CloudIdentity cloudIdentity = new CloudIdentity();
        cloudIdentity.setIdentityServerType(IdentityServerType.KEYSTONE);
        cloudIdentity.setKeystoneUrl("toto");
        cloudIdentity.setMsoPass(CloudIdentity.encryptPassword("mockId"));
        cloudSite.setIdentityService(cloudIdentity);
        when(cloudConfig.getCloudSite("cloud")).thenReturn(Optional.of(cloudSite));
        when(cloudConfig.getCloudSite("none")).thenReturn(Optional.empty());
    }

    @Test
    @Ignore
    public void testUpdateStack() {
        // Heat heat = Mockito.mock (Heat.class);
        Map<String, Object> stackInputs = new HashMap<>();
        try {
            util.updateStack("none", "tenantId", "stackName", "heatTemplate", stackInputs, false, 1);
        } catch (MsoException e) {
            if (e instanceof MsoCloudSiteNotFound) {
                // Ok
            } else {
                e.printStackTrace();
                fail("Exception caught");
            }
        }
        try {
            util.updateStack("cloud", "tenantId", "stackName", "heatTemplate", stackInputs, false, 1);
        } catch (MsoException e) {
            if (e instanceof MsoIOException && e.getCause() != null
                    && e.getCause() instanceof OpenStackConnectException) {
                // Ok, we were able to go up to the connection to OpenStack
            } else {
                e.printStackTrace();
                fail("Exception caught");
            }
        }
        try {
            util.updateStack("cloud", "tenantId", "stackName", "heatTemplate", stackInputs, false, 1, "environment");
        } catch (MsoException e) {
            if (e instanceof MsoIOException && e.getCause() != null
                    && e.getCause() instanceof OpenStackConnectException) {
                // Ok, we were able to go up to the connection to OpenStack
            } else {
                e.printStackTrace();
                fail("Exception caught");
            }
        }
        try {
            util.updateStack("cloud", "tenantId", "stackName", "heatTemplate", stackInputs, false, 1, "environment", null);
        } catch (MsoException e) {
            if (e instanceof MsoIOException && e.getCause() != null
                    && e.getCause() instanceof OpenStackConnectException) {
                // Ok, we were able to go up to the connection to OpenStack
            } else {
                e.printStackTrace();
                fail("Exception caught");
            }
        }
        try {
            util.updateStack("cloud", "tenantId", "stackName", "heatTemplate", stackInputs, false, 1, "environment", null, null);
        } catch (MsoException e) {
            if (e instanceof MsoIOException && e.getCause() != null
                    && e.getCause() instanceof OpenStackConnectException) {
                // Ok, we were able to go up to the connection to OpenStack
            } else {
                e.printStackTrace();
                fail("Exception caught");
            }
        }
    }
}
