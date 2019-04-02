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

package org.onap.so.adapters.vnfmadapter.extclients.vnfm;

import com.google.common.base.Optional;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;

/**
 * Provides methods for invoking REST calls to a VNFM.
 */
public interface VnfmServiceProvider {

    /**
     * Invoke a get request for a VNF.
     *
     * @param vnfSelfLink the link to the VNF in the VNFM
     * @return the VNF from the VNFM
     */
    Optional<InlineResponse201> getVnf(final String vnfSelfLink);

    /**
     * Invoke a get request for a VNFM operation.
     *
     * @param vnfmId the id of the VNFM in AAI
     * @param operationId the id of the operation on the VNFM
     * @return the operation from the VNFM
     */
    Optional<InlineResponse200> getOperation(final String vnfmId, final String operationId);

}
