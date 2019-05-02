/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 *  Modifications Copyright (c) 2019 Samsung
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

package org.onap.svnfm.simulator.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Ronan Kenny (ronan.kenny@est.tech)
 */
public class VnfmAdapterCreationNotification implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(VnfmAdapterCreationNotification.class);

    @Override
    public void run() {
        try {
            Thread.sleep(10000);
        } catch (final InterruptedException e) {
            logger.error("Error occured while simulating creation ", e);
            Thread.currentThread().interrupt();
        }
        logger.info("Call to VNFM Adapter-Create");
    }

}
