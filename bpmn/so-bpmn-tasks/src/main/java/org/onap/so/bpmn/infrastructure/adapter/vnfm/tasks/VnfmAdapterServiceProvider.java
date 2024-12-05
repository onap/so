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

package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.DeleteVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse;
import com.google.common.base.Optional;



/**
 * Provide a service which interacts with VNFM adapter for instantiating, monitoring VNF
 *
 * @author waqas.ikram@est.tech
 */
public interface VnfmAdapterServiceProvider {

    Optional<CreateVnfResponse> invokeCreateInstantiationRequest(final String vnfId, final CreateVnfRequest request);

    Optional<DeleteVnfResponse> invokeDeleteRequest(final String vnfId);

    Optional<QueryJobResponse> getInstantiateOperationJobStatus(final String jobId);

}
