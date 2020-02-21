/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.onap.so.client.cds;

import com.google.gson.JsonObject;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Optional;
import static org.onap.so.client.cds.PayloadConstants.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PnfCDSRequestProvider implements CDSRequestProvider {
    private String blueprintName;
    private String blueprintVersion;
    private DelegateExecution execution;

    @Override
    public String getBlueprintName() {
        return blueprintName;
    }

    @Override
    public String getBlueprintVersion() {
        return blueprintVersion;
    }

    @Override
    public <T> void setExecutionObject(T executionObject) {
        execution = (DelegateExecution) executionObject;
    }

    @Override
    public Optional<String> buildRequestPayload(String action) {

        final JsonObject pnfObject = new JsonObject();
        final String resolutionKey = String.valueOf(execution.getVariable(PNF_CORRELATION_ID));
        blueprintName = String.valueOf(execution.getVariable(PRC_BLUEPRINT_NAME));
        blueprintVersion = String.valueOf(execution.getVariable(PRC_BLUEPRINT_VERSION));

        extractAndSetExecutionVariable("service-instance-id", SERVICE_INSTANCE_ID, pnfObject);
        extractAndSetExecutionVariable("service-model-uuid", MODEL_UUID, pnfObject);
        extractAndSetExecutionVariable("pnf-id", PNF_UUID, pnfObject);
        extractAndSetExecutionVariable("pnf-name", PNF_CORRELATION_ID, pnfObject);
        extractAndSetExecutionVariable("pnf-customization-uuid", PRC_CUSTOMIZATION_UUID, pnfObject);
        extractAndSetExecutionVariable("target-software-version", PRC_TARGET_SOFTWARE_VERSION, pnfObject);

        final JsonObject cdsPropertyObject = new JsonObject();
        cdsPropertyObject.addProperty(RESOLUTION_KEY, resolutionKey);
        cdsPropertyObject.add(action + SEPARATOR + PROPERTIES, pnfObject);

        return Optional.of(buildRequestJsonObject(cdsPropertyObject, action));
    }

    private void extractAndSetExecutionVariable(String jsonProperty, String executionProperty, JsonObject pnfObject) {
        if (execution.getVariable(executionProperty) != null) {
            pnfObject.addProperty(jsonProperty, String.valueOf(execution.getVariable(executionProperty)));
        }
    }
}
