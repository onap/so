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

package org.openecomp.mso.apihandlerinfra;

import java.util.HashMap;
import java.util.Map;

/*
 * Map of actions to RequestActions
*/
public class RequestActionMap {
    private static final Map<String, String> actionMap;

    private RequestActionMap() {
    }

    static {
        actionMap = new HashMap<>();
        actionMap.put("CREATE_VF_MODULE", "createInstance");
        actionMap.put("DELETE_VF_MODULE", "deleteInstance");
        actionMap.put("UPDATE_VF_MODULE", "updateInstance");
        actionMap.put("CREATE_VF_MODULE_VOL", "createInstance");
        actionMap.put("DELETE_VF_MODULE_VOL", "deleteInstance");
        actionMap.put("UPDATE_VF_MODULE_VOL", "updateInstance");
        actionMap.put("CREATE", "createInstance");
        actionMap.put("DELETE", "deleteInstance");
        actionMap.put("UPDATE", "updateInstance");
        actionMap.put("createInstance", "createInstance");
        actionMap.put("deleteInstance", "deleteInstance");
        actionMap.put("updateInstance", "updateInstance");
        actionMap.put("replaceInstance", "replaceInstance");

    }

    public static String getMappedRequestAction(String action) {
        return actionMap.get(action);
    }
}
