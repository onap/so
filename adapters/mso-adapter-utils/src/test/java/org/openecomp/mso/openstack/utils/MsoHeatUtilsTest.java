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

package org.openecomp.mso.openstack.utils;


import com.woorea.openstack.heat.Heat;
import com.woorea.openstack.heat.model.Stack;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openecomp.mso.cloud.CloudConfigFactory;
import org.openecomp.mso.cloud.CloudSite;
import org.openecomp.mso.openstack.beans.HeatStatus;
import org.openecomp.mso.openstack.beans.StackInfo;
import org.openecomp.mso.openstack.exceptions.MsoException;
import org.openecomp.mso.openstack.exceptions.MsoTenantNotFound;
import org.openecomp.mso.properties.MsoPropertiesFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

//@RunWith(PowerMockRunner.class)
@PrepareForTest({MsoHeatUtils.class})


public class MsoHeatUtilsTest {

    @Mock
    StackInfo stackInfo;

    @Mock
    MsoPropertiesFactory msoPropertiesFactory;

    @Mock
    CloudConfigFactory cloudConfigFactory;

    @Mock
    Heat heatClient;

    @Mock
    CloudSite cloudSite;

    @Test(expected = NullPointerException.class)
    public void testCreateStack() throws MsoException
    {

        MsoHeatUtils mht = PowerMockito.spy(new MsoHeatUtils("msoPropID" ,msoPropertiesFactory,cloudConfigFactory));
        Map<String,String>metadata=new HashMap<>();
        metadata.put("1", "value");
        mht.createStack("cloudSiteId",
                "tenantId",
                "stackName",
                "heatTemplate",
                 metadata,
                 true,
        1);
           doReturn(mht.createStack("cloudSiteId",
                   "tenantId",
                   "stackName",
                   "heatTemplate",
                   metadata,
        true,
        1,
        null, null,
        null,
        true));

    }

    @Test(expected = NullPointerException.class)
    public void testCreateStackOne() throws MsoException
    {
        MsoHeatUtils mht = PowerMockito.spy(new MsoHeatUtils("msoPropID" ,msoPropertiesFactory,cloudConfigFactory));
        Map<String,String>metadata=new HashMap<>();
        metadata.put("1", "value");
        mht.createStack("cloudSiteId",
                "tenantId",
                "stackName",
                "heatTemplate",
                 metadata,
                true,
                1,
                "env");
        doReturn(mht.createStack("cloudSiteId",
                "tenantId",
                "stackName",
                "heatTemplate",
                metadata,
                true,
                1,
                "env", null,
                null,
                true));
    }

    @Test(expected = NullPointerException.class)
    public void testCreateStackTwo() throws MsoException
    {
        MsoHeatUtils mht = PowerMockito.spy(new MsoHeatUtils("msoPropID" ,msoPropertiesFactory,cloudConfigFactory));
        Map<String,String>metadata=new HashMap<>();
        metadata.put("1", "value");
        Map<String,Object>fileMap=new HashMap<>();
        fileMap.put("2", "value");
        mht.createStack("cloudSiteId",
                "tenantId",
                "stackName",
                "heatTemplate",
                 metadata,
                true,
                1,
                "env",
                 fileMap);
        doReturn(mht.createStack("cloudSiteId",
                "tenantId",
                "stackName",
                "heatTemplate",
                metadata,
                true,
                1,
                "env", fileMap,
                null,
                true));
    }

    @Test(expected = NullPointerException.class)
    public void testCreateStackThree() throws MsoException
    {
        MsoHeatUtils mht = PowerMockito.spy(new MsoHeatUtils("msoPropID" ,msoPropertiesFactory,cloudConfigFactory));
        Map<String,String>metadata=new HashMap<>();
        metadata.put("1", "value");
        Map<String,Object>fileMap=new HashMap<>();
        fileMap.put("2", "value");
        Map<String,Object>heatFileMap=new HashMap<>();
        heatFileMap.put("3", "value");
        mht.createStack("cloudSiteId",
                "tenantId",
                "stackName",
                "heatTemplate",
                metadata,
                true,
                1,
                "env",
                fileMap,
                heatFileMap);
        doReturn(mht.createStack("cloudSiteId",
                "tenantId",
                "stackName",
                "heatTemplate",
                metadata,
                true,
                1,
                "env", fileMap,
                heatFileMap,
                true));
    }

    @Test(expected = NullPointerException.class)


    public void testqueryStack() throws MsoException
    {
        MsoHeatUtils mht = PowerMockito.spy(new MsoHeatUtils("msoPropID" ,msoPropertiesFactory,cloudConfigFactory));

        mht.queryStack("cloudSiteId","tenantId","stackName");

        try {
            heatClient = mht.getHeatClient (cloudSite, "tenantId");
            assertNotNull(heatClient);

        } catch (MsoTenantNotFound e) {
            doReturn(new StackInfo ("stackName", HeatStatus.NOTFOUND));
        } catch (MsoException me) {

            me.addContext ("QueryStack");
            throw me;
        }

        Stack heatStack = mht.queryHeatStack (heatClient, "stackName");

        assertNull(heatStack);
        StackInfo stackInfo = new StackInfo ("stackName", HeatStatus.NOTFOUND);
        doReturn(stackInfo);

        assertNotNull(heatStack);
        doReturn(new StackInfo (heatStack));



    }

}
