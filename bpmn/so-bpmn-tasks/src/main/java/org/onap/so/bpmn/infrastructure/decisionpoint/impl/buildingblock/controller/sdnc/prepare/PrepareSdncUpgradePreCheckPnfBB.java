/*
 * ============LICENSE_START======================================================= Copyright (C) 2019 Nordix
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 *
 * SPDX-License-Identifier: Apache-2.0 ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.sdnc.prepare;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.springframework.stereotype.Component;

/**
 * This class is used to prepare the {@ref ControllerContext} for SDNC UpgradePreCheck action.
 */
@Component
public class PrepareSdncUpgradePreCheckPnfBB extends PrepareSdncBB {

    @Override
    public boolean understand(ControllerContext<BuildingBlockExecution> controllerContext) {
        return super.understand(controllerContext) && controllerContext.getControllerAction().equals("UpgradePreCheck")
                && controllerContext.getControllerScope().equalsIgnoreCase("pnf");
    }

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> controllerContext) {

        String payload = controllerContext.getExecution().getVariable("payload");
        String actualPayLoad = constructPayload(payload, controllerContext.getControllerAction());

        controllerContext.getExecution().setVariable("payload", actualPayLoad);
    }

    private String constructPayload(String payload, String action) {

        JSONObject jsonObject = new JSONObject(payload);
        if (jsonObject.has("action")) {
            JSONArray jsonArray = jsonObject.getJSONArray("action");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                if (jsonObject1.has(action)) {
                    return jsonObject1.toString();
                }
            }
        }
        return payload;
    }
}

