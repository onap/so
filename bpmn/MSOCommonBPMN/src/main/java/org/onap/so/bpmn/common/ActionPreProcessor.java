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

package org.onap.so.bpmn.common;


import org.camunda.bpm.engine.delegate.DelegateExecution;

/**
 * interface to be extended by the classes, where pre processing is required
 */
public interface ActionPreProcessor {

    /**
     * method to bind and return the action
     *
     * @return
     */
    String getAction();

    /**
     * method to preform certain pre processing task before BB execution
     *
     * @param execution
     * @return
     */
    boolean process(DelegateExecution execution);
}
