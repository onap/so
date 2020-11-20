/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 # Copyright (c) 2020, CMCC Technologies Co., Ltd.
 #
 # Licensed under the Apache License, Version 2.0 (the "License")
 # you may not use this file except in compliance with the License.
 # You may obtain a copy of the License at
 #
 #       http://www.apache.org/licenses/LICENSE-2.0
 #
 # Unless required by applicable law or agreed to in writing, software
 # distributed under the License is distributed on an "AS IS" BASIS,
 # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 # See the License for the specific language governing permissions and
 # limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.nssmf.enums;

import lombok.Getter;

@Getter
public enum ActionType {
    /**
     * allocate
     */
    ALLOCATE("allocate"),

    DEALLOCATE("deallocate"),

    CREATE("create"),

    TERMINATE("terminate"),

    ACTIVATE("activate"),

    DEACTIVATE("deactivate"),

    QUERY_JOB_STATUS("query_job_status"),

    MODIFY_BY_ID("modify_by_id"),

    MODIFY("modify"),

    QUERY_NSSI_SELECTION_CAPABILITY("query_nssi_selection_capability"),

    QUERY_SUB_NET_CAPABILITY("query_sub_net_capability"),;

    private String type;


    ActionType(String type) {
        this.type = type;
    }

    public static ActionType getActionType(String value) {
        for (ActionType actionType : ActionType.values()) {
            if (actionType.type.equalsIgnoreCase(value)) {
                return actionType;
            }
        }
        return null;
    }
}
