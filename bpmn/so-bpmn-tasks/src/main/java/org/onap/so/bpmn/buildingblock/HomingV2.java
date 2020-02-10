/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Bell Canada.
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

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HomingV2 {

    public static final String HOMING_SNIRO = "sniro";
    public static final String HOMING_OOF = "oof";
    public static final String HOMING_SOLUTION = "Homing_Solution";

    private SniroHomingV2 sniroHomingV2;
    private OofHomingV2 oofHomingV2;

    @Autowired
    public HomingV2(OofHomingV2 oofHomingV2, SniroHomingV2 sniroHomingV2) {
        this.oofHomingV2 = oofHomingV2;
        this.sniroHomingV2 = sniroHomingV2;
    }

    public void callHoming(BuildingBlockExecution execution) {
        if (isSniro(execution)) {
            sniroHomingV2.callSniro(execution);
        } else {
            oofHomingV2.callOof(execution);
        }
    }

    public void processSolution(BuildingBlockExecution execution, String asyncResponse) {
        if (isSniro(execution)) {
            sniroHomingV2.processSolution(execution, asyncResponse);
        } else {
            oofHomingV2.processSolution(execution, asyncResponse);
        }
    }

    private boolean isSniro(BuildingBlockExecution execution) {
        return execution.getGeneralBuildingBlock().getRequestContext().getRequestParameters().getUserParams().stream()
                .anyMatch(params -> HomingV2.HOMING_SNIRO.equals(params.get(HomingV2.HOMING_SOLUTION)));
    }
}
