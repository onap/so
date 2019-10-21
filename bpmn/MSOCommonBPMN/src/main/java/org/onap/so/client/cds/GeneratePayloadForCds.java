/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
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

package org.onap.so.client.cds;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.cds.beans.AbstractCDSPropertiesBean;
import java.util.Optional;

public class GeneratePayloadForCds {

    private BuildingBlockExecution execution;
    private String scope;
    private String action;
    private ExtractPojosForBB extractPojosForBB;
    private static final String BUILDING_BLOCK = "buildingBlock";

    public GeneratePayloadForCds(BuildingBlockExecution execution, ExtractPojosForBB pojosForBB) {
        this.execution = execution;
        this.extractPojosForBB = pojosForBB;
        ExecuteBuildingBlock executeBuildingBlock = execution.getVariable(BUILDING_BLOCK);
        BuildingBlock buildingBlock = executeBuildingBlock.getBuildingBlock();
        this.scope = buildingBlock.getBpmnScope();
        this.action = buildingBlock.getBpmnAction();
    }

    /**
     * Build the payload based on SO scope and action.
     * 
     * @return Payload to push to CDS.
     */
    public Optional<String> buildPayloadForCds() {
        return generateConfigPropertiesPayload();
    }

    private Optional<String> generateConfigPropertiesPayload() {
        try {

            switch (scope) {
                case "vnf":
                    Actor configVnf = new ConfigVnf();
                    configVnf.setAction(action);
                    return configVnf.buildRequestPayload(extractPojosForBB, execution);

                case "vf-module":
                    Actor configVfModule = new ConfigVfModule();
                    configVfModule.setAction(action);
                    return configVfModule.buildRequestPayload(extractPojosForBB, execution);
            }
        }

        catch (Exception ex) {
            // TODO error handling
        }
        return Optional.empty();
    }

    // TODO buildCdsPropertiesBean
    // public AbstractCDSPropertiesBean buildCdsPropertiesBean() throws Exception {
    // final AbstractCDSPropertiesBean cdsPropertiesBean = new AbstractCDSPropertiesBean();
    //
    // String requestPayload = generateConfigPropertiesPayload().orElseThrow(() -> new Exception("Failed to build
    // payload for CDS"));
    //
    // cdsPropertiesBean.setRequestObject(generateConfigPropertiesPayload().get());
    // }
}
