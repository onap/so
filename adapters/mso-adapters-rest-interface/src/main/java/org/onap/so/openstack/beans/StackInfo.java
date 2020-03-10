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

package org.onap.so.openstack.beans;


import java.util.HashMap;
import java.util.Map;

/*
 * This Java bean class relays Heat stack status information to ActiveVOS processes.
 *
 * This bean is returned by all Heat-specific adapter operations (create, query, delete)
 */

public class StackInfo {
    // Set defaults for everything
    private String name = "";
    private String canonicalName = "";
    private HeatStatus status = HeatStatus.UNKNOWN;
    private String statusMessage = "";
    private boolean operationPerformed;
    private Map<String, Object> outputs = new HashMap<>();
    private Map<String, Object> parameters = new HashMap<>();

    public StackInfo() {}

    public StackInfo(String name, HeatStatus status, String statusMessage, Map<String, Object> outputs) {
        this.name = name;
        this.canonicalName = name; // Don't have an ID, so just use name

        this.status = status;
        if (statusMessage != null)
            this.statusMessage = statusMessage;
        if (outputs != null)
            this.outputs = outputs;
    }

    public StackInfo(String name, HeatStatus status) {
        this.name = name;
        this.canonicalName = name; // Don't have an ID, so just use name
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String name) {
        this.canonicalName = name;
    }

    public HeatStatus getStatus() {
        return status;
    }

    public void setStatus(HeatStatus status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public boolean isOperationPerformed() {
        return operationPerformed;
    }

    public void setOperationPerformed(boolean operationPerformed) {
        this.operationPerformed = operationPerformed;
    }



}

