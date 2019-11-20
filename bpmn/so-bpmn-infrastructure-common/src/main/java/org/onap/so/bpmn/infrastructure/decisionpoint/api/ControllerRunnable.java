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
package org.onap.so.bpmn.infrastructure.decisionpoint.api;

/**
 * the ControllerRunnable interface should be implemented by any class intended to execute against controller northbound
 * interface.
 *
 */
public interface ControllerRunnable<T> {

    /**
     * This method is used to decide whether the implementation is to serve the controller northbound interface.
     *
     * @param context {@link ControllerContext} is used as the input to the execution.
     * @return
     */
    Boolean understand(final ControllerContext<T> context);

    /**
     * this method is used to check whether the controller Northbound interface is ready to use.
     *
     * @param context {@link ControllerContext} is used as the input to the execution.
     * @return True if the controller is ready to use or return false.
     */
    Boolean ready(final ControllerContext<T> context);

    /**
     * This method is used to set up the context so it can be used to run against the controller NB.
     */
    void prepare(final ControllerContext<T> context);

    /**
     * This method is used to run against the controller northbound interface.
     *
     * @param context {@link ControllerContext} is used as the input to the execution.
     */
    void run(final ControllerContext<T> context);

}
