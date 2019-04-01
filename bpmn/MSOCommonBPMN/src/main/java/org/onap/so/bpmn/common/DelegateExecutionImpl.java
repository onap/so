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
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.common.exceptions.MalformedBuildingBlockInputException;
import org.onap.so.bpmn.common.exceptions.MissingBuildingBlockInputException;
import org.onap.so.bpmn.common.exceptions.RequiredExecutionVariableExeception;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DelegateExecutionImpl implements BuildingBlockExecution, Serializable {

    private static final long serialVersionUID = 5559067662634919395L;

    @JsonProperty
    private final Map<String, Serializable> seedMap;

    private transient DelegateExecution execution;
    private static final String INVALID_INPUT_MISSING = "Expected variable of \"%s\" not found in execution";

    private static final String MISSING_MSG =
            "Execution variable \"gBBInput\" cannot be null when executing building blocks";
    private static final String MALFORMED_MSG =
            "Execution variable \"gBBInput\" must contain an element of type GeneralBuildingBlock";

    public DelegateExecutionImpl(final Map<String, Serializable> seedMap) {
        this.seedMap = seedMap;
    }

    public DelegateExecutionImpl(final DelegateExecution execution) {
        this.seedMap = new HashMap<>();
        execution.getVariables().forEach((key, value) -> {
            if (value instanceof Serializable) {
                seedMap.put(key, (Serializable) value);
            }
        });
        /* must occur for junit tests to work */
        this.execution = execution;
    }

    @JsonIgnore
    @Override
    public GeneralBuildingBlock getGeneralBuildingBlock() {
        try {
            final GeneralBuildingBlock generalBuildingBlock = (GeneralBuildingBlock) execution.getVariable("gBBInput");

            if (generalBuildingBlock == null) {
                throw new MissingBuildingBlockInputException(MISSING_MSG);
            }

            return generalBuildingBlock;
        } catch (final ClassCastException e) {
            throw new MalformedBuildingBlockInputException(MALFORMED_MSG, e);
        }
    }

    @Override
    public <T> T getVariable(final String key) {
        return this.get(key);
    }

    @Override
    public <T> T getRequiredVariable(final String key) throws RequiredExecutionVariableExeception {
        final T result;

        result = this.get(key);
        if (result == null) {
            throw new RequiredExecutionVariableExeception(String.format(INVALID_INPUT_MISSING, key));

        }
        return result;
    }

    @Override
    public void setVariable(final String key, final Serializable value) {
        this.execution.setVariable(key, value);
    }

    @JsonIgnore
    @Override
    public Map<ResourceKey, String> getLookupMap() {
        return this.get("lookupKeyMap");
    }

    @JsonIgnore
    @Override
    public String getFlowToBeCalled() {
        return this.get("flowToBeCalled");
    }

    @JsonIgnore
    public DelegateExecution getDelegateExecution() {
        return this.execution;
    }

    public void setDelegateExecution(final DelegateExecution execution) {
        this.execution = execution;
        this.seedMap.forEach((key, value) -> {
            if (!execution.hasVariable(key)) {
                execution.setVariable(key, value);
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected <T> T get(final String key) {
        final Object value = this.execution.getVariable(key);
        return (T) value;
    }

}
