/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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

package org.onap.so.db.catalog.beans;

public enum OrchestrationStatus {
    ACTIVE("Active", "active"),
    ACTIVATED("Activated", "activated"),
    ASSIGNED("Assigned", "assigned"),
    CREATED("Created", "created"),
    INVENTORIED("Inventoried", "inventoried"),
    PENDING("Pending", "pending"),
    PENDING_ACTIVATION("PendingActivation", "pending.?activation"),
    PENDING_CREATE("PendingCreate", "pending.?create"),
    PENDING_DELETE("PendingDelete", "pending.?delete"),
    PRECREATED("PreCreated", "pre.?created"),
    CONFIGASSIGNED("ConfigAssigned", "config.?assigned"),
    CONFIGDEPLOYED("ConfigDeployed", "config.?deployed"),
    CONFIGURE("Configure", "configure"),
    CONFIGURED("Configured", "configured"),
    REGISTER("Register", "register"),
    REGISTERED("Registered", "registered");

    private final String name;
    private final String fuzzyMatcher;

    private OrchestrationStatus(String name, String fuzzyMatcher) {
        this.name = name;
        this.fuzzyMatcher = fuzzyMatcher;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * case insensitive regular expression match to enum value
     *
     * @param status
     * @return
     */
    public boolean fuzzyMap(String status) {
        if (status != null) {
            return status.matches("(?i)" + this.fuzzyMatcher);
        } else {
            return false;
        }
    }
}
