package org.onap.so.bpmn.buildingblock;

import fj.data.$;
import java.util.Map;
import java.util.Map.Entry;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.InjectExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HomingV2 {

    @Autowired
    private OofHomingV2 oofHomingV2;
    @Autowired
    private SniroHomingV2 sniroHomingV2;

    private static final String HOMINGSOLUTION = "Homing_Solution";

    public void handleHomingBB(BuildingBlockExecution execution) {
        if (isOof(execution)) {
            oofHomingV2.callOof(execution);
        } else {
            sniroHomingV2.callSniro(execution);
        }
    }

    public void processSolution(BuildingBlockExecution execution, String asyncResponse) {
        if (isOof(execution)) {
            oofHomingV2.processSolution(execution, asyncResponse);
        } else {
            sniroHomingV2.processSolution(execution, asyncResponse);
        }
    }

    private boolean isOof(BuildingBlockExecution execution) {
        for (Map<String, Object> params : execution.getGeneralBuildingBlock().getRequestContext().getRequestParameters()
            .getUserParams()) {
            if (params.containsKey(HOMINGSOLUTION) && params.get(HOMINGSOLUTION).equals("oof")) {
                return true;
            }
        }
        return false;
    }
}
