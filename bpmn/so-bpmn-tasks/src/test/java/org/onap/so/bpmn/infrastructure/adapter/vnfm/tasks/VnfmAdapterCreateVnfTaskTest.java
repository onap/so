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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.CREATE_VNF_REQUEST_PARAM_NAME;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.CREATE_VNF_RESPONSE_PARAM_NAME;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.INPUT_PARAMETER;
import java.util.Collections;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.Tenant;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.InputParameter;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoGenericVnf;
import com.google.common.base.Optional;


/**
 * @author waqas.ikram@est.tech
 */
public class VnfmAdapterCreateVnfTaskTest extends BaseTaskTest {

    private static final String MODEL_INSTANCE_NAME = "MODEL_INSTANCE_NAME";

    private static final String CLOUD_OWNER = "CLOUD_OWNER";

    private static final String LCP_CLOUD_REGIONID = "RegionOnce";

    private static final String VNF_ID = UUID.randomUUID().toString();

    private static final String VNF_NAME = "VNF_NAME";

    private static final String JOB_ID = UUID.randomUUID().toString();

    @Mock
    private VnfmAdapterServiceProvider mockedVnfmAdapterServiceProvider;

    private final BuildingBlockExecution stubbedxecution = new StubbedBuildingBlockExecution();

    @Test
    public void testBuildCreateVnfRequest_withValidValues_storesRequestInExecution() throws Exception {

        final VnfmAdapterCreateVnfTask objUnderTest = getEtsiVnfInstantiateTask();
        stubbedxecution.setVariable(INPUT_PARAMETER,
                new InputParameter(Collections.emptyMap(), Collections.emptyList()));

        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(getGenericVnf());
        objUnderTest.buildCreateVnfRequest(stubbedxecution);

        final CreateVnfRequest actual = stubbedxecution.getVariable(CREATE_VNF_REQUEST_PARAM_NAME);
        assertNotNull(actual);
        assertEquals(VNF_NAME + "." + MODEL_INSTANCE_NAME, actual.getName());

        final Tenant actualTenant = actual.getTenant();
        assertEquals(CLOUD_OWNER, actualTenant.getCloudOwner());
        assertEquals(LCP_CLOUD_REGIONID, actualTenant.getRegionName());
        assertEquals(StubbedBuildingBlockExecution.getTenantId(), actualTenant.getTenantId());

    }

    @Test
    public void testBuildCreateVnfRequest_extractPojosForBBThrowsException_exceptionBuilderCalled() throws Exception {

        final VnfmAdapterCreateVnfTask objUnderTest = getEtsiVnfInstantiateTask();

        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenThrow(RuntimeException.class);

        objUnderTest.buildCreateVnfRequest(stubbedxecution);

        final CreateVnfRequest actual = stubbedxecution.getVariable(CREATE_VNF_REQUEST_PARAM_NAME);

        assertNull(actual);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1200),
                any(Exception.class));

    }

    @Test
    public void testInvokeVnfmAdapter_validValues_storesResponseInExecution() throws Exception {

        final VnfmAdapterCreateVnfTask objUnderTest = getEtsiVnfInstantiateTask();

        stubbedxecution.setVariable(CREATE_VNF_REQUEST_PARAM_NAME, new CreateVnfRequest());

        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(getGenericVnf());
        when(mockedVnfmAdapterServiceProvider.invokeCreateInstantiationRequest(eq(VNF_ID), any(CreateVnfRequest.class)))
                .thenReturn(getCreateVnfResponse());

        objUnderTest.invokeVnfmAdapter(stubbedxecution);

        assertNotNull(stubbedxecution.getVariable(CREATE_VNF_RESPONSE_PARAM_NAME));
    }

    @Test
    public void testInvokeVnfmAdapter_invalidValues_storesResponseInExecution() throws Exception {

        final VnfmAdapterCreateVnfTask objUnderTest = getEtsiVnfInstantiateTask();

        stubbedxecution.setVariable(CREATE_VNF_REQUEST_PARAM_NAME, new CreateVnfRequest());

        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(getGenericVnf());
        when(mockedVnfmAdapterServiceProvider.invokeCreateInstantiationRequest(eq(VNF_ID), any(CreateVnfRequest.class)))
                .thenReturn(Optional.absent());

        objUnderTest.invokeVnfmAdapter(stubbedxecution);

        assertNull(stubbedxecution.getVariable(CREATE_VNF_RESPONSE_PARAM_NAME));
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1202),
                any(Exception.class));
    }


    @Test
    public void testInvokeVnfmAdapter_extractPojosForBBThrowsException_exceptionBuilderCalled() throws Exception {

        final VnfmAdapterCreateVnfTask objUnderTest = getEtsiVnfInstantiateTask();

        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenThrow(RuntimeException.class);

        objUnderTest.invokeVnfmAdapter(stubbedxecution);

        assertNull(stubbedxecution.getVariable(CREATE_VNF_RESPONSE_PARAM_NAME));
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(1202),
                any(Exception.class));

    }

    private Optional<CreateVnfResponse> getCreateVnfResponse() {
        final CreateVnfResponse response = new CreateVnfResponse();
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

    private VnfmAdapterCreateVnfTask getEtsiVnfInstantiateTask() {
        return new VnfmAdapterCreateVnfTask(exceptionUtil, extractPojosForBB, mockedVnfmAdapterServiceProvider);
    }
}
