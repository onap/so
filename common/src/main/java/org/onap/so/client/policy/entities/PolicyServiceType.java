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

package org.onap.so.client.policy.entities;

public enum PolicyServiceType {
    GET_CONFIG("getConfig"),
    SEND_EVENT("sendEvent"),
    PUSH_POLICY("pushPolicy"),
    CREATE_POLICY("createPolicy"),
    UPDATE_POLICY("updatePolicy"),
    GET_DECISION("getDecision"),
    GET_METRICS("getMetrics"),
    DELETE_POLICY("deletePolicy"),
    LIST_CONFIG("listConfig"),
    CREATE_DICTIONARY_ITEM("createDictionaryItem"),
    UPDATE_DICTIONARY_ITEM("updateDictionaryItem"),
    GET_DICTIONARY_ITEMS("getDictionaryItems");

    private final String name;

    PolicyServiceType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
