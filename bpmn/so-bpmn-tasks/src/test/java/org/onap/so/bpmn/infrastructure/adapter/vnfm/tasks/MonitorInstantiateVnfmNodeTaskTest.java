/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.CREATE_VNF_NODE_STATUS;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.VNF_CREATED;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.orchestration.AAIVnfResources;

/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public class MonitorInstantiateVnfmNodeTaskTest extends BaseTaskTest {

    private static final String VNF_ID = UUID.randomUUID().toString();

    private static final String VNF_NAME = "VNF_NAME";

    private MonitorVnfmNodeTask objUnderTest;

    @Mock
    private VnfmAdapterServiceProvider mockedVnfmAdapterServiceProvider;

    @Mock
    private AAIVnfResources mockedAaiVnfResources;

    private final BuildingBlockExecution stubbedxecution = new StubbedBuildingBlockExecution();

    @Before
    public void setUp() {
        objUnderTest = getEtsiVnfMonitorNodeJobTask();
    }

    @Test
    public void testGetNodeStatus_genericVnfWithOrchStatusCreated_executionVariableSetToCreate() throws Exception {
        final org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf = getGenericVnf();
        final GenericVnf aaiGenericVnf = getAAIGenericVnf();
        aaiGenericVnf.setOrchestrationStatus(VNF_CREATED);

        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(vnf);
        when(mockedAaiVnfResources.getGenericVnf(eq(VNF_ID))).thenReturn(Optional.of(aaiGenericVnf));
        objUnderTest.getNodeStatus(stubbedxecution);
        assertTrue(stubbedxecution.getVariable(CREATE_VNF_NODE_STATUS));
    }

    @Test
    public void testGetNodeStatus_noGenericVnfFoundInAAI_throwException() throws Exception {
        final org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf = getGenericVnf();

        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(vnf);
        when(mockedAaiVnfResources.getGenericVnf(eq(VNF_ID))).thenReturn(Optional.empty());
        objUnderTest.getNodeStatus(stubbedxecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1220),
                any(Exception.class));
        assertNull(stubbedxecution.getVariable(CREATE_VNF_NODE_STATUS));

    }

    @Test
    public void testGetNodeStatusException() throws Exception {
        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenThrow(RuntimeException.class);
        objUnderTest.getNodeStatus(stubbedxecution);
        assertNull(stubbedxecution.getVariable(CREATE_VNF_NODE_STATUS));
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1220),
                any(Exception.class));
    }

    @Test
    public void testTimeOutLogFailue() {
        objUnderTest.timeOutLogFailue(stubbedxecution);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1221),
                eq("Node operation time out"));
    }

    private GenericVnf getAAIGenericVnf() {
        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId(VNF_ID);
        genericVnf.setVnfName(VNF_NAME);
        return genericVnf;
    }

    private org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf getGenericVnf() {
        final org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf genericVnf =
                new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
        genericVnf.setVnfId(VNF_ID);
        return genericVnf;

    }

    private MonitorInstantiateVnfmNodeTask getEtsiVnfMonitorNodeJobTask() {
        return new MonitorInstantiateVnfmNodeTask(extractPojosForBB, exceptionUtil, mockedAaiVnfResources);
    }

}
