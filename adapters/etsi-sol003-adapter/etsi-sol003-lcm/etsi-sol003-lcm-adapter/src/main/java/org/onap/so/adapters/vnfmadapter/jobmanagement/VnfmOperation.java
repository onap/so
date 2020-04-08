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
    private NotificationStatus notificationStatus;
    private boolean vnfDeleted;

    public VnfmOperation(final String vnfmId, final String operationId, final boolean waitForNotificationForSuccess) {
        this.vnfmId = vnfmId;
        this.operationId = operationId;
        this.notificationStatus = waitForNotificationForSuccess ? NotificationStatus.NOTIFICATION_PROCESSING_PENDING
                : NotificationStatus.NOTIFICATION_PROCESSING_NOT_REQUIRED;
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
     * Set the required notification has been processed for the operation.
     *
     * @param notificationProcessingWasSuccessful <code>true</code> if the notification processing was successful,
     *        <code>false<code> otherwise
     */
    public void setNotificationProcessed(final boolean notificationProcessingWasSuccessful) {
        this.notificationStatus =
                notificationProcessingWasSuccessful ? NotificationStatus.NOTIFICATION_PROCEESING_SUCCESSFUL
                        : NotificationStatus.NOTIFICATION_PROCESSING_FAILED;
    }

    /**
     * Get the notification status for the operation.
     *
     * @return the notification status
     */
    public NotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    /**
     * Set the VNF has been deleted from the VNFM.
     */
    public void setVnfDeleted() {
        this.vnfDeleted = true;
    }

    /**
     * Check if the VNF has been deleted from the VNFM
     *
     * @return <code>true</code> of the VNF has been deleted from the VNFM, <code>false</code> otherwise
     */
    public boolean isVnfDeleted() {
        return vnfDeleted;
    }


    public enum NotificationStatus {
        /**
         * No notification handling is required to determine the status of the operation
         */
        NOTIFICATION_PROCESSING_NOT_REQUIRED,
        /**
         * A notification must be processed before the notification can be considered to be completed
         */
        NOTIFICATION_PROCESSING_PENDING,
        /**
         * A notification has been successfully handled for the operation
         */
        NOTIFICATION_PROCEESING_SUCCESSFUL,
        /**
         * An error occurred processing a notification for the operation
         */
        NOTIFICATION_PROCESSING_FAILED;
    }

    @Override
    public String toString() {
        return "VnfmOperation [vnfmId=" + vnfmId + ", operationId=" + operationId + ", notificationStatus="
                + notificationStatus + "]";
    }


}
