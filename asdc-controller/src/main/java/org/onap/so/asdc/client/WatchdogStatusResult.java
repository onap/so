/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.asdc.client;

/**
 * Immutable outcome of waiting for all distribution components to report their status.
 */
public class WatchdogStatusResult {

    private final String overallStatus;
    private final String watchdogError;
    private final boolean deploySuccess;

    public WatchdogStatusResult(String overallStatus, String watchdogError, boolean deploySuccess) {
        this.overallStatus = overallStatus;
        this.watchdogError = watchdogError;
        this.deploySuccess = deploySuccess;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public String getWatchdogError() {
        return watchdogError;
    }

    public boolean isDeploySuccess() {
        return deploySuccess;
    }
}
