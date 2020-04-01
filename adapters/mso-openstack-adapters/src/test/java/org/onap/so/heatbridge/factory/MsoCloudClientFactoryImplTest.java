/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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

package org.onap.so.heatbridge.factory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.heatbridge.HeatBridgeException;
import org.onap.so.heatbridge.openstack.api.OpenstackAccess;
import org.onap.so.heatbridge.openstack.factory.OpenstackClientFactory;
import org.onap.so.utils.CryptoUtils;

public class MsoCloudClientFactoryImplTest {

    private static final String URL_V2 = "http://localhost:8080/v2.0";
    private static final String URL_V3 = "http://localhost:8080/v3";
    private static final String URL_WITH_UNSUPPORTED_VERSION = "http://localhost:8080/v4";

    private static final String MSO_ID = "testMsoId";
    private static final String ENCRYPTED_PASSWORD = CryptoUtils.encryptCloudConfigPassword("testPassword");
    private static final String REGION_ID = "testCloudRegionId";
    private static final String TENANT_ID = "testTenantId";

    private MsoCloudClientFactoryImpl testedObject;
    private OpenstackClientFactory openstackClientFactoryMock;

    @Before
    public void setup() {
        openstackClientFactoryMock = mock(OpenstackClientFactory.class);
        testedObject = new MsoCloudClientFactoryImpl(openstackClientFactoryMock);
    }

    @Test
    public void getOpenstackClientWithVersion2() throws Exception {
        testedObject.getOpenstackClient(URL_V2, MSO_ID, ENCRYPTED_PASSWORD, REGION_ID, TENANT_ID, "v2.0");
        verify(openstackClientFactoryMock).createOpenstackV2Client(any(OpenstackAccess.class));
    }

    @Test
    public void getOpenstackClientWithVersion3() throws Exception {
        testedObject.getOpenstackClient(URL_V3, MSO_ID, ENCRYPTED_PASSWORD, REGION_ID, TENANT_ID, "v3");
        verify(openstackClientFactoryMock).createOpenstackV3Client(any(OpenstackAccess.class));
    }

    @Test(expected = HeatBridgeException.class)
    public void getOpenstackClient_unsupportedVersion() throws Exception {
        testedObject.getOpenstackClient(URL_WITH_UNSUPPORTED_VERSION, MSO_ID, ENCRYPTED_PASSWORD, REGION_ID, TENANT_ID,
                "UNKNOWN");
    }

}
