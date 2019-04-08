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
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.so.asdc.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

public class NotificationDataImplTest extends BaseTest {
    @Autowired
    private NotificationDataImpl notificationDataImpl;

    @Mock
    private ArtifactInfoImpl artifactInfoImpl;

    private static final String NOTIFICATION_DATA_IMPL_STRING =
            "NotificationDataImpl [distributionID=distributionID, serviceName=serviceName, "
                    + "serviceVersion=serviceVersion, serviceUUID=serviceUUID, serviceDescription=serviceDescription, "
                    + "serviceInvariantUUID=serviceInvariantUUID, resources=null, serviceArtifacts=[artifactInfoImpl], workloadContext=workloadContext]";

    @Test
    public void toStringTest() {
        List<ArtifactInfoImpl> relevantServiceArtifacts = new ArrayList<ArtifactInfoImpl>();
        relevantServiceArtifacts.add(artifactInfoImpl);
        notificationDataImpl.setDistributionID("distributionID");
        notificationDataImpl.setServiceName("serviceName");
        notificationDataImpl.setServiceVersion("serviceVersion");
        notificationDataImpl.setServiceDescription("serviceDescription");
        notificationDataImpl.setServiceUUID("serviceUUID");
        notificationDataImpl.setServiceInvariantUUID("serviceInvariantUUID");
        notificationDataImpl.setWorkloadContext("workloadContext");
        notificationDataImpl.setServiceArtifacts(relevantServiceArtifacts);

        assertEquals(NOTIFICATION_DATA_IMPL_STRING, notificationDataImpl.toString());
    }
}
