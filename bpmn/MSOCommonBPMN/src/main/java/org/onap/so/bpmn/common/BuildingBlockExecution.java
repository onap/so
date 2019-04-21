/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common;

import java.io.Serializable;
import java.util.Map;
import org.onap.so.bpmn.common.exceptions.RequiredExecutionVariableExeception;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;

public interface BuildingBlockExecution {

    public GeneralBuildingBlock getGeneralBuildingBlock();

    public <T> T getVariable(String key);

    public <T> T getRequiredVariable(String key) throws RequiredExecutionVariableExeception;

    public void setVariable(String key, Serializable value);

    public Map<ResourceKey, String> getLookupMap();

    public String getFlowToBeCalled();
}
