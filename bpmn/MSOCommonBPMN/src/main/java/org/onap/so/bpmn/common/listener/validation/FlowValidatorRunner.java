/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.common.listener.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.listener.ListenerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Controls running all pre and post validation for flows.
 * 
 * To define a validation you must make it a spring bean and implement either
 * {@link org.onap.so.bpmn.common.validation.PreFlowValidator} or
 * {@link org.onap.so.bpmn.common.validation.PostFlowValidator} your validation will automatically be run by this class.
 *
 */
@Component
public abstract class FlowValidatorRunner<S extends FlowValidator, E extends FlowValidator> extends ListenerRunner {

    private static Logger logger = LoggerFactory.getLogger(FlowValidatorRunner.class);

    @Autowired
    protected ExceptionBuilder exceptionBuilder;

    protected List<S> preFlowValidators;
    protected List<E> postFlowValidators;



    /**
     * Changed to object because JUEL does not support overloaded methods
     * 
     * @param bbName
     * @param execution
     * @return
     */
    public boolean preValidate(String bbName, Object execution) {
        return validateHelper(bbName, preFlowValidators, execution);
    }

    /**
     * Changed to object because JUEL does not support overloaded methods
     * 
     * @param bbName
     * @param execution
     * @return
     */
    public boolean postValidate(String bbName, Object execution) {
        return validateHelper(bbName, postFlowValidators, execution);
    }

    protected boolean validateHelper(String bbName, List<? extends FlowValidator> validators, Object obj) {

        if (obj instanceof DelegateExecution) {
            return validate(validators, bbName, new DelegateExecutionImpl((DelegateExecution) obj));
        } else if (obj instanceof BuildingBlockExecution) {
            return validate(validators, bbName, (BuildingBlockExecution) obj);
        } else {
            return false;
        }
    }

    protected boolean validate(List<? extends FlowValidator> validators, String bbName,
            BuildingBlockExecution execution) {
        List<Pair<String, Optional<String>>> results = runValidations(validators, bbName, execution);

        if (!results.isEmpty()) {
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000,
                    "Failed Validations:\n" + results.stream()
                            .map(item -> String.format("%s: %s", item.getValue0(), item.getValue1().get()))
                            .collect(Collectors.joining("\n")));
        }

        return true;

    }

    protected List<Pair<String, Optional<String>>> runValidations(List<? extends FlowValidator> validators,
            String bbName, BuildingBlockExecution execution) {

        List<? extends FlowValidator> filtered = filterListeners(validators, (item -> item.shouldRunFor(bbName)));

        List<Pair<String, Optional<String>>> results = new ArrayList<>();
        filtered.forEach(item -> results.add(new Pair<>(item.getClass().getName(), item.validate(execution))));

        return results.stream().filter(item -> item.getValue1().isPresent()).collect(Collectors.toList());
    }

    protected abstract List<S> getPreFlowValidators();

    protected abstract List<E> getPostFlowValidators();

}
