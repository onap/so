/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
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
import org.onap.so.client.exception.PayloadGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class GeneratePayloadForCds {

    private static final String ORIGINATOR_ID = "SO";
    private static final String MODE = "sync";
    private static final String BUILDING_BLOCK = "buildingBlock";

    @Autowired
    private VnfCDSRequestProvider vnfCDSRequestProvider;

    @Autowired
    private VfModuleCDSRequestProvider vfModuleCDSRequestProvider;

    @Autowired
    private ServiceCDSRequestProvider serviceCDSRequestProvider;

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    /**
     * Build properties like (blueprint name, version, action etc..) along with the request payload.
     *
     * @param execution - A building block execution object.
     * @return AbstractCDSPropertiesBean - A POJO which contains CDS related information.
     * @throws PayloadGenerationException - Throw an exception if it fails to build payload for CDS.
     */
    public AbstractCDSPropertiesBean buildCdsPropertiesBean(BuildingBlockExecution execution)
            throws PayloadGenerationException {

        ExecuteBuildingBlock executeBuildingBlock = execution.getVariable(BUILDING_BLOCK);
        BuildingBlock buildingBlock = executeBuildingBlock.getBuildingBlock();
        String scope = buildingBlock.getBpmnScope();
        String action = buildingBlock.getBpmnAction();


        CDSRequestProvider requestProvider = getRequestProviderByScope(scope);
        requestProvider.setExecutionObject(execution);

        final String requestPayload = requestProvider.buildRequestPayload(action)
                .orElseThrow(() -> new PayloadGenerationException("Failed to build payload for CDS"));

        final String requestId = execution.getGeneralBuildingBlock().getRequestContext().getMsoRequestId();
        final AbstractCDSPropertiesBean cdsPropertiesBean = new AbstractCDSPropertiesBean();
        cdsPropertiesBean.setRequestObject(requestPayload);
        cdsPropertiesBean.setBlueprintName(requestProvider.getBlueprintName());
        cdsPropertiesBean.setBlueprintVersion(requestProvider.getBlueprintVersion());
        cdsPropertiesBean.setRequestId(requestId);
        cdsPropertiesBean.setOriginatorId(ORIGINATOR_ID);
        cdsPropertiesBean.setSubRequestId(UUID.randomUUID().toString());
        cdsPropertiesBean.setActionName(action);
        cdsPropertiesBean.setMode(MODE);

        return cdsPropertiesBean;
    }

    private CDSRequestProvider getRequestProviderByScope(String scope) throws PayloadGenerationException {
        CDSRequestProvider requestProvider;
        switch (scope) {
            case "vnf":
                requestProvider = vnfCDSRequestProvider;
                break;
            case "vfModule":
                requestProvider = vfModuleCDSRequestProvider;
                break;
            case "service":
                requestProvider = serviceCDSRequestProvider;
                break;
            default:
                throw new PayloadGenerationException("No scope defined with " + scope);
        }
        return requestProvider;
    }
}
