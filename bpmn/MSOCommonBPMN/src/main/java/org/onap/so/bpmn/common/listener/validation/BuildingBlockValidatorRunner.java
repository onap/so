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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;


/**
 * Controls running all pre and post validation for building blocks.
 * 
 * To define a validation you must make it a spring bean and implement either
 * {@link org.onap.so.bpmn.common.listener.validation.PreBuildingBlockValidator} or
 * {@link org.onap.so.bpmn.common.listener.validation.PostBuildingBlockValidator} your validation will automatically be
 * run by this class.
 *
 */
@Component
public class BuildingBlockValidatorRunner
        extends FlowValidatorRunner<PreBuildingBlockValidator, PostBuildingBlockValidator> {

    @PostConstruct
    protected void init() {

        preFlowValidators = new ArrayList<>(Optional.ofNullable(context.getBeansOfType(PreBuildingBlockValidator.class))
                .orElse(new HashMap<>()).values());
        postFlowValidators = new ArrayList<>(Optional
                .ofNullable(context.getBeansOfType(PostBuildingBlockValidator.class)).orElse(new HashMap<>()).values());
    }

    protected List<PreBuildingBlockValidator> getPreFlowValidators() {
        return this.preFlowValidators;
    }

    protected List<PostBuildingBlockValidator> getPostFlowValidators() {
        return this.postFlowValidators;
    }

}
