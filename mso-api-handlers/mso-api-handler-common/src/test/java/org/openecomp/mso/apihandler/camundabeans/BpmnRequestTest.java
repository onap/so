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
package org.openecomp.mso.apihandler.camundabeans;

import org.junit.Test;

public class BpmnRequestTest {

    BpmnRequest bpmnRequest = new BpmnRequest();

    @Test
    public void getHostTest() throws Exception {
        bpmnRequest.getHost();
    }

    @Test
    public void setHostTest() throws Exception {
        bpmnRequest.setHost(new CamundaInput());
    }

    @Test
    public void getRequestIdTest() throws Exception {
        bpmnRequest.getRequestId();
    }

    @Test
    public void setRequestIdTest() throws Exception {
        bpmnRequest.setRequestId(new CamundaInput());
    }

    @Test
    public void getIsBaseVfModuleTest() throws Exception {
        bpmnRequest.getIsBaseVfModule();
    }

    @Test
    public void setIsBaseVfModuleTest() throws Exception {
        bpmnRequest.setIsBaseVfModule(new CamundaBooleanInput());
    }

    @Test
    public void getRecipeTimeoutTest() throws Exception {
        bpmnRequest.getRecipeTimeout();
    }

    @Test
    public void setRecipeTimeoutTest() throws Exception {
        bpmnRequest.setRecipeTimeout(new CamundaIntegerInput());
    }

    @Test
    public void getRequestActionTest() throws Exception {
        bpmnRequest.getRequestAction();
    }

    @Test
    public void setRequestActionTest() throws Exception {
        bpmnRequest.setRequestAction(new CamundaInput());
    }

    @Test
    public void getServiceInstanceIdTest() throws Exception {
        bpmnRequest.getServiceInstanceId();
    }

    @Test
    public void setServiceInstanceIdTest() throws Exception {
        bpmnRequest.setServiceInstanceId(new CamundaInput());
    }

    @Test
    public void getVnfIdTest() throws Exception {
        bpmnRequest.getVnfId();
    }

    @Test
    public void setVnfIdTest() throws Exception {
        bpmnRequest.setVnfId(new CamundaInput());
    }

    @Test
    public void getVfModuleIdTest() throws Exception {
        bpmnRequest.getVnfId();
    }

    @Test
    public void setVfModuleIdTest() throws Exception {
        bpmnRequest.setVfModuleId(new CamundaInput());
    }

    @Test
    public void getVolumeGroupIdTest() throws Exception {
        bpmnRequest.getVolumeGroupId();
    }

    @Test
    public void setVolumeGroupIdTest() throws Exception {
        bpmnRequest.setVolumeGroupId(new CamundaInput());
    }

    @Test
    public void getNetworkIdTest() throws Exception {
        bpmnRequest.getNetworkId();
    }

    @Test
    public void setNetworkIdTest() throws Exception {
        bpmnRequest.setNetworkId(new CamundaInput());
    }

    @Test
    public void getServiceTypeTest() throws Exception {
        bpmnRequest.getServiceType();
    }

    @Test
    public void setServiceTypeTest() throws Exception {
        bpmnRequest.setServiceType(new CamundaInput());
    }

    @Test
    public void getVnfTypeTest() throws Exception {
        bpmnRequest.getVnfType();
    }

    @Test
    public void setVnfTypeTest() throws Exception {
        bpmnRequest.setVnfType(new CamundaInput());
    }

    @Test
    public void getVfModuleTypeTest() throws Exception {
        bpmnRequest.getVfModuleType();
    }

    @Test
    public void setVfModuleTypeTest() throws Exception {
        bpmnRequest.setVfModuleType(new CamundaInput());
    }

    @Test
    public void getNetworkTypeTest() throws Exception {
        bpmnRequest.getNetworkType();
    }

    @Test
    public void setNetworkTypeTest() throws Exception {
        bpmnRequest.setNetworkType(new CamundaInput());
    }

    @Test
    public void getRequestDetailsTest() throws Exception {
        bpmnRequest.getRequestDetails();
    }

    @Test
    public void setRequestDetailsTest() throws Exception {
        bpmnRequest.setRequestDetails(new CamundaInput());
    }

}