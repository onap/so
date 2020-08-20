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

package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.aai;

import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import java.util.Optional;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public interface AaiServiceProvider {

    void createServiceInstance(final String globalCustomerId, final String serviceType,
            final ServiceInstance aaiServiceInstance);

    void createGenericVnfAndConnectServiceInstance(final String serviceInstanceId, final String vnfId,
            final GenericVnf genericVnf);

    void connectGenericVnfToTenant(final String vnfId, final String cloudOwner, final String cloudRegion,
            final String tenantId);

    Optional<GenericVnf> getGenericVnf(final String vnfId);

}
