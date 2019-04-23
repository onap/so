/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) Copyright (C) 2018 Bell Canada.
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

import java.util.Map;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HomingV2 {

    @Autowired
    private OofHomingV2 oofHomingV2;
    @Autowired
    private SniroHomingV2 sniroHomingV2;

    private static final String HOMINGSOLUTION = "Homing_Solution";

    public void callHoming(BuildingBlockExecution execution) {
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

    // Default solution is SNIRO. OOF gets called only if specified.
    private boolean isOof(BuildingBlockExecution execution) {
        for (Map<String, Object> params : execution.getGeneralBuildingBlock().getRequestContext().getRequestParameters()
                .getUserParams()) {
            if (params.containsKey(HOMINGSOLUTION) && ("oof").equals(params.get(HOMINGSOLUTION))) {
                return true;
            }
        }
        return false;
    }
}
