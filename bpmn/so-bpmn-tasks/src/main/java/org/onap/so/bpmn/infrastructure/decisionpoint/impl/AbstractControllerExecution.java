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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl;

import com.google.common.base.Strings;
import java.util.List;
import java.util.stream.Collectors;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class of the controller execution, it should be extended for the controller execution task.
 * 
 * @param <T> execution context type, e.g., BuildingBlockExecution or DelegateExecution
 */
public abstract class AbstractControllerExecution<T> {

    /**
     * parameters from the execution context.
     */
    protected static final String CONTROLLER_ACTOR_PARAM = "actor";
    protected static final String CONTROLLER_ACTION_PARAM = "action";
    protected static final String CONTROLLER_SCOPE_PARAM = "scope";
    protected static final String RESOURCE_CUSTOMIZATION_UUID_PARAM = "resource_customization_uuid";
    protected static final String RESOURCE_TYPE_PARAM = "resource_type";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected List<ControllerRunnable<T>> availableControllerRunnables;

    @Autowired
    protected ExceptionBuilder exceptionBuilder;

    /**
     * Find the {@ref ControllerRunnable} instances understanding the {@ref ControllerContext}.
     *
     * Only one instance should be able to understand the context. if more than one or none instances found, return
     * null.
     *
     * @param context
     * @return
     */
    protected ControllerRunnable getController(ControllerContext<T> context) {
        List<ControllerRunnable<T>> satisfiedControllers = availableControllerRunnables.stream()
                .filter(controllerRunnable -> controllerRunnable.understand(context)).collect(Collectors.toList());

        if (logger.isDebugEnabled()) {
            for (ControllerRunnable<T> satisfiedController : satisfiedControllers) {
                logger.debug("{} controllerRunnable understands the context", satisfiedController.getClass().getName());
            }
        }

        /**
         * Make sure only one {@ref ControllerRunnable} instance understands the context or else return Null.
         */
        if (satisfiedControllers.size() == 1) {
            return satisfiedControllers.get(0);
        } else if (satisfiedControllers.isEmpty()) {
            logger.warn("Can NOT find any ControllerRunnable for context: {}", context);
        } else if (satisfiedControllers.size() > 1) {
            logger.warn(
                    "Found {} instances understanding the context: {}, please make sure only 1 instance understands it",
                    satisfiedControllers.size(), context);
        }

        return null;
    }

    /**
     * Build the {@ref ControllerContext} context based on execution context.
     * 
     * @param t
     * @return
     */
    protected ControllerContext<T> buildControllerContext(final T t) {
        String controllerAction = getParameterFromExecution(t, CONTROLLER_ACTION_PARAM);
        String controllerScope = getParameterFromExecution(t, CONTROLLER_SCOPE_PARAM);
        String resourceCustomizationUuid = getParameterFromExecution(t, RESOURCE_CUSTOMIZATION_UUID_PARAM);
        String controllerActor = getControllerActor(t, controllerScope, resourceCustomizationUuid, controllerAction);
        ControllerContext<T> controllerContext = new ControllerContext<>();
        controllerContext.setExecution(t);
        controllerContext.setControllerAction(controllerAction);
        controllerContext.setControllerActor(controllerActor);
        controllerContext.setControllerScope(controllerScope);
        return controllerContext;
    }

    /**
     * Retrieve the controller actor.
     * 
     * @param t
     * @param controllerScope
     * @param resourceCustomizationUuid
     * @param controllerAction
     * @return
     */
    protected abstract String getControllerActor(T t, String controllerScope, String resourceCustomizationUuid,
            String controllerAction);

    /**
     * Controller execution based on the Controller Context.
     * 
     * @param controllerContext
     */
    public abstract void controllerExecute(final ControllerContext<T> controllerContext);

    /**
     * Retrieve the parameter value as String from the execution context.
     * 
     * @param t execution Context
     * @param parameterName parameter name
     * @return String value
     */
    protected abstract String getParameterFromExecution(final T t, String parameterName);

    /**
     * Check whether the controller actor value is SO ref value, i.e, equals to SO-REF-DATA.
     * 
     * @param controllerActor
     * @return
     */
    protected boolean isSoRefControllerActor(final String controllerActor) {
        return !Strings.isNullOrEmpty(controllerActor) && controllerActor.equalsIgnoreCase("SO-REF-DATA");
    }

    /**
     * Check whether the controller scope is PNF resource related.
     * 
     * @param controllerScope
     * @return
     */
    protected boolean isPnfResourceScope(final String controllerScope) {
        return ("pnf").equalsIgnoreCase(controllerScope);
    }

    /**
     * Check whether the controller scope is VNF resource related.
     * 
     * @param controllerScope
     * @return
     */
    protected boolean isVnfResourceScope(final String controllerScope) {
        if (Strings.isNullOrEmpty(controllerScope)) {
            return false;
        }
        if (controllerScope.toLowerCase().startsWith("vnf") || controllerScope.equalsIgnoreCase("vfmodule")) {
            return true;
        }
        return false;
    }
}
