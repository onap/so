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
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.ADDITIONAL_PARAMS_VALUE;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.EXT_VIRTUAL_LINKS_VALUE;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.EXT_VIRTUAL_LINK_ID;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.TestConstants.getUserParamsMap;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.ExternalVirtualLink;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.ExternalVirtualLinkCpConfig;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.ExternalVirtualLinkExtCps;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.exceptions.RequiredExecutionVariableExeception;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.InputParameterRetrieverTask;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.sdnc.SDNCClient;

/**
 * @author waqas.ikram@est.tech
 */
public class InputParameterRetrieverTaskTest extends BaseTaskTest {

    private static final String INSTANCE_TYPE_VALUE_1 = "m1.small";

    private static final String INSTANCE_TYPE_VALUE_2 = "m1.large";

    private static final String INSTANCE_TYPE = "instance_type";

    private static final String RANDOM_EXT_VIRTUAL_LINK_ID = UUID.randomUUID().toString();

    private static final String CPU_INSTANCE_ID = EXT_VIRTUAL_LINK_ID;

    private static final String FLAVOR_VALUE = "ubuntu";

    private static final String FLAVOR = "flavor_type";

    private final StubbedBuildingBlockExecution stubbedxecution = new StubbedBuildingBlockExecution();

    @Mock
    private InputParametersProvider<GenericVnf> sdncInputParametersProvider;

    private final InputParametersProvider<Map<String, Object>> userParamsinputParametersProvider =
            new UserParamInputParametersProvider();

    @Mock
    private SDNCClient mockedSdncClient;

    @Test
    public void testGetInputParameters_inputParameterStoredInExecutionContext() throws BBObjectNotFoundException {
        final InputParameterRetrieverTask objUnderTest = new InputParameterRetrieverTask(sdncInputParametersProvider,
                userParamsinputParametersProvider, extractPojosForBB);


        final GeneralBuildingBlock buildingBlock =
                getGeneralBuildingBlock(getUserParamsMap(ADDITIONAL_PARAMS_VALUE, null));
        stubbedxecution.setGeneralBuildingBlock(buildingBlock);

        final InputParameter inputParameter = new InputParameter(Collections.emptyMap(), Collections.emptyList());
        when(sdncInputParametersProvider.getInputParameter(Mockito.any(GenericVnf.class))).thenReturn(inputParameter);

        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(new GenericVnf());
        objUnderTest.getInputParameters(stubbedxecution);

        final Object actual = stubbedxecution.getVariable(Constants.INPUT_PARAMETER);
        assertNotNull(actual);
        assertTrue(actual instanceof InputParameter);
        final InputParameter actualInputParameter = (InputParameter) actual;
        final Map<String, String> actualAdditionalParams = actualInputParameter.getAdditionalParams();
        assertEquals(3, actualAdditionalParams.size());

        final String actualInstanceType = actualAdditionalParams.get(INSTANCE_TYPE);
        assertEquals(INSTANCE_TYPE_VALUE_1, actualInstanceType);

    }

    @Test
    public void testGetInputParameters_ThrowExecption_NullInputParameterStoredInExecutionContext()
            throws BBObjectNotFoundException {
        final InputParameterRetrieverTask objUnderTest = new InputParameterRetrieverTask(sdncInputParametersProvider,
                userParamsinputParametersProvider, extractPojosForBB);

        when(sdncInputParametersProvider.getInputParameter(Mockito.any(GenericVnf.class)))
                .thenThrow(RuntimeException.class);
        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(new GenericVnf());
        objUnderTest.getInputParameters(stubbedxecution);

        final Object actual = stubbedxecution.getVariable(Constants.INPUT_PARAMETER);
        assertNotNull(actual);
        assertTrue(actual instanceof NullInputParameter);
    }

    @Test
    public void testGetInputParameters_SdncAndUserParamInputParameterStoredInExecutionContext()
            throws BBObjectNotFoundException {
        final InputParameterRetrieverTask objUnderTest = new InputParameterRetrieverTask(sdncInputParametersProvider,
                userParamsinputParametersProvider, extractPojosForBB);


        final GeneralBuildingBlock buildingBlock =
                getGeneralBuildingBlock(getUserParamsMap(ADDITIONAL_PARAMS_VALUE, EXT_VIRTUAL_LINKS_VALUE));
        stubbedxecution.setGeneralBuildingBlock(buildingBlock);

        final InputParameter inputParameter = new InputParameter(getAdditionalParams(), getExternalVirtualLink());
        when(sdncInputParametersProvider.getInputParameter(Mockito.any(GenericVnf.class))).thenReturn(inputParameter);

        when(extractPojosForBB.extractByKey(any(), eq(ResourceKey.GENERIC_VNF_ID))).thenReturn(new GenericVnf());
        objUnderTest.getInputParameters(stubbedxecution);

        final Object actual = stubbedxecution.getVariable(Constants.INPUT_PARAMETER);
        assertNotNull(actual);
        assertTrue(actual instanceof InputParameter);
        final InputParameter actualInputParameter = (InputParameter) actual;
        final Map<String, String> actualAdditionalParams = actualInputParameter.getAdditionalParams();
        assertEquals(4, actualAdditionalParams.size());

        final String actualInstanceType = actualAdditionalParams.get(INSTANCE_TYPE);
        assertEquals(INSTANCE_TYPE_VALUE_1, actualInstanceType);

        assertEquals(FLAVOR_VALUE, actualAdditionalParams.get(FLAVOR));
        final List<ExternalVirtualLink> actualExtVirtualLinks = actualInputParameter.getExtVirtualLinks();
        assertEquals(2, actualExtVirtualLinks.size());

        final Optional<ExternalVirtualLink> externalVirtualLink0 = actualExtVirtualLinks.stream()
                .filter(extVirtualLink -> EXT_VIRTUAL_LINK_ID.equals(extVirtualLink.getId())).findAny();
        assertTrue(externalVirtualLink0.isPresent());
        assertEquals(EXT_VIRTUAL_LINK_ID, externalVirtualLink0.get().getId());

        final Optional<ExternalVirtualLink> externalVirtualLink1 = actualExtVirtualLinks.stream()
                .filter(extVirtualLink -> RANDOM_EXT_VIRTUAL_LINK_ID.equals(extVirtualLink.getId())).findAny();
        assertTrue(externalVirtualLink1.isPresent());
        assertEquals(RANDOM_EXT_VIRTUAL_LINK_ID, externalVirtualLink1.get().getId());


    }

    private List<ExternalVirtualLink> getExternalVirtualLink() {
        return Arrays.asList(
                new ExternalVirtualLink().id(RANDOM_EXT_VIRTUAL_LINK_ID).addExtCpsItem(new ExternalVirtualLinkExtCps()
                        .addCpConfigItem(new ExternalVirtualLinkCpConfig().cpInstanceId(CPU_INSTANCE_ID))));
    }

    private Map<String, String> getAdditionalParams() {
        final Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put(FLAVOR, FLAVOR_VALUE);
        additionalParams.put(INSTANCE_TYPE, INSTANCE_TYPE_VALUE_2);
        return additionalParams;
    }


    private GeneralBuildingBlock getGeneralBuildingBlock(final Map<String, Object> userParams) {
        final GeneralBuildingBlock buildingBlock = new GeneralBuildingBlock();
        final RequestContext requestContext = new RequestContext();
        final RequestParameters requestParameters = new RequestParameters();
        requestParameters.setUserParams(Arrays.asList(userParams));
        requestContext.setRequestParameters(requestParameters);
        buildingBlock.setRequestContext(requestContext);
        return buildingBlock;

    }

    private class StubbedBuildingBlockExecution implements BuildingBlockExecution {

        private final Map<String, Serializable> execution = new HashMap<>();
        private GeneralBuildingBlock buildingBlock;

        private void setGeneralBuildingBlock(final GeneralBuildingBlock buildingBlock) {
            this.buildingBlock = buildingBlock;
        }

        @Override
        public GeneralBuildingBlock getGeneralBuildingBlock() {
            return buildingBlock;
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

        @Override
        public int getCurrentSequence() {
            return 0;
        }

    }
}
