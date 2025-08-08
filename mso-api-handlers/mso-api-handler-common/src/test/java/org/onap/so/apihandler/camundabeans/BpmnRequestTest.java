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
package org.onap.so.apihandler.camundabeans;

import org.junit.Test;

public class BpmnRequestTest {

    BpmnRequest bpmnRequest = new BpmnRequest();

    @Test
    public void getHostTest() {
        bpmnRequest.getHost();
    }

    @Test
    public void setHostTest() {
        bpmnRequest.setHost(new CamundaInput());
    }

    @Test
    public void getRequestIdTest() {
        bpmnRequest.getRequestId();
    }

    @Test
    public void setRequestIdTest() {
        bpmnRequest.setRequestId(new CamundaInput());
    }

    @Test
    public void getIsBaseVfModuleTest() {
        bpmnRequest.getIsBaseVfModule();
    }

    @Test
    public void setIsBaseVfModuleTest() {
        bpmnRequest.setIsBaseVfModule(new CamundaBooleanInput());
    }

    @Test
    public void getRecipeTimeoutTest() {
        bpmnRequest.getRecipeTimeout();
    }

    @Test
    public void setRecipeTimeoutTest() {
        bpmnRequest.setRecipeTimeout(new CamundaIntegerInput());
    }

    @Test
    public void getRequestActionTest() {
        bpmnRequest.getRequestAction();
    }

    @Test
    public void setRequestActionTest() {
        bpmnRequest.setRequestAction(new CamundaInput());
    }

    @Test
    public void getServiceInstanceIdTest() {
        bpmnRequest.getServiceInstanceId();
    }

    @Test
    public void setServiceInstanceIdTest() {
        bpmnRequest.setServiceInstanceId(new CamundaInput());
    }

    @Test
    public void getVnfIdTest() {
        bpmnRequest.getVnfId();
    }

    @Test
    public void setVnfIdTest() {
        bpmnRequest.setVnfId(new CamundaInput());
    }

    @Test
    public void getVfModuleIdTest() {
        bpmnRequest.getVnfId();
    }

    @Test
    public void setVfModuleIdTest() {
        bpmnRequest.setVfModuleId(new CamundaInput());
    }

    @Test
    public void getVolumeGroupIdTest() {
        bpmnRequest.getVolumeGroupId();
    }

    @Test
    public void setVolumeGroupIdTest() {
        bpmnRequest.setVolumeGroupId(new CamundaInput());
    }

    @Test
    public void getNetworkIdTest() {
        bpmnRequest.getNetworkId();
    }

    @Test
    public void setNetworkIdTest() {
        bpmnRequest.setNetworkId(new CamundaInput());
    }

    @Test
    public void getServiceTypeTest() {
        bpmnRequest.getServiceType();
    }

    @Test
    public void setServiceTypeTest() {
        bpmnRequest.setServiceType(new CamundaInput());
    }

    @Test
    public void getVnfTypeTest() {
        bpmnRequest.getVnfType();
    }

    @Test
    public void setVnfTypeTest() {
        bpmnRequest.setVnfType(new CamundaInput());
    }

    @Test
    public void getVfModuleTypeTest() {
        bpmnRequest.getVfModuleType();
    }

    @Test
    public void setVfModuleTypeTest() {
        bpmnRequest.setVfModuleType(new CamundaInput());
    }

    @Test
    public void getNetworkTypeTest() {
        bpmnRequest.getNetworkType();
    }

    @Test
    public void setNetworkTypeTest() {
        bpmnRequest.setNetworkType(new CamundaInput());
    }

    @Test
    public void getRequestDetailsTest() {
        bpmnRequest.getRequestDetails();
    }

    @Test
    public void setRequestDetailsTest() {
        bpmnRequest.setRequestDetails(new CamundaInput());
    }

}
