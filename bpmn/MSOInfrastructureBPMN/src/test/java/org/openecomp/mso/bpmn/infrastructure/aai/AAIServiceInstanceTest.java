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


package org.openecomp.mso.bpmn.infrastructure.aai;

import org.junit.Test;


public class AAIServiceInstanceTest {

    AAIServiceInstance test = new AAIServiceInstance("serviceInstanceName","serviceType","serviceRole","orchestrationStatus","modelInvariantUuid","modelVersionId","environmentContext","workloadContext");

    @Test
    public void getServiceInstanceNameTest() throws Exception {
        test.getServiceInstanceName();
    }

    @Test
    public void setServiceInstanceNameTest() throws Exception {
        test.setServiceInstanceName("serviceInstanceName");
    }

    @Test
    public void getServiceTypeTest() throws Exception {
        test.getServiceType();
    }

    @Test
    public void setServiceTypeTest() throws Exception {
        test.setServiceType("serviceType");
    }


    @Test
    public void getServiceRoleTest() throws Exception {
        test.getServiceRole();
    }

    @Test
    public void setServiceRoleTest() throws Exception {
        test.setServiceRole("serviceRole");
    }

    @Test
    public void getOrchestrationStatusTest() throws Exception {
        test.getOrchestrationStatus();
    }

    @Test
    public void setOrchestrationStatusTest() throws Exception {
        test.setOrchestrationStatus("status");
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
    public void getModelVersionIdTest() throws Exception {
        test.getModelVersionId();
    }

    @Test
    public void setModelVersionIdTest() throws Exception {
        test.setModelVersionId("versionId");
    }

    @Test
    public void getEnvironmentContextTest() throws Exception {
        test.getEnvironmentContext();
    }

    @Test
    public void setEnvironmentContextTest() throws Exception {
        test.setEnvironmentContext("context");
    }

    @Test
    public void getWorkloadContextTest() throws Exception {
        test.getWorkloadContext();
    }

    @Test
    public void setWorkloadContextTest() throws Exception {
        test.setWorkloadContext("context");
    }

}


