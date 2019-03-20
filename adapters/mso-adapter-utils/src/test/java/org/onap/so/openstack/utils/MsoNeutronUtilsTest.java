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

package org.onap.so.openstack.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.StubOpenStack;
import org.onap.so.BaseTest;
import org.onap.so.openstack.beans.NetworkInfo;
import org.onap.so.openstack.exceptions.MsoException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MsoNeutronUtilsTest extends BaseTest{
    
    @Autowired
    private MsoNeutronUtils msoNeutronUtils;

    private List<Integer> vlans;
    
    @Before
    public void before() throws IOException {
        vlans = new ArrayList<>();
        vlans.add(3014);
        StubOpenStack.mockOpenStackResponseAccess(wireMockServer, wireMockPort);
    }
    
    @Test
    public void createNetworkTest_OpenStackBaseException() throws Exception {
        expectedException.expect(MsoException.class);
        msoNeutronUtils.createNetwork("MTN13", "tenantId", 
                MsoNeutronUtils.NetworkType.PROVIDER,"networkName", "PROVIDER", vlans);
    }

    @Test
    public void createNetworkTest_NetworkTypeAsMultiProvider() throws Exception {
        StubOpenStack.mockOpenstackPostNetwork(wireMockServer, "OpenstackCreateNeutronNetworkResponse.json");
        NetworkInfo networkInfo = msoNeutronUtils.createNetwork("MTN13", "tenantId",
                MsoNeutronUtils.NetworkType.MULTI_PROVIDER,"networkName","PROVIDER", vlans);

        Assert.assertEquals("2a4017ef-31ff-496a-9294-e96ecc3bc9c9",networkInfo.getId());
    }

    @Test
    public void createNetworkTest() throws Exception {
        StubOpenStack.mockOpenstackPostNetwork(wireMockServer, "OpenstackCreateNeutronNetworkResponse.json");
        NetworkInfo networkInfo = msoNeutronUtils.createNetwork("MTN13", "tenantId",
                MsoNeutronUtils.NetworkType.PROVIDER,"networkName","PROVIDER", vlans);

        Assert.assertEquals("2a4017ef-31ff-496a-9294-e96ecc3bc9c9",networkInfo.getId());
    }

    @Test
    public void queryNetworkTest() throws Exception {
        StubOpenStack.mockOpenStackGetNeutronNetwork(wireMockServer, "GetNeutronNetwork.json", "43173f6a-d699-414b-888f-ab243dda6dfe");
        NetworkInfo networkInfo = msoNeutronUtils.queryNetwork("43173f6a-d699-414b-888f-ab243dda6dfe", "tenantId","MTN13");

        Assert.assertEquals("net1",networkInfo.getName());
    }

    @Test
    public void queryNetworkTest_404() throws Exception {
        NetworkInfo networkInfo = msoNeutronUtils.queryNetwork("43173f6a-d699-414b-888f-ab243dda6dfe", "tenantId","MTN13");
        Assert.assertNull(networkInfo);
    }

    @Test
    public void queryNetworkTest_500() throws Exception {
        expectedException.expect(MsoException.class);
        StubOpenStack.mockOpenStackGetNeutronNetwork_500(wireMockServer, "43173f6a-d699-414b-888f-ab243dda6dfe");
        msoNeutronUtils.queryNetwork("43173f6a-d699-414b-888f-ab243dda6dfe", "tenantId","MTN13");

    }

    @Test
    public void deleteNetworkkTest() throws Exception {
        StubOpenStack.mockOpenStackGetNeutronNetwork(wireMockServer, "GetNeutronNetwork.json", "43173f6a-d699-414b-888f-ab243dda6dfe");
        StubOpenStack.mockOpenStackDeleteNeutronNetwork(wireMockServer, "43173f6a-d699-414b-888f-ab243dda6dfe");
        Boolean result = msoNeutronUtils.deleteNetwork("43173f6a-d699-414b-888f-ab243dda6dfe", "tenantId","MTN13");

        Assert.assertTrue(result);
    }

    @Test
    public void updateNetworkTest() throws Exception {
        StubOpenStack.mockOpenStackGetNeutronNetwork(wireMockServer, "GetNeutronNetwork.json", "43173f6a-d699-414b-888f-ab243dda6dfe");
        StubOpenStack.mockOpenstackPutNetwork(wireMockServer, "OpenstackCreateNeutronNetworkResponse.json", "43173f6a-d699-414b-888f-ab243dda6dfe");
        NetworkInfo networkInfo = msoNeutronUtils.updateNetwork("MTN13", "tenantId",
                "43173f6a-d699-414b-888f-ab243dda6dfe",MsoNeutronUtils.NetworkType.PROVIDER,"PROVIDER", vlans);

        Assert.assertEquals("2a4017ef-31ff-496a-9294-e96ecc3bc9c9",networkInfo.getId());
    }

    @Test
    public void updateNetworkTest_NetworkTypeAsMultiProvider() throws Exception {
        StubOpenStack.mockOpenStackGetNeutronNetwork(wireMockServer, "GetNeutronNetwork.json", "43173f6a-d699-414b-888f-ab243dda6dfe");
        StubOpenStack.mockOpenstackPutNetwork(wireMockServer, "OpenstackCreateNeutronNetworkResponse.json", "43173f6a-d699-414b-888f-ab243dda6dfe");
        NetworkInfo networkInfo = msoNeutronUtils.updateNetwork("MTN13", "tenantId",
                "43173f6a-d699-414b-888f-ab243dda6dfe",MsoNeutronUtils.NetworkType.MULTI_PROVIDER,"PROVIDER", vlans);

        Assert.assertEquals("2a4017ef-31ff-496a-9294-e96ecc3bc9c9",networkInfo.getId());
    }
}
