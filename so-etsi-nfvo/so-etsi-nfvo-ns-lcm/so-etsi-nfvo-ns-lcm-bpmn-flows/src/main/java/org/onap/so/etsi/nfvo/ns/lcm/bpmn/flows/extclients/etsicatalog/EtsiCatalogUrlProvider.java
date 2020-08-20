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

package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.etsicatalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class EtsiCatalogUrlProvider {

    @Value("${etsi-catalog-manager.base.endpoint}")
    private String etsiCatalogManagerEndpoint;

    public EtsiCatalogUrlProvider() {}

    public String getNsPackageUrl(final String nsdId) {
        return etsiCatalogManagerEndpoint + "/nsd/v1/ns_descriptors/" + nsdId;
    }

    public String getNsPackageContentUrl(final String nsdId) {
        return etsiCatalogManagerEndpoint + "/nsd/v1/ns_descriptors/" + nsdId + "/nsd_content";
    }

    public String getVnfPackageUrl(final String vnfPkgId) {
        return etsiCatalogManagerEndpoint + "/vnfpkgm/v1/vnf_packages/" + vnfPkgId;
    }


}
