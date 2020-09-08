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

import java.util.Optional;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.NsdInfo;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.VnfPkgInfo;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd.NetworkServiceDescriptor;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface EtsiCatalogPackageManagementServiceProvider {

    Optional<NsdInfo> getNSPackageModel(final String nsdId);

    Optional<VnfPkgInfo> getVnfPkgInfo(final String vnfPkgId);

    Optional<NetworkServiceDescriptor> getNetworkServiceDescriptor(final String nsdId);

}
