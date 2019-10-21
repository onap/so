/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Bell Canada
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

import com.google.gson.JsonObject;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.PayloadGenerationException;
import java.util.Optional;
import static org.onap.so.client.cds.PayloadConstants.PROPERTIES;
import static org.onap.so.client.cds.PayloadConstants.SEPARATOR;

public class ServiceCDSRequestProvider implements CDSRequestProvider {

    private static final String EMPTY_STRING = "";
    private ExtractPojosForBB extractPojosForBB;
    private String resolutionKey;
    private BuildingBlockExecution execution;
    private JsonObject serviceObject;

    ServiceCDSRequestProvider(ExtractPojosForBB extractPojosForBB) {
        this.extractPojosForBB = extractPojosForBB;
    }

    @Override
    public Optional<String> buildRequestPayload(String action) throws PayloadGenerationException {
        JsonObject cdsPropertyObject = new JsonObject();
        serviceObject = buildPropertyObjectforService();

        cdsPropertyObject.addProperty("resolution-key", resolutionKey);
        cdsPropertyObject.add(action + SEPARATOR + PROPERTIES, serviceObject);

        return Optional.of(buildRequestJsonObject(cdsPropertyObject, action));
    }

    @Override
    public String getBlueprintName() {
        return EMPTY_STRING;
    }

    @Override
    public String getBlueprintVersion() {
        return EMPTY_STRING;
    }

    @Override
    public <T> void setExecutionObject(T executionObject) {
        execution = (BuildingBlockExecution) executionObject;
    }

    private JsonObject buildPropertyObjectforService() throws PayloadGenerationException {
        JsonObject serviceObject = new JsonObject();
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);

            resolutionKey = serviceInstance.getServiceInstanceName();

            // TODO Need to figure out how to populate blueprint name and version for service.

            serviceObject.addProperty("service-instance-id", serviceInstance.getServiceInstanceId());
            serviceObject.addProperty("service-model-uuid",
                    serviceInstance.getModelInfoServiceInstance().getModelUuid());

        } catch (Exception e) {
            throw new PayloadGenerationException("Failed to buildPropertyObjectForService", e);
        }

        return serviceObject;
    }
}
