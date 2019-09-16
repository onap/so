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
        then(oofHoming).shouldHaveZeroInteractions();
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
        then(sniroHoming).shouldHaveZeroInteractions();
    }

    @Test
    public void whenHomingSolutionNotSetShouldCallOof() {
        BuildingBlockExecution givenExecution = createBuildingBlockExecutionMock(new HashMap<>());

        OofHomingV2 oofHoming = mock(OofHomingV2.class);
        SniroHomingV2 sniroHoming = mock(SniroHomingV2.class);

        HomingV2 homingV2 = new HomingV2(oofHoming, sniroHoming);
        homingV2.callHoming(givenExecution);

        then(oofHoming).should().callOof(givenExecution);
        then(sniroHoming).shouldHaveZeroInteractions();
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