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

public class AAIOperationalEnvironmentTest {

    AAIOperationalEnvironment test = new AAIOperationalEnvironment();

    @Test
    public void getOperationalEnvironmentIdTest() throws Exception {
        test.getOperationalEnvironmentId();
    }

    @Test
    public void setOperationalEnvironmentIdTest() throws Exception {
        test.setOperationalEnvironmentId("id");
    }

    @Test
    public void getOperationalEnvironmentNameTest() throws Exception {
        test.getOperationalEnvironmentName();
    }

    @Test
    public void setOperationalEnvironmentNameTest() throws Exception {
        test.setOperationalEnvironmentName("name");
    }


    @Test
    public void getOperationalEnvironmentTypeTest() throws Exception {
        test.getOperationalEnvironmentType();
    }

    @Test
    public void setOperationalEnvironmentTypeTest() throws Exception {
        test.setOperationalEnvironmentType("type");
    }

    @Test
    public void getOperationalEnvironmentStatusTest() throws Exception {
        test.getOperationalEnvironmentStatus();
    }

    @Test
    public void setOperationalEnvironmentStatusTest() throws Exception {
        test.setOperationalEnvironmentStatus("status");
    }

    @Test
    public void getTenantContextTest() throws Exception {
        test.getTenantContext();
    }

    @Test
    public void setTenantContextTest() throws Exception {
        test.setTenantContext("context");
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
    public void getResourceVersionTest() throws Exception {
        test.getResourceVersion();
    }

    @Test
    public void setResourceVersionTest() throws Exception {
        test.setResourceVersion("version");
    }

    @Test
    public void withOperationalEnvironmentIdTest() throws Exception {
        test.withOperationalEnvironmentId("envId");
    }

    @Test
    public void withOperationalEnvironmentNameTest() throws Exception {
        test.withOperationalEnvironmentName("envName");
    }

    @Test
    public void withOperationalEnvironmentTypeTest() throws Exception {
        test.withOperationalEnvironmentType("envType");
    }

    @Test
    public void withOperationalEnvironmentStatusTest() throws Exception {
        test.withOperationalEnvironmentStatus("envStatus");
    }

    @Test
    public void withWorkloadContextTest() throws Exception {
        test.withTenantContext("tenContext");
    }

    @Test
    public void withTenantContextTest() throws Exception {
        test.withTenantContext("tenContext");
    }

    @Test
    public void withResourceVersionTest() throws Exception {
        test.withResourceVersion("resVersion");
    }

}
