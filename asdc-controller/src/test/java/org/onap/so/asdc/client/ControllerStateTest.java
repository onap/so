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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ControllerStateTest {

    @Test
    public void initialState_isStopped() {
        ControllerState state = new ControllerState();
        assertTrue(state.isStopped());
        assertFalse(state.isBusy());
        assertEquals(ASDCControllerStatus.STOPPED, state.getControllerStatus());
        assertEquals(0, state.getNbOfNotificationsOngoing());
    }

    @Test
    public void busy_incrementsOngoingCounter() {
        ControllerState state = new ControllerState();
        state.changeControllerStatus(ASDCControllerStatus.BUSY);
        assertTrue(state.isBusy());
        assertEquals(1, state.getNbOfNotificationsOngoing());
        state.changeControllerStatus(ASDCControllerStatus.BUSY);
        assertEquals(2, state.getNbOfNotificationsOngoing());
    }

    @Test
    public void idle_withMultipleOngoing_decrementsButStaysBusy() {
        ControllerState state = new ControllerState();
        state.changeControllerStatus(ASDCControllerStatus.BUSY);
        state.changeControllerStatus(ASDCControllerStatus.BUSY);
        state.changeControllerStatus(ASDCControllerStatus.IDLE);
        assertEquals(1, state.getNbOfNotificationsOngoing());
        assertTrue(state.isBusy()); // status only flips to IDLE when counter reaches 0
    }

    @Test
    public void idle_withSingleOngoing_resetsToIdle() {
        ControllerState state = new ControllerState();
        state.changeControllerStatus(ASDCControllerStatus.BUSY);
        state.changeControllerStatus(ASDCControllerStatus.IDLE);
        assertEquals(0, state.getNbOfNotificationsOngoing());
        assertEquals(ASDCControllerStatus.IDLE, state.getControllerStatus());
    }

    @Test
    public void stopped_setsStatusDirectly() {
        ControllerState state = new ControllerState();
        state.changeControllerStatus(ASDCControllerStatus.BUSY);
        state.changeControllerStatus(ASDCControllerStatus.STOPPED);
        assertTrue(state.isStopped());
    }
}
