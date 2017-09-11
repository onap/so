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

package org.openecomp.mso.asdc;


import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.openecomp.mso.asdc.client.ASDCGlobalController;
import org.openecomp.mso.logger.MsoLogger;

@Singleton(name = "ASDCController")
@Lock(LockType.READ)
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class ASDCControllerSingleton {

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger (MsoLogger.Catalog.ASDC);
    private static boolean working = false;

    private ASDCGlobalController globalController = new ASDCGlobalController ();

    /**
     * Main Constructor of the ASDC Singleton
     */
    public ASDCControllerSingleton () {
    }

    @Schedule(second ="30", minute = "*", hour = "*", persistent = false)
    public void periodicControllerTask () {
        if (isWorking ()) {
            LOGGER.debug ("Another thread is already trying to init ASDC, cancel this periodic call");
            return;
        }
        try {
            setWorking (true);
            
            globalController.updateControllersConfigIfNeeded();
            globalController.checkInStoppedState();

        } finally {
            setWorking (false);
        }
    }

    @PreDestroy
    private void terminate () {
        globalController.closeASDC ();
    }

    private static synchronized boolean isWorking () {
        return ASDCControllerSingleton.working;
    }

    private static synchronized void setWorking (boolean working) {
        ASDCControllerSingleton.working = working;
    }
}
