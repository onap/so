/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.asdc.util;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.sdc.api.notification.IArtifactInfo;
import org.onap.sdc.api.notification.INotificationData;
import org.onap.sdc.api.notification.IResourceInstance;
import org.onap.so.asdc.installer.VfModuleMetaData;

public class ASDCNotificationLoggingTest {
    @Test
    public void dumpASDCNotificationTestForNull() throws Exception {
        INotificationData asdcNotification = iNotificationDataObject();

        String result = ASDCNotificationLogging.dumpASDCNotification(asdcNotification);

        assertTrue(!result.equalsIgnoreCase("NULL"));
    }

    private INotificationData iNotificationDataObject() {
        INotificationData iNotification = new INotificationData() {

            @Override
            public String getServiceVersion() {
                return "DistributionID";
            }

            @Override
            public String getServiceUUID() {
                return "12343254";
            }

            @Override
            public String getServiceName() {
                return "servername";
            }

            @Override
            public String getServiceInvariantUUID() {
                return "ServiceInvariantUUID";
            }

            @Override
            public String getServiceDescription() {
                return "Description";
            }

            @Override
            public List<IArtifactInfo> getServiceArtifacts() {
                return new ArrayList();
            }

            @Override
            public List<IResourceInstance> getResources() {
                return new ArrayList();
            }

            @Override
            public String getDistributionID() {
                return "23434";
            }

            @Override
            public IArtifactInfo getArtifactMetadataByUUID(String arg0) {
                return null;
            }

            @Override
            public String getWorkloadContext() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void setWorkloadContext(String arg0) {
                // TODO Auto-generated method stub

            }
        };
        return iNotification;
    }

    @Test
    public void dumpASDCNotificationTest() throws Exception {
        INotificationData asdcNotification = iNotificationDataObject();
        String result = ASDCNotificationLogging.dumpASDCNotification(asdcNotification);

        assertTrue(!result.equalsIgnoreCase("NULL"));
    }

    @Test
    public void dumpVfModuleMetaDataListTest() {
        INotificationData asdcNotification = iNotificationDataObject();
        List<VfModuleMetaData> list = new ArrayList<>();
        list.add(new VfModuleMetaData());
        String result = null;
        try {
            result = ASDCNotificationLogging.dumpVfModuleMetaDataList(list);
        } catch (Exception e) {
        }

        assertTrue(result == null);

    }

    public IArtifactInfo getIArtifactInfo() {
        return new IArtifactInfo() {

            @Override
            public List<IArtifactInfo> getRelatedArtifacts() {
                return null;
            }

            @Override
            public IArtifactInfo getGeneratedArtifact() {
                return null;
            }

            @Override
            public String getArtifactVersion() {
                return "version";
            }

            @Override
            public String getArtifactUUID() {
                return "123";
            }

            @Override
            public String getArtifactURL() {
                return "url";
            }

            @Override
            public String getArtifactType() {
                return "type";
            }

            @Override
            public Integer getArtifactTimeout() {
                return 12;
            }

            @Override
            public String getArtifactName() {
                return "name";
            }

            @Override
            public String getArtifactDescription() {
                return "desc";
            }

            @Override
            public String getArtifactChecksum() {
                return "true";
            }
        };
    }
}
