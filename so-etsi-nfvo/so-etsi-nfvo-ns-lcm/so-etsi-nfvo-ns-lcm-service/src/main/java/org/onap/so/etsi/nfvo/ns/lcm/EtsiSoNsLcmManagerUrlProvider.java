/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
package org.onap.so.etsi.nfvo.ns.lcm;

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Configuration
public class EtsiSoNsLcmManagerUrlProvider {

    private final String etsiNsLcmManagerEndpoint;

    @Autowired
    public EtsiSoNsLcmManagerUrlProvider(
            @Value("${etsi-so-ns-lcm-manager.endpoint}") final String etsiNsLcmManagerEndpoint) {
        this.etsiNsLcmManagerEndpoint = etsiNsLcmManagerEndpoint;
    }

    public URI getCreatedNsResourceUri(final String nsInstanceId) {
        return URI.create(etsiNsLcmManagerEndpoint + Constants.NS_LIFE_CYCLE_MANAGEMENT_BASE_URL + "/ns_instances/"
                + nsInstanceId);
    }

    public URI getInstantiatedOccUri(final String nsLcmOpOccId) {
        return URI.create(etsiNsLcmManagerEndpoint + Constants.NS_LIFE_CYCLE_MANAGEMENT_BASE_URL + "/ns_lcm_op_occs/"
                + nsLcmOpOccId);
    }

}
