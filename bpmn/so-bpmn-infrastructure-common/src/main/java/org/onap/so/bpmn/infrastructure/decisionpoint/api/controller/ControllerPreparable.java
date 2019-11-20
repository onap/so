/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
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

package org.onap.so.bpmn.infrastructure.decisionpoint.api.controller;

import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;

/**
 * This interface is used by {@link ControllerRunnable} to prepare the Context.
 *
 * The interface should be implementation by controller preparation instance to configure the context required for
 * execution.
 */
public interface ControllerPreparable<T> {

    /**
     * This method is used to decide whether the implementation is used to configure the {@link ControllerContext}.
     *
     * @param controllerContext
     * @return
     */
    boolean understand(final ControllerContext<T> controllerContext);

    /**
     * This method is used to prepare the {@link ControllerContext}.
     *
     * @param controllerContext
     */
    void prepare(final ControllerContext<T> controllerContext);

}
