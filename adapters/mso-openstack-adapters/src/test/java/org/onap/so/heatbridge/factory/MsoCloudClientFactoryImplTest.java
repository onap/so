/*
 * Copyright (C) 2018 Bell Canada. All rights reserved.
 *
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
 */
package org.onap.so.heatbridge.factory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.onap.so.heatbridge.HeatBridgeException;
import org.onap.so.heatbridge.openstack.api.OpenstackAccess;
import org.onap.so.heatbridge.openstack.api.OpenstackClient;
import org.onap.so.heatbridge.openstack.api.OpenstackClientException;
import org.onap.so.heatbridge.openstack.factory.OpenstackClientFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.mso.cloud.CloudConfig;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudIdentity;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.openstack.exceptions.MsoCloudIdentityNotFound;

public class MsoCloudClientFactoryImplTest {

    private static final String URL = "http://url/v2.0";
    private static final String MSO_ID = "user";
    private static final String MSO_PASS = "313DECE408AF7759D442D7B06DD9A6AA";
    private static final String REGION_ID = "RegionOne";
    private static final String TENANT_ID = "7320ec4a5b9d4589ba7c4412ccfd290f";

    @Mock
    private CloudConfigFactory cloudConfigFactory;

    @Mock
    private OpenstackClientFactory openstackClientFactory;

    @Mock
    private CloudConfig cloudConfig;

    @Mock
    private CloudSite cloudSite;

    @Mock
    private CloudIdentity cloudIdentity;

    @Mock
    private OpenstackClient expectedOsClient;

    private MsoCloudClientFactoryImpl msoCloudConfigFactory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        msoCloudConfigFactory = new MsoCloudClientFactoryImpl(openstackClientFactory);
    }

    @Test
    public void testGetOpenstackClient()
        throws MsoCloudIdentityNotFound, OpenstackClientException, HeatBridgeException {
        // Arrange
        when(cloudConfigFactory.getCloudConfig()).thenReturn(cloudConfig);
        when(openstackClientFactory.createOpenstackV2Client(any(OpenstackAccess.class))).thenReturn(
            expectedOsClient);
        when(cloudConfig.getCloudSite(REGION_ID)).thenReturn(cloudSite);
        when(cloudSite.getIdentityService()).thenReturn(cloudIdentity);
        when(cloudSite.getRegionId()).thenReturn(REGION_ID);
        when(cloudIdentity.getIdentityUrl()).thenReturn("http://test-identity/v2.0");
        when(cloudIdentity.getMsoId()).thenReturn("test-username");
        when(cloudIdentity.getMsoPass()).thenReturn("test-password");
        // Act
        OpenstackClient osClient = msoCloudConfigFactory.getOpenstackClient(URL, MSO_ID, MSO_PASS, REGION_ID, TENANT_ID);

        // Assert
        verify(openstackClientFactory).createOpenstackV2Client(any(OpenstackAccess.class));
        assertEquals(expectedOsClient, osClient);
    }
}
