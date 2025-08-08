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


package org.onap.so.bpmn.infrastructure.aai;

import org.junit.Test;


public class AAIServiceInstanceTest {

    AAIServiceInstance test = new AAIServiceInstance.AAIServiceInstanceBuilder()
            .setServiceInstanceName("serviceInstanceName").setServiceType("serviceType").setServiceRole("serviceRole")
            .setOrchestrationStatus("orchestrationStatus").setModelInvariantUuid("modelInvariantUuid")
            .setModelVersionId("modelVersionId").setEnvironmentContext("environmentContext")
            .setWorkloadContext("workloadContext").createAAIServiceInstance();

    @Test
    public void getServiceInstanceNameTest() {
        test.getServiceInstanceName();
    }

    @Test
    public void setServiceInstanceNameTest() {
        test.setServiceInstanceName("serviceInstanceName");
    }

    @Test
    public void getServiceTypeTest() {
        test.getServiceType();
    }

    @Test
    public void setServiceTypeTest() {
        test.setServiceType("serviceType");
    }

    @Test
    public void getServiceRoleTest() {
        test.getServiceRole();
    }

    @Test
    public void setServiceRoleTest() {
        test.setServiceRole("serviceRole");
    }

    @Test
    public void getOrchestrationStatusTest() {
        test.getOrchestrationStatus();
    }

    @Test
    public void setOrchestrationStatusTest() {
        test.setOrchestrationStatus("status");
    }

    @Test
    public void getModelInvariantUuidTest() {
        test.getModelInvariantUuid();
    }

    @Test
    public void setModelInvariantUuidTest() {
        test.setModelInvariantUuid("uuid");
    }

    @Test
    public void getModelVersionIdTest() {
        test.getModelVersionId();
    }

    @Test
    public void setModelVersionIdTest() {
        test.setModelVersionId("versionId");
    }

    @Test
    public void getEnvironmentContextTest() {
        test.getEnvironmentContext();
    }

    @Test
    public void setEnvironmentContextTest() {
        test.setEnvironmentContext("context");
    }

    @Test
    public void getWorkloadContextTest() {
        test.getWorkloadContext();
    }

    @Test
    public void setWorkloadContextTest() {
        test.setWorkloadContext("context");
    }

}


