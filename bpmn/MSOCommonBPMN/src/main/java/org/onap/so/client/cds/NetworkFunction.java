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

import java.util.Optional;

public interface NetworkFunction {

    /**
     * Build entire payload for CDS.
     *
     * @param action - action could be assign/deploy/undeploy etc.
     * @return "payload":{ "config-<action>-<scope>":{ // information about resolution key, property configuration and
     *         template prefix based on the scope and action}
     * @throws Exception If fail to build the payload.
     */
    Optional<String> buildRequestPayload(String action) throws Exception;

    /**
     * Get the blueprint name for CDS payload
     *
     * @return blueprint name
     */
    String getBlueprintName();

    /**
     * Get the blueprint version for CDS payload
     *
     * @return blueprint version
     */
    String getBlueprintVersion();

    /**
     * Set the executionObject(BuildingBlockExecution or DelegateExecution for PNF)
     *
     * @param executionObject object could be BuildingBlockExecution or DelegateExecution.
     */
    <T> void setExecutionObject(T executionObject);
}
