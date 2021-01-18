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

package org.onap.so.beans.nsmf;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum OperationType {
    /**
     * allocate
     */
    ALLOCATE("allocate"),

    DEALLOCATE("deallocate"),

    CREATE("create"),

    TERMINATE("terminate"),

    ACTIVATE("activation"),

    DEACTIVATE("deactivation");

    private String type;


    OperationType(String type) {
        this.type = type;
    }

    public static OperationType getOperationType(String value) {
        for (OperationType operationType : OperationType.values()) {
            if (operationType.type.equalsIgnoreCase(value)) {
                return operationType;
            }
        }
        return null;
    }
}
