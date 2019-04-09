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
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.*;

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
     * Invoke an instantiate request for a VNF.
     *
     * @param vnfSelfLink the link to he VNF on the VNFM
     * @param instantiateVnfRequest the instantiate request
     * @return the operation ID of the instantiation operation
     */
    String instantiateVnf(final String vnfSelfLink, final InstantiateVnfRequest instantiateVnfRequest);

    /**
     * Invoke a notification subscription request to a VNFM.
     *
     * @param vnfmId the ID of the VNFM
     * @param subscriptionRequest
     * @return the response to the subscription request
     */
    InlineResponse2001 subscribeForNotifications(final String vnfmId,
            final LccnSubscriptionRequest subscriptionRequest);

    /**
     * Invoke a terminate request for a VNF.
     *
     * @param vnfSelfLink the link to he VNF on the VNFM
     * @param terminateVnfRequest the terminate request
     * @return the operation ID of the termination operation
     */
    String terminateVnf(final String vnfSelfLink, final TerminateVnfRequest terminateVnfRequest);

    /**
     * Invoke a delete request for a VNF.
     *
     * @param vnfSelfLink the link to he VNF on the VNFM
     * @return the operation ID of the instantiation operation
     */
    void deleteVnf(final String vnfSelfLink);

    /**
     * Invoke a get request for a VNFM operation.
     *
     * @param vnfmId the id of the VNFM in AAI
     * @param operationId the id of the operation on the VNFM
     * @return the operation from the VNFM
     */
    Optional<InlineResponse200> getOperation(final String vnfmId, final String operationId);

    /**
     * Invoke a create request to a VNFM
     *
     * @param vnfmId the id of the VNFM in AAI
     * @param createVnfRequest the parameters for creating a VNF
     * @return the newly created VNF
     */
    Optional<InlineResponse201> createVnf(final String vnfmId, final CreateVnfRequest createVnfRequest);

}
