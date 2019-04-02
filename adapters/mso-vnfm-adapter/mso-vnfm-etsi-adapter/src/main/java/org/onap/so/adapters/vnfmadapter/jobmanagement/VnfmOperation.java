/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vnfmadapter.jobmanagement;

/**
 * Represents an operation on a VNFM.
 */
public class VnfmOperation {

    private final String vnfmId;
    private final String operationId;
    private boolean waitForNotificationForSuccess = false;
    private boolean isNotificationProcessed = false;

    public VnfmOperation(final String vnfmId, final String operationId, final boolean waitForNotificationForSuccess) {
        this.vnfmId = vnfmId;
        this.operationId = operationId;
        this.waitForNotificationForSuccess = waitForNotificationForSuccess;
    }

    /**
     * Get the ID of the operation on the VNFM.
     *
     * @return the ID of the operation on the VNFM
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Get the ID of the VNFM the operation is carried out by.
     *
     * @return the ID of the VNFM
     */
    public String getVnfmId() {
        return vnfmId;
    }

    /**
     * Check if a notification should be processed before the operation is considered successfully
     * completed.
     *
     * @return <code>true></code> if a notification must be processed before the operation is considered
     *         successfully completed, <code>false</code> otherwise
     */
    public boolean isWaitForNotificationForSuccess() {
        return waitForNotificationForSuccess;
    }

    /**
     * Set the required notification has been processed for the operation.
     */
    public void setNotificationProcessed() {
        this.isNotificationProcessed = true;
    }

    /**
     * Check if the required notification has been processed.
     *
     * @return <code>true</code> of the required notification has been processed, <code>false</code>
     *         otherwise
     */
    public boolean isNotificationProcessed() {
        return isNotificationProcessed;
    }

}
