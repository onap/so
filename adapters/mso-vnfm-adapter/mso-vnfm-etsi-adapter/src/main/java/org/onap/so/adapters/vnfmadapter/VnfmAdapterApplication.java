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

package org.onap.so.adapters.vnfmadapter;

import static org.slf4j.LoggerFactory.getLogger;
import org.onap.so.adapters.vnfmadapter.rest.VnfmAdapterController;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The spring boot application for the VNFM (Virtual Network Function Manager) Adapter.
 * <p>
 * The VNFM Adapter receives requests through its REST API {@link VnfmAdapterController} which it
 * adapts into ETSI SOL003 compliant LCM (Life Cycle Management) calls towards an ETSI compliant
 * VNFM.
 *
 * @see <a href=
 *      "https://www.etsi.org/deliver/etsi_gs/NFV-SOL/001_099/003/02.05.01_60/gs_nfv-sol003v020501p.pdf">ETSI
 *      SOL003 v2.5.1</a>
 */
@SpringBootApplication(scanBasePackages = {"org.onap.so"})
public class VnfmAdapterApplication {
    private static final Logger logger = getLogger(VnfmAdapterApplication.class);


    /**
     * Entry point for the Spring boot application
     *
     * @param args arguments for the application
     */
    public static void main(final String[] args) {
        new SpringApplication(VnfmAdapterApplication.class).run(args);
        logger.info("VnfmAdapterApplication started!");
    }

}
