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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import org.onap.etsi.sol003.adapter.lcm.v1.model.DeleteVnfResponse;
import com.google.common.base.Optional;

/**
 *
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 *
 */
public class EtsiVnfDeleteTaskTest extends BaseTaskTest {

    private static final String MODEL_INSTANCE_NAME = "MODEL_INSTANCE_NAME";

    private static final String VNF_ID = UUID.randomUUID().toString();

    private static final String VNF_NAME = "VNF_NAME";

    private static final String JOB_ID = UUID.randomUUID().toString();

    @Mock
    private VnfmAdapterServiceProvider mockedVnfmAdapterServiceProvider;

    @Mock
    private GeneralBuildingBlock buildingBlock;

    @Mock
    private RequestContext requestContext;

    private final BuildingBlockExecution stubbedxecution = new StubbedBuildingBlockExecution();

    @Test
    public void testInvokeVnfmAdapter() throws Exception {
        final EtsiVnfDeleteTask objUnderTest = getEtsiVnfDeleteTask();
        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(getGenericVnf());
        when(mockedVnfmAdapterServiceProvider.invokeDeleteRequest(eq(VNF_ID))).thenReturn(getDeleteVnfResponse());
        objUnderTest.invokeVnfmAdapter(stubbedxecution);
        assertNotNull(stubbedxecution.getVariable(Constants.DELETE_VNF_RESPONSE_PARAM_NAME));
    }

    @Test
    public void testInvokeVnfmAdapterException() throws Exception {
        final EtsiVnfDeleteTask objUnderTest = getEtsiVnfDeleteTask();
        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(getGenericVnf());
        when(mockedVnfmAdapterServiceProvider.invokeDeleteRequest(eq(VNF_ID))).thenReturn(Optional.absent());
        objUnderTest.invokeVnfmAdapter(stubbedxecution);
        assertNull(stubbedxecution.getVariable(Constants.DELETE_VNF_RESPONSE_PARAM_NAME));
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1212),
                any(Exception.class));
    }

    private Optional<DeleteVnfResponse> getDeleteVnfResponse() {
        final DeleteVnfResponse response = new DeleteVnfResponse();
        response.setJobId(JOB_ID);
        return Optional.of(response);
    }

    private GenericVnf getGenericVnf() {
        final GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfId(VNF_ID);
        genericVnf.setModelInfoGenericVnf(getModelInfoGenericVnf());
        genericVnf.setVnfName(VNF_NAME);
        return genericVnf;
    }

    private ModelInfoGenericVnf getModelInfoGenericVnf() {
        final ModelInfoGenericVnf modelInfoGenericVnf = new ModelInfoGenericVnf();
        modelInfoGenericVnf.setModelInstanceName(MODEL_INSTANCE_NAME);
        return modelInfoGenericVnf;
    }

    private EtsiVnfDeleteTask getEtsiVnfDeleteTask() {
        return new EtsiVnfDeleteTask(exceptionUtil, extractPojosForBB, mockedVnfmAdapterServiceProvider);
    }
}
