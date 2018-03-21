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
package org.openecomp.mso.openstack.beans;

import com.woorea.openstack.quantum.model.Network;
import org.junit.Test;

import java.util.Arrays;

public class NetworkInfoTest {

    NetworkInfo networkInfo = new NetworkInfo(new Network());

    @Test
    public void getName() throws Exception {
        networkInfo.getName();
    }

    @Test
    public void setName() throws Exception {
        networkInfo.setName("test");
    }

    @Test
    public void getId() throws Exception {
        networkInfo.getId();
    }

    @Test
    public void setId() throws Exception {
        networkInfo.setId("test");
    }

    @Test
    public void getStatus() throws Exception {
        networkInfo.getStatus();
    }

    @Test
    public void setStatus() throws Exception {
        networkInfo.setStatus(null);
    }

    @Test
    public void getProvider() throws Exception {
        networkInfo.getProvider();
    }

    @Test
    public void setProvider() throws Exception {
        networkInfo.setProvider("provider");
    }

    @Test
    public void getVlans() throws Exception {
        networkInfo.getVlans();
    }

    @Test
    public void setVlans() throws Exception {
        networkInfo.setVlans(Arrays.asList(10, 20, 30));
    }

    @Test
    public void getSubnets() throws Exception {
        networkInfo.getSubnets();
    }

}