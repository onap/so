/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia.
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

package org.onap.so.bpmn.buildingblock;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.so.bpmn.buildingblock.HomingV2.HOMING_OOF;
import static org.onap.so.bpmn.buildingblock.HomingV2.HOMING_SNIRO;
import static org.onap.so.bpmn.buildingblock.HomingV2.HOMING_SOLUTION;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;

public class HomingV2Test {

    @Test
    public void whenHomingSolutionSetToSniroShouldCallSniro() {
        HashMap<String, Object> userParams = new HashMap<>();
        userParams.put(HOMING_SOLUTION, HOMING_SNIRO);
        BuildingBlockExecution givenExecution = createBuildingBlockExecutionMock(userParams);

        OofHomingV2 oofHoming = mock(OofHomingV2.class);
        SniroHomingV2 sniroHoming = mock(SniroHomingV2.class);

        HomingV2 homingV2 = new HomingV2(oofHoming, sniroHoming);
        homingV2.callHoming(givenExecution);

        then(sniroHoming).should().callSniro(givenExecution);
        then(oofHoming).shouldHaveNoInteractions();
    }

    @Test
    public void whenHomingSolutionSetToSniroShouldProcessSniro() {
        HashMap<String, Object> userParams = new HashMap<>();
        userParams.put(HOMING_SOLUTION, HOMING_SNIRO);
        BuildingBlockExecution givenExecution = createBuildingBlockExecutionMock(userParams);

        OofHomingV2 oofHoming = mock(OofHomingV2.class);
        SniroHomingV2 sniroHoming = mock(SniroHomingV2.class);

        HomingV2 homingV2 = new HomingV2(oofHoming, sniroHoming);
        homingV2.processSolution(givenExecution, "dummy");

        then(sniroHoming).should().processSolution(givenExecution, "dummy");
        then(oofHoming).shouldHaveNoInteractions();
    }

    @Test
    public void whenHomingSolutionSetToOofShouldCallOof() {
        HashMap<String, Object> userParams = new HashMap<>();
        userParams.put(HOMING_SOLUTION, HOMING_OOF);
        BuildingBlockExecution givenExecution = createBuildingBlockExecutionMock(userParams);

        OofHomingV2 oofHoming = mock(OofHomingV2.class);
        SniroHomingV2 sniroHoming = mock(SniroHomingV2.class);

        HomingV2 homingV2 = new HomingV2(oofHoming, sniroHoming);
        homingV2.callHoming(givenExecution);

        then(oofHoming).should().callOof(givenExecution);
        then(sniroHoming).shouldHaveNoInteractions();
    }

    @Test
    public void whenHomingSolutionSetToOofShouldProcessOof() {
        HashMap<String, Object> userParams = new HashMap<>();
        userParams.put(HOMING_SOLUTION, HOMING_OOF);
        BuildingBlockExecution givenExecution = createBuildingBlockExecutionMock(userParams);

        OofHomingV2 oofHoming = mock(OofHomingV2.class);
        SniroHomingV2 sniroHoming = mock(SniroHomingV2.class);

        HomingV2 homingV2 = new HomingV2(oofHoming, sniroHoming);
        homingV2.processSolution(givenExecution, "dummy");

        then(oofHoming).should().processSolution(givenExecution, "dummy");
        then(sniroHoming).shouldHaveNoInteractions();
    }

    @Test
    public void whenHomingSolutionNotSetShouldCallOof() {
        BuildingBlockExecution givenExecution = createBuildingBlockExecutionMock(new HashMap<>());

        OofHomingV2 oofHoming = mock(OofHomingV2.class);
        SniroHomingV2 sniroHoming = mock(SniroHomingV2.class);

        HomingV2 homingV2 = new HomingV2(oofHoming, sniroHoming);
        homingV2.callHoming(givenExecution);

        then(oofHoming).should().callOof(givenExecution);
        then(sniroHoming).shouldHaveNoInteractions();
    }

    @Test
    public void whenHomingSolutionNotSetShouldProcessOof() {
        BuildingBlockExecution givenExecution = createBuildingBlockExecutionMock(new HashMap<>());

        OofHomingV2 oofHoming = mock(OofHomingV2.class);
        SniroHomingV2 sniroHoming = mock(SniroHomingV2.class);

        HomingV2 homingV2 = new HomingV2(oofHoming, sniroHoming);
        homingV2.processSolution(givenExecution, "dummy");

        then(oofHoming).should().processSolution(givenExecution, "dummy");
        then(sniroHoming).shouldHaveNoInteractions();
    }

    private BuildingBlockExecution createBuildingBlockExecutionMock(Map<String, Object> userParams) {
        BuildingBlockExecution execution = mock(BuildingBlockExecution.class);
        GeneralBuildingBlock generalBuildingBlock = mock(GeneralBuildingBlock.class);
        RequestContext requestContext = mock(RequestContext.class);
        RequestParameters requestParameters = mock(RequestParameters.class);
        List<Map<String, Object>> userParamsList = new ArrayList<>();

        userParamsList.add(userParams);

        when(execution.getGeneralBuildingBlock()).thenReturn(generalBuildingBlock);
        when(generalBuildingBlock.getRequestContext()).thenReturn(requestContext);
        when(requestContext.getRequestParameters()).thenReturn(requestParameters);
        when(requestParameters.getUserParams()).thenReturn(userParamsList);

        return execution;
    }
}
