/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.common;

public interface SoPropertyConstants {
    String TARGET_SOFTWARE_VERSION = "targetSoftwareVersion";
    String RECIPE_TIMEOUT = "recipeTimeout";

    String SO_ACTION = "action";

    String ACTION_ACTIVATE_N_E_SW = "activateNESw";
    String ACTION_DOWNLOAD_N_E_SW = "downloadNESw";
    String ACTION_POST_CHECK = "postCheck";
    String ACTION_PRE_CHECK = "preCheck";

    String CONTROLLER_STATUS = "ControllerStatus";
}
