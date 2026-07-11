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
 * Thread-safe holder for the ASDC controller lifecycle state (STOPPED/IDLE/BUSY) and the count of notifications
 * currently being processed. Extracted from ASDCController; the notification callback may be invoked concurrently, so
 * every mutation and read is synchronized.
 */
public class ControllerState {

    private ASDCControllerStatus controllerStatus = ASDCControllerStatus.STOPPED;
    private int nbOfNotificationsOngoing = 0;

    public synchronized void changeControllerStatus(ASDCControllerStatus newControllerStatus) {
        switch (newControllerStatus) {
            case BUSY:
                ++this.nbOfNotificationsOngoing;
                this.controllerStatus = newControllerStatus;
                break;
            case IDLE:
                changeOnStatusIDLE(newControllerStatus);
                break;
            default:
                this.controllerStatus = newControllerStatus;
                break;
        }
    }

    private void changeOnStatusIDLE(ASDCControllerStatus newControllerStatus) {
        if (this.nbOfNotificationsOngoing > 1) {
            --this.nbOfNotificationsOngoing;
        } else {
            this.nbOfNotificationsOngoing = 0;
            this.controllerStatus = newControllerStatus;
        }
    }

    public synchronized ASDCControllerStatus getControllerStatus() {
        return this.controllerStatus;
    }

    public synchronized int getNbOfNotificationsOngoing() {
        return this.nbOfNotificationsOngoing;
    }

    public synchronized boolean isStopped() {
        return this.controllerStatus == ASDCControllerStatus.STOPPED;
    }

    public synchronized boolean isBusy() {
        return this.controllerStatus == ASDCControllerStatus.BUSY;
    }
}
