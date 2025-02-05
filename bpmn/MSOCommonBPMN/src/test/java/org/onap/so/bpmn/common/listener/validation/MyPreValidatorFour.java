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

import java.util.Collections;
import java.util.Optional;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.springframework.stereotype.Component;

@Component
public class MyPreValidatorFour implements PreBuildingBlockValidator {

    @Override
    public boolean shouldRunFor(String bbName) {
        return Collections.singleton("test2").contains(bbName);
    }

    @Override
    public Optional<String> validate(BuildingBlockExecution exeuction) {
        return Optional.of("my-error-four");
    }

}
