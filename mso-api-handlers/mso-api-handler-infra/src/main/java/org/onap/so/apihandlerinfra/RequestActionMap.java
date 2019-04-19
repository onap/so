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

package org.onap.so.apihandlerinfra;

import java.util.HashMap;
import java.util.Map;

/*
 * Map of actions to RequestActions
 */
public class RequestActionMap {
    private static final Map<String, String> actionMap;
    private static final String CREATE_INSTANCE = "createInstance";
    private static final String DELETE_INSTANCE = "deleteInstance";
    private static final String UPDATE_INSTANCE = "updateInstance";
    private static final String REPLACE_INSTANCE = "replaceInstance";


    static {
        actionMap = new HashMap<>();
        actionMap.put("CREATE_VF_MODULE", CREATE_INSTANCE);
        actionMap.put("DELETE_VF_MODULE", DELETE_INSTANCE);
        actionMap.put("UPDATE_VF_MODULE", UPDATE_INSTANCE);
        actionMap.put("CREATE_VF_MODULE_VOL", CREATE_INSTANCE);
        actionMap.put("DELETE_VF_MODULE_VOL", DELETE_INSTANCE);
        actionMap.put("UPDATE_VF_MODULE_VOL", UPDATE_INSTANCE);
        actionMap.put("CREATE", CREATE_INSTANCE);
        actionMap.put("DELETE", DELETE_INSTANCE);
        actionMap.put("UPDATE", UPDATE_INSTANCE);
        actionMap.put("createInstance", CREATE_INSTANCE);
        actionMap.put("deleteInstance", DELETE_INSTANCE);
        actionMap.put("updateInstance", UPDATE_INSTANCE);
        actionMap.put("replaceInstance", REPLACE_INSTANCE);

    }

    public static String getMappedRequestAction(String action) {
        return actionMap.get(action);
    }
}