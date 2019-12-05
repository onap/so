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
import org.onap.so.client.exception.PayloadGenerationException;
import sun.nio.ch.Net;
import java.util.Optional;
import static org.onap.so.client.cds.PayloadConstants.*;

public class ConfigPnf implements NetworkFunction {
    private JsonObject pnfObject;
    private String resolutionKey;
    private String blueprintName;
    private String blueprintVersion;
    private String action;
    private DelegateExecution execution;

    @Override
    public Optional<String> buildRequestPayload(String action) throws PayloadGenerationException {

        this.action = action;
        JsonObject pnfObject = buildPropertyObjectForPnf(execution);
        String requestBuilder =
                "{\"" + action + SEPARATOR + PNF_SCOPE + REQUEST + "\":{" + "\"" + RESOLUTION_KEY + "\":" + "\""
                        + resolutionKey + "\"," + PropertyPayloadBuilder.buildConfigProperties(action, pnfObject) + '}';

        return Optional.of(requestBuilder);
    }


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

    private JsonObject buildPropertyObjectForPnf(final DelegateExecution execution) {
        pnfObject = new JsonObject();
        resolutionKey = String.valueOf(execution.getVariable(PNF_CORRELATION_ID));
        blueprintName = String.valueOf(execution.getVariable(PRC_BLUEPRINT_NAME));
        blueprintVersion = String.valueOf(execution.getVariable(PRC_BLUEPRINT_VERSION));

        pnfObject.addProperty("service-instance-id", String.valueOf(execution.getVariable(SERVICE_INSTANCE_ID)));
        pnfObject.addProperty("service-model-uuid", String.valueOf(execution.getVariable(MODEL_UUID)));
        pnfObject.addProperty("pnf-id", String.valueOf(execution.getVariable(PNF_UUID)));
        pnfObject.addProperty("pnf-name", String.valueOf(execution.getVariable(PNF_CORRELATION_ID)));
        pnfObject.addProperty("pnf-customization-uuid", String.valueOf(execution.getVariable(PRC_CUSTOMIZATION_UUID)));

        /**
         * add your customized properties here for specified actions.
         */
        switch (action) {
            case "sw-activate":
            case "sw-download":
                pnfObject.addProperty("software-version", String.valueOf(execution.getVariable("softwareVersion")));
                break;
        }
        return pnfObject;
    }
}
