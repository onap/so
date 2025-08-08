/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.namingservice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoServiceInstance;
import org.onap.so.client.exception.BBObjectNotFoundException;

public class NamingRequestUtilsTest extends BaseTaskTest {

    @Mock
    private ServiceInstance serviceInstanceMock;

    @Mock
    ModelInfoServiceInstance modelInfoServiceInstanceMock;

    @InjectMocks
    private NamingServiceUtils namingServiceUtils = new NamingServiceUtils();

    @Before
    public void before() throws BBObjectNotFoundException {
        when(extractPojosForBB.extractByKey(any(), ArgumentMatchers.eq(ResourceKey.SERVICE_INSTANCE_ID)))
                .thenReturn(serviceInstanceMock);
        doReturn(modelInfoServiceInstanceMock).when(serviceInstanceMock).getModelInfoServiceInstance();
    }

    @Test
    public void checkVpnBondingServiceTest() {
        doReturn("bonding").when(modelInfoServiceInstanceMock).getServiceType();
        doReturn("infrastructure-vpn").when(modelInfoServiceInstanceMock).getServiceRole();
        doReturn("testNaminPolicy").when(modelInfoServiceInstanceMock).getNamingPolicy();
        doReturn(true).when(modelInfoServiceInstanceMock).getOnapGeneratedNaming();
        namingServiceUtils.checkVpnBondingService(execution);
        assertTrue(execution.getVariable("isVpnBondingService"));

        doReturn("bonding-false").when(modelInfoServiceInstanceMock).getServiceType();
        doReturn("infrastructure-vpn").when(modelInfoServiceInstanceMock).getServiceRole();
        namingServiceUtils.checkVpnBondingService(execution);
        assertFalse(execution.getVariable("isVpnBondingService"));
    }

    @Test
    public void checkBondingAndInfrastureVpnTrueTest() {
        doReturn("bonding").when(modelInfoServiceInstanceMock).getServiceType();
        doReturn("infrastructure-vpn").when(modelInfoServiceInstanceMock).getServiceRole();
        namingServiceUtils.checkBondingAndInfrastureVpn(execution);
        assertTrue(execution.getVariable("isBondingAndInsfrastructureVpn"));
    }

    @Test
    public void checkBondingAndInfrastureVpnFalse1Test() {
        doReturn("falseBonding").when(modelInfoServiceInstanceMock).getServiceType();
        doReturn("infrastructure-vpn").when(modelInfoServiceInstanceMock).getServiceRole();
        namingServiceUtils.checkBondingAndInfrastureVpn(execution);
        assertFalse(execution.getVariable("isBondingAndInsfrastructureVpn"));
    }

    @Test
    public void checkBondingAndInfrastureVpnFalse2Test() {
        doReturn("bonding").when(modelInfoServiceInstanceMock).getServiceType();
        doReturn("false-infrastructure-vpn").when(modelInfoServiceInstanceMock).getServiceRole();
        namingServiceUtils.checkBondingAndInfrastureVpn(execution);
        assertFalse(execution.getVariable("isBondingAndInsfrastructureVpn"));
    }

    @Test
    public void checkNamingPolicyAndAndEcompGeneratedNaming_TrueTest() {
        doReturn("testNaminPolicy").when(modelInfoServiceInstanceMock).getNamingPolicy();
        doReturn(true).when(modelInfoServiceInstanceMock).getOnapGeneratedNaming();
        namingServiceUtils.checkNamingPolicyAndOnapGeneratedNaming(execution);
        assertTrue(execution.getVariable("isNamingPolicyAndOnapGeneratedNaming"));
    }

    @Test
    public void checkNamingPolicyAndAndEcompGeneratedNamingFalse1Test() {
        doReturn(null).when(modelInfoServiceInstanceMock).getNamingPolicy();
        doReturn(true).when(modelInfoServiceInstanceMock).getOnapGeneratedNaming();
        namingServiceUtils.checkNamingPolicyAndOnapGeneratedNaming(execution);
        assertFalse(execution.getVariable("isNamingPolicyAndOnapGeneratedNaming"));
    }

    @Test
    public void checkNamingPolicyAndAndEcompGeneratedNamingFalse2Test() {
        doReturn("testNaminPolicy").when(modelInfoServiceInstanceMock).getNamingPolicy();
        doReturn(false).when(modelInfoServiceInstanceMock).getOnapGeneratedNaming();
        namingServiceUtils.checkNamingPolicyAndOnapGeneratedNaming(execution);
        assertFalse(execution.getVariable("isNamingPolicyAndOnapGeneratedNaming"));
    }

    @Test
    public void checkNamingPolicyAndAndEcompGeneratedNamingFalse3Test() {
        doReturn("").when(modelInfoServiceInstanceMock).getNamingPolicy();
        doReturn(false).when(modelInfoServiceInstanceMock).getOnapGeneratedNaming();
        namingServiceUtils.checkNamingPolicyAndOnapGeneratedNaming(execution);
        assertFalse(execution.getVariable("isNamingPolicyAndOnapGeneratedNaming"));
    }

    @Test
    public void checkNamingPolicyAndAndEcompGeneratedNamingFalse4Test() {
        doReturn("bonding").when(modelInfoServiceInstanceMock).getNamingPolicy();
        doReturn(null).when(modelInfoServiceInstanceMock).getOnapGeneratedNaming();
        namingServiceUtils.checkNamingPolicyAndOnapGeneratedNaming(execution);
        assertFalse(execution.getVariable("isNamingPolicyAndOnapGeneratedNaming"));
    }

}
