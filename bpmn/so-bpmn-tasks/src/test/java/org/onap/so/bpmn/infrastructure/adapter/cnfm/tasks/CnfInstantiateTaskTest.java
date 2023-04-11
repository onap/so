/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.INPUT_PARAMETER;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.CLOUD_OWNER_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.CLOUD_REGION_PARAM_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.NAMESPACE_KEY;
import static org.onap.so.cnfm.lcm.model.utils.AdditionalParamsConstants.TENANT_ID_PARAM_KEY;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.InputParameter;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.cnfm.lcm.model.AsInstance;
import org.onap.so.cnfm.lcm.model.CreateAsRequest;
import org.onap.so.cnfm.lcm.model.InstantiateAsRequest;


/**
 * @author raviteja.kaumuri@est.tech
 */
@RunWith(MockitoJUnitRunner.class)
public class CnfInstantiateTaskTest {

    @Mock
    protected ExceptionBuilder exceptionUtil;
    private static final String CREATE_AS_REQUEST_OBJECT = "CreateAsRequestObject";
    private static final String INSTANTIATE_AS_REQUEST_OBJECT = "InstantiateAsRequest";
    private static final String MODEL_INSTANCE_NAME = "instanceTest";
    private static final String AS_INSTANCE_ID = "asInstanceid";
    private static final String CLOUD_OWNER = "CloudOwner";
    private static final String LCP_CLOUD_REGION_ID = "RegionOne";
    private static final String NAME_SPACE = "default";
    private static final String JOB_ID = UUID.randomUUID().toString();

    @Mock
    private CnfmHttpServiceProvider mockedCnfmHttpServiceProvider;

    private final BuildingBlockExecution stubbedExecution = new StubbedBuildingBlockExecution();

    @Test
    public void testCreateCreateASRequest_withValidValues_storesRequestInExecution() throws Exception {

        final CnfInstantiateTask objUnderTest = getCnfInstantiateTask();
        stubbedExecution.setVariable(INPUT_PARAMETER,
                new InputParameter(Collections.emptyMap(), Collections.emptyList()));

        objUnderTest.createCreateAsRequest(stubbedExecution);

        final CreateAsRequest actual = stubbedExecution.getVariable(CREATE_AS_REQUEST_OBJECT);
        assertNotNull(actual);
        assertEquals(MODEL_INSTANCE_NAME, actual.getAsInstanceName());

        assertEquals(CLOUD_OWNER, actual.getAdditionalParams().get(CLOUD_OWNER_PARAM_KEY).toString());
        assertEquals(LCP_CLOUD_REGION_ID, actual.getAdditionalParams().get(CLOUD_REGION_PARAM_KEY).toString());
        assertEquals(StubbedBuildingBlockExecution.getTenantId(),
                actual.getAdditionalParams().get(TENANT_ID_PARAM_KEY).toString());
        assertEquals(NAME_SPACE, actual.getAdditionalParams().get(NAMESPACE_KEY).toString());

    }

    @Test
    public void testCreateCreateASRequest_ForBBThrowsException_exceptionBuilderCalled() throws Exception {

        final CnfInstantiateTask objUnderTest = getCnfInstantiateTask();

        final BuildingBlockExecution stubbedExecutionNoReqDetails = new StubbedBuildingBlockExecution();
        final ExecuteBuildingBlock executeBuildingBlock = stubbedExecutionNoReqDetails.getVariable("buildingBlock");
        executeBuildingBlock.setRequestDetails(null);

        doThrow(BpmnError.class).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class),
                eq(2000), anyString(), any());
        objUnderTest.createCreateAsRequest(stubbedExecutionNoReqDetails);
        final CreateAsRequest actual = stubbedExecutionNoReqDetails.getVariable(CREATE_AS_REQUEST_OBJECT);

        assertNull(actual);
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(2000), anyString(),
                any());

    }

    @Test
    public void invokeCnfmWithCreateAsRequest_validValues_storesResponseInExecution() throws Exception {

        final CnfInstantiateTask objUnderTest = getCnfInstantiateTask();
        stubbedExecution.setVariable(CREATE_AS_REQUEST_OBJECT, new CreateAsRequest());

        when(mockedCnfmHttpServiceProvider.invokeCreateAsRequest(any(CreateAsRequest.class)))
                .thenReturn(getAsInstance());

        objUnderTest.invokeCnfmWithCreateAsRequest(stubbedExecution);

        assertEquals(JOB_ID, stubbedExecution.getVariable(AS_INSTANCE_ID));
    }

    @Test
    public void invokeCnfmWithCreateAsRequest_ForBBThrowsException_exceptionBuilderCalled() throws Exception {

        final CnfInstantiateTask objUnderTest = getCnfInstantiateTask();
        stubbedExecution.setVariable(CREATE_AS_REQUEST_OBJECT, new CreateAsRequest());

        when(mockedCnfmHttpServiceProvider.invokeCreateAsRequest(any(CreateAsRequest.class)))
                .thenReturn(Optional.empty());
        doThrow(BpmnError.class).when(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class),
                eq(2003), anyString(), any());

        objUnderTest.invokeCnfmWithCreateAsRequest(stubbedExecution);

        assertNull(stubbedExecution.getVariable(AS_INSTANCE_ID));
        verify(exceptionUtil).buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(2003), anyString(),
                any());

    }

    @Test
    public void testcreateAsInstanceRequest_withValidValues_storesRequestInExecution() throws Exception {

        final CnfInstantiateTask objUnderTest = getCnfInstantiateTask();

        objUnderTest.createAsInstanceRequest(stubbedExecution);

        final InstantiateAsRequest actual = stubbedExecution.getVariable(INSTANTIATE_AS_REQUEST_OBJECT);
        assertNotNull(actual);
        assertNotNull(actual.getDeploymentItems());
        assertEquals(1, actual.getDeploymentItems().size());
        assertFalse(actual.getDeploymentItems().get(0).getDeploymentItemsId().isBlank());
    }

    private Optional<AsInstance> getAsInstance() {
        final AsInstance response = new AsInstance();
        response.setAsInstanceid(JOB_ID);
        return Optional.of(response);
    }

    private CnfInstantiateTask getCnfInstantiateTask() {
        return new CnfInstantiateTask(mockedCnfmHttpServiceProvider, exceptionUtil);
    }
}
