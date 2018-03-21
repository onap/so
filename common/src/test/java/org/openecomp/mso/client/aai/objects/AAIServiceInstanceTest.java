/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.mso.client.aai.objects;

import org.junit.Test;

public class AAIServiceInstanceTest {

    AAIServiceInstance test = new AAIServiceInstance();

    @Test
    public void getServiceInstanceNameTest() throws Exception {
        test.getServiceInstanceName();
    }

    @Test
    public void setServiceInstanceNameTest() throws Exception {
        test.setServiceInstanceName("name");
    }

    @Test
    public void getServiceTypeTest() throws Exception {
        test.getServiceType();
    }

    @Test
    public void setServiceTypeTest() throws Exception {
        test.setServiceType("type");
    }

    @Test
    public void getServiceRoleTest() throws Exception {
        test.getServiceRole();
    }

    @Test
    public void setServiceRoleTest() throws Exception {
        test.setServiceRole("role");
    }

    @Test
    public void getoStatusTest() throws Exception {
        test.getoStatus();
    }

    @Test
    public void setoStatusTest() throws Exception {
        test.setoStatus("set");
    }

    @Test
    public void getModelInvariantUuidTest() throws Exception {
        test.getModelInvariantUuid();
    }

    @Test
    public void setModelInvariantUuidTest() throws Exception {
        test.setModelInvariantUuid("uuid");
    }

    @Test
    public void getModelUuidTest() throws Exception {
        test.getModelUuid();
    }

    @Test
    public void setModelUuidTest() throws Exception {
        test.setModelUuid("mUuid");
    }

    @Test
    public void getEnvironmentContextTest() throws Exception {
        test.getEnvironmentContext();
    }

    @Test
    public void setEnvironmentContextTest() throws Exception {
        test.setEnvironmentContext("envContext");
    }

    @Test
    public void getWorkloadContextTest() throws Exception {
        test.getWorkloadContext();
    }

    @Test
    public void setWorkloadContextTest() throws Exception {
        test.setWorkloadContext("workload");
    }

    @Test
    public void getServiceInstanceIdTest() throws Exception {
        test.getServiceInstanceId();
    }

    @Test
    public void setServiceInstanceIdTest() throws Exception {
        test.setServiceInstanceId("id");
    }

    @Test
    public void withServiceInstanceTest() throws Exception {
        test.withServiceInstance("instanceID");
    }

    @Test
    public void getUriTest() throws Exception {
        test.getUri();
    }

}
