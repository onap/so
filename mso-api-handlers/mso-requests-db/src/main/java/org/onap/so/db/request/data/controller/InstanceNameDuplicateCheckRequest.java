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

package org.onap.so.db.request.data.controller;

import java.util.HashMap;

public class InstanceNameDuplicateCheckRequest {

    private HashMap<String, String> instanceIdMap;
    private String instanceName;
    private String requestScope;

    public InstanceNameDuplicateCheckRequest() {}

    public InstanceNameDuplicateCheckRequest(HashMap<String, String> instanceIdMap, String instanceName,
            String requestScope) {
        this.instanceIdMap = instanceIdMap;
        this.instanceName = instanceName;
        this.requestScope = requestScope;
    }

    public HashMap<String, String> getInstanceIdMap() {
        return instanceIdMap;
    }

    public void setInstanceIdMap(HashMap<String, String> instanceIdMap) {
        this.instanceIdMap = instanceIdMap;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getRequestScope() {
        return requestScope;
    }

    public void setRequestScope(String requestScope) {
        this.requestScope = requestScope;
    }
}
