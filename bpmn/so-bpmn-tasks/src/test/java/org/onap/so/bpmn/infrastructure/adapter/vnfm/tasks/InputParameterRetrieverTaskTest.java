/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.exceptions.RequiredExecutionVariableExeception;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.InputParameter;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.InputParametersProvider;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils.NullInputParameter;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.client.exception.BBObjectNotFoundException;

/**
 * @author waqas.ikram@est.tech
 */
public class InputParameterRetrieverTaskTest extends BaseTaskTest {

    private final BuildingBlockExecution stubbedxecution = new StubbedBuildingBlockExecution();

    @Mock
    private InputParametersProvider inputParametersProvider;

    @Test
    public void testGGetInputParameters_inputParameterStoredInExecutionContext() throws BBObjectNotFoundException {
        final InputParameterRetrieverTask objUnderTest =
                new InputParameterRetrieverTask(inputParametersProvider, extractPojosForBB);

        final InputParameter inputParameter = new InputParameter(Collections.emptyMap(), Collections.emptyList());
        when(inputParametersProvider.getInputParameter(Mockito.any(GenericVnf.class))).thenReturn(inputParameter);
        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(new GenericVnf());
        objUnderTest.getInputParameters(stubbedxecution);

        final Object actual = stubbedxecution.getVariable(Constants.INPUT_PARAMETER);
        assertNotNull(actual);
        assertTrue(actual instanceof InputParameter);
    }

    @Test
    public void testGGetInputParameters_ThrowExecption_NullInputParameterStoredInExecutionContext()
            throws BBObjectNotFoundException {
        final InputParameterRetrieverTask objUnderTest =
                new InputParameterRetrieverTask(inputParametersProvider, extractPojosForBB);

        when(inputParametersProvider.getInputParameter(Mockito.any(GenericVnf.class)))
                .thenThrow(RuntimeException.class);
        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(new GenericVnf());
        objUnderTest.getInputParameters(stubbedxecution);

        final Object actual = stubbedxecution.getVariable(Constants.INPUT_PARAMETER);
        assertNotNull(actual);
        assertTrue(actual instanceof NullInputParameter);
    }


    private class StubbedBuildingBlockExecution implements BuildingBlockExecution {

        private final Map<String, Serializable> execution = new HashMap<>();

        @Override
        public GeneralBuildingBlock getGeneralBuildingBlock() {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getVariable(final String key) {
            return (T) execution.get(key);
        }

        @Override
        public <T> T getRequiredVariable(final String key) throws RequiredExecutionVariableExeception {
            return null;
        }

        @Override
        public void setVariable(final String key, final Serializable value) {
            execution.put(key, value);
        }

        @Override
        public Map<ResourceKey, String> getLookupMap() {
            return Collections.emptyMap();
        }

        @Override
        public String getFlowToBeCalled() {
            return null;
        }

    }
}
