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

public class GeneratePayloadForService implements NetworkFunction {

    private ExtractPojosForBB extractPojosForBB;
    private JsonObject serviceObject;
    private String blueprintName;
    private String resolutionKey;
    private String blueprintVersion;
    private BuildingBlockExecution execution;


    GeneratePayloadForService(ExtractPojosForBB extractPojosForBB) {
        this.extractPojosForBB = extractPojosForBB;
    }

    @Override
    public Optional<String> buildRequestPayload(String action) throws PayloadGenerationException {
        serviceObject = buildPropertyObjectforService();

        String requestBuilder = "{\"" + action + "-" + "request\":{" + "\"resolution-key\":" + "\"" + resolutionKey
                + "\"," + PropertyPayloadBuilder.buildConfigProperties(action, serviceObject) + '}';

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
        execution = (BuildingBlockExecution) executionObject;
    }

    private JsonObject buildPropertyObjectforService() throws PayloadGenerationException {
        try {
            serviceObject = new JsonObject();
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
